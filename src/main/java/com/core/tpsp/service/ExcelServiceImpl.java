package com.core.tpsp.service;

import com.core.tpsp.constant.TPSPConstants;
import com.core.tpsp.entity.*;
import com.core.tpsp.exception.TpspException;
import com.core.tpsp.payload.*;
import com.core.tpsp.repo.*;
import com.core.tpsp.utils.ExcelHelper;
import com.core.tpsp.utils.TpspUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

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

    @Value("${report.allocation.sheet}")
    private String allocationSheet;

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
    public ExcelReportDTO loadApplicationFile() {
        log.info("Loading applicant file ...");
        List<Application> apps = applicationRepo.findAllByOrderByApplicationIdDesc();
        return toExcelDTO(apps, appHeader, appSheet);
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
                        list.add(TpspUtils.toFullName(x.getFirstName(), x.getLastName()));
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
        ByteArrayInputStream byteData;
        try {
            byteData = excelHelper.toExcelReport(combinedFuture.get(),
                    convenorRateHeader, convenorRateSheet);
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error running query {}", e);
            throw new TpspException("Error while extracting convenor data!");
        }

        ExcelReportDTO result = ExcelReportDTO.builder()
                .fileName(convenorRateSheet + TPSPConstants.FILE_EXTENSION)
                .data(byteData).build();
        log.info("Done creating excel data ...");

        return result;
    }

    @Override
    public ExcelReportDTO loadAllocationFile() {
        log.info("Loading allocation file ...");
        List<Application> apps = applicationRepo.findByApproved(TPSPConstants.APPROVED);
        return toExcelDTO(apps, appHeader, allocationSheet);
    }

    private ExcelReportDTO toExcelDTO (List<Application> apps, List<String> headerCols, String sheetName) {

        Set<String> userIds = new HashSet<>();
        Set<Integer> clazzIds = new HashSet<>();
        apps.forEach(a -> {
            // finding a list of applicantIDs and classIDs
            userIds.add(a.getApplicant());
            clazzIds.add(a.getAppliedClass());
        });
        log.info("Done loading application table apps {}, userIds: {}, clazzIds: {}",
                apps, userIds, clazzIds);

        // class data
        CompletableFuture<Map<Integer, ClazzDTO>> clazzFuture = initClazzFuture(userIds, clazzIds);
        try {
            // blocking to get a complete list of userIDs for userFuture
            clazzFuture.get();
        } catch (ExecutionException | InterruptedException e) {
            log.error("Error querying ", e);
            throw new TpspException("Exception happened while querying class data!");
        }

        // user data
        CompletableFuture<Map<String, UserDTO>> userFuture = initUserFuture(userIds);

        // final data
        CompletableFuture<List<List<Object>>> combinedFuture = initExcelDataList(apps, clazzFuture, userFuture);

        ByteArrayInputStream byteData;
        try {
            byteData = excelHelper.toExcelReport(combinedFuture.get(), headerCols, sheetName);
            log.info("Done creating excel data ...");
        } catch (ExecutionException | InterruptedException e) {
            log.error("Error running query {}", e);
            throw new TpspException("Failed to download the application file!");
        }
        return new ExcelReportDTO(sheetName + TPSPConstants.FILE_EXTENSION, byteData);

    }

    private CompletableFuture<Map<Integer, ClazzDTO>> initClazzFuture(Set<String> userIds, Set<Integer> clazzIds) {

        if (CollectionUtils.isEmpty(userIds) || CollectionUtils.isEmpty(clazzIds)) {
            throw new TpspException("Empty User ID or Class ID search list!");
        }

        return CompletableFuture.supplyAsync(() -> {
            Map<Integer, ClazzDTO> classes = clazzRepo.findByIdIn(clazzIds).stream()
                    .map(c -> {
                        ClazzDTO dto = ClazzDTO.builder().build();
                        BeanUtils.copyProperties(c, dto,
                                "startDate", "startTimeScheduled", "endTimeScheduled");
                        dto.setStartDate(TpspUtils.format(c.getStartDate()));
                        dto.setStartTimeScheduled(TpspUtils.format(c.getStartTimeScheduled()));
                        dto.setEndTimeScheduled(TpspUtils.format(c.getEndTimeScheduled()));
                        return dto;
                    }).collect(Collectors.toMap(ClazzDTO::getId, Function.identity(), (o, n) -> o));
            log.info("Done extracting class data {}", classes);

            Set<Integer> unitIds = classes.values().stream().map(x -> x.getUnitId()).collect(Collectors.toSet());
            log.info("Searching for unitIds {}", unitIds);

            List<UnitDTO> units = unitRepo.findByIdIn(unitIds).stream()
                    .map(x -> {
                        UnitDTO unitDTO = UnitDTO.builder().build();
                        BeanUtils.copyProperties(x, unitDTO);
                        return unitDTO;
                    }).collect(Collectors.toList());
            Map<Integer, UnitDTO> unitDTOMap = units.stream()
                    .collect(Collectors.toMap(UnitDTO::getId, Function.identity(), (o, n) -> n));
            log.info("Got unit info list {}", unitDTOMap);


            classes.forEach((k, clazzDTO) -> {
                UnitDTO unitDTO = unitDTOMap.get(clazzDTO.getUnitId());
                clazzDTO.setUnitDTO(unitDTO);

                // adding for search list
                if (unitDTO != null && !StringUtils.isEmpty(unitDTO.getUnitOwner())) {
                    userIds.add(unitDTO.getUnitOwner());
                }

                // adding for search list
                if (clazzDTO.getTutorAllocated() != null) {
                    userIds.add(clazzDTO.getTutorAllocated());
                }
            });
            return classes;
        });

    }

    private CompletableFuture<Map<String, UserDTO>> initUserFuture(Set<String> userIds) {

        if (CollectionUtils.isEmpty(userIds)) {
            throw new TpspException("Empty User ID search list!");
        }

        return CompletableFuture.supplyAsync(() -> {

            // get a complete list of applicantID, unitOwnerId and tutorId here
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
    }

    private CompletableFuture<List<List<Object>>> initExcelDataList(List<Application> apps,
                                                                    CompletableFuture<Map<Integer, ClazzDTO>> clazzFuture,
                                                                    CompletableFuture<Map<String, UserDTO>> userFuture) {

        if (CollectionUtils.isEmpty(apps) || clazzFuture == null || userFuture == null) {
            throw new TpspException("Empty application, class or user data list!");
        }

        return clazzFuture.thenCombine(userFuture, (clazzData, userData) -> {
            List<List<Object>> result = apps.stream().map(x -> {
                List<Object> list = new ArrayList<>();

                UserDTO applicant = userData.get(x.getApplicant());
                list.add(applicant != null ? applicant.getUserName() : TPSPConstants.EMPTY);
                list.add(TpspUtils.toFullName(applicant.getFirstName(), applicant.getLastName()));
                list.add(applicant.getEmail());
                list.add(applicant.getPhoneNumber());

                ClazzDTO clazzDTO = clazzData.get(x.getAppliedClass());
                if (clazzDTO != null) {
                    UnitDTO unitDTO = clazzDTO.getUnitDTO();
                    if (unitDTO != null) {
                        list.add(unitDTO.getUnitCode());
                        list.add(unitDTO.getUnitName());
                        UserDTO unitOwner = userData.get(unitDTO.getUnitOwner());
                        list.add(unitOwner != null
                                ? TpspUtils.toFullName(unitOwner.getFirstName(), unitOwner.getLastName())
                                : TPSPConstants.EMPTY);
                        list.add(unitDTO.getUnitLink());
                    } else {
                        for (int i = 0; i < 4; i++) {
                            list.add(TPSPConstants.EMPTY);
                        }
                    }
                    list.add(clazzDTO.getClassType());
                    UserDTO allocatedTutor = userData.get(clazzDTO.getTutorAllocated());
                    list.add(allocatedTutor != null
                            ? TpspUtils.toFullName(allocatedTutor.getFirstName(), allocatedTutor.getLastName())
                            : TPSPConstants.EMPTY);
                    list.add(clazzDTO.getStudyPeriod() + " " + clazzDTO.getYear());
                    list.add(clazzDTO.getDayOfWeek());
                    list.add(clazzDTO.getStartDate());
                } else {
                    for (int i = 0; i < 9; i++) {
                        list.add(TPSPConstants.EMPTY);
                    }
                }

                list.add(x.getApproved());
                list.add(x.getPa());
                list.add(x.getPreference());
                return list;
            }).collect(Collectors.toList());
            log.info("Successfully combined application data list: {} ", result);
            return result;
        });
    }
}
