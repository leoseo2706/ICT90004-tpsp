package com.core.tpsp.service;

import com.core.tpsp.constant.TPSPConstants;
import com.core.tpsp.entity.*;
import com.core.tpsp.exception.TpspException;
import com.core.tpsp.payload.ClazzDTO;
import com.core.tpsp.payload.ExcelReportDTO;
import com.core.tpsp.payload.UnitDTO;
import com.core.tpsp.payload.UserDTO;
import com.core.tpsp.repo.*;
import com.core.tpsp.utils.ExcelHelper;
import com.core.tpsp.utils.TpspUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ExcelServiceImpl implements ExcelService {

    @Value("#{'${report.app.header}'.split(',')}")
    private List<String> appHeader;

    @Value("${report.app.sheet}")
    private String appSheet;

    @Value("#{'${report.convenor.ranking.header}'.split(',')}")
    private List<String> convenorRateHeader;

    @Value("${report.convenor.ranking.sheet}")
    private String convenorRateSheet;

    @Autowired
    ObjectMapper mapper;

    @Autowired
    private ExcelHelper excelHelper;

    @Autowired
    private ApplicationRepo applicationRepo;

    @Autowired
    private RoleRepo roleRepo;

    @Autowired
    private UserRoleRepo userRoleRepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private TutorPreferenceRepo tutorPreferenceRepo;

    @Autowired
    private ClazzRepo clazzRepo;

    @Autowired
    private UnitRepo unitRepo;

    @Override
    public ExcelReportDTO loadAllocationFile() {

        log.info("Loading applicant file ...");
        Set<String> userIds = new HashSet<>();
        Set<Integer> clazzIds = new HashSet<>();
        List<Application> apps = applicationRepo.findAllByOrderByApplicationIdDesc();
        apps.forEach(a -> {
            userIds.add(a.getApplicant());
            clazzIds.add(a.getAppliedClass());
        });
        log.info("Done loading application table apps {}, userIds: {}, clazzIds: {}",
                apps, userIds, clazzIds);

        // class data
        CompletableFuture<Map<Integer, ClazzDTO>> clazzFuture = CompletableFuture.supplyAsync(() -> {
            List<ClazzDTO> classes = clazzRepo.findByIdIn(clazzIds).stream()
                    .map(c -> {
                        ClazzDTO dto = ClazzDTO.builder().build();
                        BeanUtils.copyProperties(c, dto,
                                "startDate", "startTimeScheduled", "endTimeScheduled");
                        dto.setStartDate(TpspUtils.format(c.getStartDate()));
                        dto.setStartTimeScheduled(TpspUtils.format(c.getStartTimeScheduled()));
                        dto.setEndTimeScheduled(TpspUtils.format(c.getEndTimeScheduled()));
                        return dto;
                    }).collect(Collectors.toList());
            log.info("Done extracting class data {}", classes);

            Set<Integer> unitIds = classes.stream().map(x -> x.getUnitId()).collect(Collectors.toSet());
            List<UnitDTO> units = unitRepo.findByIdIn(unitIds).stream()
                    .map(x -> {
                        UnitDTO unitDTO = UnitDTO.builder().build();
                        BeanUtils.copyProperties(x, unitDTO);
                        return unitDTO;
                    }).collect(Collectors.toList());
            Map<Integer, UnitDTO> unitDTOMap = units.stream()
                    .collect(Collectors.toMap(UnitDTO::getId, Function.identity(), (o, n) -> n));
            for (ClazzDTO clazzDTO : classes) {
                clazzDTO.setUnitDTO(unitDTOMap.get(clazzDTO.getUnitId()));
            }
            Map<Integer, ClazzDTO> classesMap = classes.stream()
                    .collect(Collectors.toMap(ClazzDTO::getId, Function.identity(), (o, n) -> n));
            return classesMap;
        });


        // user data
        CompletableFuture<Map<String, UserDTO>> userFuture = CompletableFuture.supplyAsync(() -> {
            List<UserDTO> userDTOS = userRepo.findByIdIn(userIds).stream().map(x -> {
                UserDTO dto = UserDTO.builder().build();
                BeanUtils.copyProperties(x, dto);
                return dto;
            }).collect(Collectors.toList());
            Map<String, UserDTO> userMap = userDTOS.stream()
                    .collect(Collectors.toMap(UserDTO::getId, Function.identity(), (o, n) -> n));
            log.info("Done extracting user data {}", userMap.toString());
            return userMap;
        });

        // final data
        CompletableFuture<List<List<Object>>> combinedFuture = clazzFuture.thenCombine(userFuture,
                (clazzData, userData) -> {

                    List<List<Object>> result = apps.stream().map(x -> {
                        List<Object> list = new ArrayList<>();
                        list.add(x.getApplicationId());

                        UserDTO userDTO = userData.get(x.getApplicant());
                        list.add(userDTO != null ? userDTO.getUserName() : "");

                        ClazzDTO clazzDTO = clazzData.get(x.getAppliedClass());
                        if (clazzDTO != null) {
                            UnitDTO unitDTO = clazzDTO.getUnitDTO();
                            list.add(unitDTO.getUnitCode());
                            list.add(unitDTO.getUnitName());
                        } else {
                            list.add("");
                            list.add("");
                        }
                        list.add(x.getApproved());
                        list.add(x.getPa());
                        list.add(x.getPreference());
                        return list;
                    }).collect(Collectors.toList());
                    log.info("Successfully combined application data list: {} ", result);
                    return result;
                });

        ByteArrayInputStream byteData = null;
        try {
            byteData = excelHelper.toExcelReport(combinedFuture.get(), appHeader, appSheet);
        } catch (ExecutionException | InterruptedException e) {
            log.error("Error running query {}", e);
            throw new TpspException("Failed to download the application file!");
        }
        log.info("Done creating excel data ...");
        return new ExcelReportDTO(appSheet + ".xlsx", byteData);
    }

    @Override
    public ExcelReportDTO loadConvenorRankingFile() {

        log.info("Begin downloading convenor ranking file ...");

        Optional<Role> optionalRole = roleRepo.findByName(TPSPConstants.ROLE.CONVENOR);
        if (!optionalRole.isPresent()) {
            throw new TpspException("Unavailable role ");
        }

        log.info("Found role {}", TPSPConstants.ROLE.CONVENOR);

        List<UserRole> userRoleList = userRoleRepo.findByUserRoleKeyRoleId(optionalRole.get().getId());
        Set<String> userIds = userRoleList.stream()
                .map(a -> a.getUserRoleKey().getUserId()).collect(Collectors.toSet());
        log.info("Found user ID list {}", TpspUtils.toJsonString(mapper, userIds));

        // main data future
        CompletableFuture<List<UserDTO>> userFuture = CompletableFuture.supplyAsync(() -> {
            List<User> users = userRepo.findByIdIn(userIds);
            List<UserDTO> data = users.stream().map(x -> {
                UserDTO dto = UserDTO.builder()
                        .role(TPSPConstants.ROLE.CONVENOR)
                        .build();
                BeanUtils.copyProperties(x, dto);
                return dto;
            }).collect(Collectors.toList());
            log.info("Successfully found user list: {} ", TpspUtils.toJsonString(mapper, data));
            return data;
        });

        // ranking data future
        CompletableFuture<Map<String, Double>> rankingFuture = CompletableFuture.supplyAsync(() -> {
            List<TutorPreference> rankings = tutorPreferenceRepo.findByConvenorIdIn(userIds);
            Map<String, Double> rankingData = rankings.stream()
                    .collect(Collectors.groupingBy(TutorPreference::getConvenorId, Collectors.averagingInt(TutorPreference::getRating)));
            log.info("Successfully found user ranking data list: {} ", TpspUtils.toJsonString(mapper, rankingData));
            return rankingData;
        });

        // combined future
        CompletableFuture<List<List<Object>>> combinedFuture = userFuture.thenCombine(rankingFuture,
                (userData, rankingData) -> {
                    List<List<Object>> result = userData.stream().map(x -> {
                        List<Object> list = new ArrayList<>();
                        list.add(x.getId());
                        list.add(x.getUserName());
                        list.add(x.getRole());
                        list.add(x.getFirstName() + " " + x.getLastName());
                        list.add(x.getEmail());
                        list.add(x.getPhoneNumber());
                        Double averageRate = rankingData.get(x.getId());
                        list.add(averageRate == null ? 0.0 : averageRate);
                        list.add(x.getSwinburneId());
                        list.add(TpspUtils.concatenateAddress(x.getStreet(), x.getCity(),
                                x.getState(), x.getPostalCode()));
                        list.add(x.getQualification());
                        list.add(x.getLinkedinUrl());
                        list.add(x.getCitizenshipStudyStatus());
                        list.add(x.getAustralianWorkRights());
                        list.add(x.getNumberYearsWorkExperience());
                        list.add(x.getPreviousTeachingExperience());
                        list.add(x.getPublications());
                        return list;
                    }).collect(Collectors.toList());

                    log.info("Successfully combined user data list: {} ",
                            TpspUtils.toJsonString(mapper, result));
                    return result;
                });

        // blocking and get
        ByteArrayInputStream byteData = null;
        try {
            byteData = excelHelper.toExcelReport(combinedFuture.get(),
                    convenorRateHeader, convenorRateSheet);
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error running query {}", e);
            throw new TpspException("Error while extracting convenor data!");
        }

        ExcelReportDTO result = ExcelReportDTO.builder()
                .fileName(convenorRateSheet + ".xlsx").data(byteData)
                .build();
        log.info("Done creating excel data ...");

        return result;
    }

    @Override
    public ExcelReportDTO loadUnitFile() {

        log.info("Loading unit file ...");

        return null;
    }
}
