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
import org.apache.poi.ss.usermodel.CellType;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

    @Value("#{'${report.tutor.header}'.split(',')}")
    private List<String> reportTutorHeader;

    @Value("${report.tutor.sheet}")
    private String reportTutorSheet;

    @Value("${report.allocation.sheet}")
    private String allocationSheet;

    @Value("${report.column.approved}")
    private String approvedColLetter;

    @Autowired
    ObjectMapper mapper;

    @Autowired
    private ExcelHelper excelHelper;

    @Autowired
    private ApplicationRepo applicationRepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private TutorPreferenceRepo tutorPreferenceRepo;

    @Autowired
    private ClazzRepo clazzRepo;

    @Autowired
    private UnitRepo unitRepo;

    @Autowired
    private RoleRepo roleRepo;

    @Override
    public ExcelReportDTO loadApplicationFile() {
        log.info("Loading applicant file ...");
        List<Application> apps = applicationRepo.findAllByOrderByApplicationIdDesc();
        return toApplicationExcelDTO(apps, appHeader, appSheet, getExtraAppCells());
    }

    @Override
    public ExcelReportDTO loadConvenorRankingFile() {

        log.info("Loading convenor ranking file ...");
        List<TutorPreference> preferences = tutorPreferenceRepo.findAllByOrderByTutor();
        List<TutorPreferenceDTO> preferenceDTOs = preferences.stream().map(p -> {

            // force lazy loading. change it to eager to avoid troublesome?
            User convenor = p.getConvenor();
            UserDTO convenorDTO = UserDTO.builder().build();
            BeanUtils.copyProperties(convenor, convenorDTO);

            User tutor = p.getTutor();
            UserDTO tutorDTO = UserDTO.builder().build();
            BeanUtils.copyProperties(tutor, tutorDTO);

            return TutorPreferenceDTO.builder().Id(p.getId())
                    .convenor(convenorDTO).tutor(tutorDTO)
                    .rating(p.getRating()).build();
        }).collect(Collectors.toList());
        log.info("Found preferences {}", preferenceDTOs);

        List<List<Object>> excelPayloads = preferenceDTOs.stream().map(x -> {
            List<Object> payloadRow = new ArrayList<>();
            UserDTO convenor = x.getConvenor();
            if (convenor == null) {
                IntStream.range(0, 4).forEach(i -> payloadRow.add(TPSPConstants.EMPTY));
            } else {
                payloadRow.add(convenor.getUserName());
                payloadRow.add(TpspUtils.toFullName(convenor.getFirstName(), convenor.getLastName()));
                payloadRow.add(convenor.getEmail());
                payloadRow.add(convenor.getPhoneNumber());
            }

            UserDTO tutor = x.getTutor();
            if (tutor == null) {
                IntStream.range(0, 14).forEach(i -> payloadRow.add(TPSPConstants.EMPTY));
            } else {
                payloadRow.add(tutor.getUserName());
                payloadRow.add(TpspUtils.toFullName(tutor.getFirstName(), tutor.getLastName()));
                payloadRow.add(tutor.getEmail());
                payloadRow.add(tutor.getPhoneNumber());
                payloadRow.add(x.getRating());
                payloadRow.add(tutor.getSwinburneId());
                payloadRow.add(TpspUtils.concatenateAddress(tutor.getStreet(), tutor.getCity(),
                        tutor.getState(), tutor.getPostalCode()));
                payloadRow.add(tutor.getQualification());
                payloadRow.add(tutor.getLinkedinUrl());
                payloadRow.add(tutor.getCitizenshipStudyStatus());
                payloadRow.add(tutor.getAustralianWorkRights());
                payloadRow.add(tutor.getNumberYearsWorkExperience());
                payloadRow.add(tutor.getPreviousTeachingExperience());
                payloadRow.add(tutor.getPublications());
            }
            return payloadRow;
        }).collect(Collectors.toList());
        log.info("Done preparing convenor ranking rows {}", excelPayloads);
        return toExcelDTO(excelPayloads, convenorRateHeader, convenorRateSheet, null);
    }

    @Override
    public ExcelReportDTO loadTutorListFile() {

        log.info("Loading tutor list file ...");
        Optional<Role> applicantRole = roleRepo.findByName(TPSPConstants.APPLICANT);
        if (!applicantRole.isPresent()) {
            throw new TpspException(MessageFormat.format("Cannot find the role {0}",
                    TPSPConstants.APPLICANT));
        }

        List<UserRole> applicantRoles = applicantRole.get().getUserRoles();
        if (CollectionUtils.isEmpty(applicantRoles)) {
            // return empty file
            return toExcelDTO(null, reportTutorHeader, reportTutorSheet, null);
        }

        Set<String> applicantIds = applicantRoles.stream()
                .map(a -> a.getUserRoleKey().getUserId()).collect(Collectors.toSet());

        // find applicant info
        CompletableFuture<List<UserDTO>> applicantFuture = CompletableFuture.supplyAsync(() -> {
            List<UserDTO> applicants = userRepo.findByIdIn(applicantIds).stream().map(a -> {
                UserDTO dto = UserDTO.builder().build();
                BeanUtils.copyProperties(a, dto);
                return dto;
            }).collect(Collectors.toList());
            log.info("Found the list of applicants {}", applicantIds);
            return applicants;
        });

        // find total applications of applicant
        CompletableFuture<Map<String, Long>> totalAppFuture = CompletableFuture.supplyAsync(() -> {
            List<Application> apps = applicationRepo.findByApplicantIn(applicantIds);
            return apps.stream().collect(Collectors.groupingBy(Application::getApplicant,
                    Collectors.counting()));
        });

        CompletableFuture<List<List<Object>>> excelPayloadFuture = applicantFuture
                .thenCombine(totalAppFuture, (applicantData, totalAppData) -> {
                    List<List<Object>> excelPayloads = applicantData.stream().map(a -> {
                        List<Object> list = new ArrayList<>();
                        list.add(a.getUserName());
                        list.add(TpspUtils.toFullName(a.getFirstName(), a.getLastName()));
                        list.add(a.getEmail());
                        list.add(a.getPhoneNumber());
                        list.add(a.getSwinburneId());
                        list.add(TpspUtils.concatenateAddress(a.getStreet(), a.getCity(),
                                a.getState(), a.getPostalCode()));
                        list.add(a.getQualification());
                        list.add(a.getLinkedinUrl());
                        list.add(a.getCitizenshipStudyStatus());
                        list.add(a.getAustralianWorkRights());
                        list.add(a.getNumberYearsWorkExperience());
                        list.add(a.getPreviousTeachingExperience());
                        list.add(a.getPublications());
                        Long totalApps = totalAppData.get(a.getId());
                        list.add(totalApps == null ? 0 : totalApps);
                        return list;
                    }).collect(Collectors.toList());
                    log.info("Done preparing tutor list rows {}", excelPayloads);
                    return excelPayloads;
                });

        try {
            return toExcelDTO(excelPayloadFuture.get(), reportTutorHeader, reportTutorSheet, null);
        } catch (ExecutionException | InterruptedException e) {
            log.error("Error combining tutor list data {}", e);
            throw new TpspException("Error encountered while downloading tutor list!");
        }
    }

    @Override
    public ExcelReportDTO loadAllocationFile() {
        log.info("Loading allocation file ...");
        List<Application> apps = applicationRepo.findByApproved(TPSPConstants.APPROVED);
        return toApplicationExcelDTO(apps, appHeader, allocationSheet, null);
    }

    private ExcelReportDTO toApplicationExcelDTO(List<Application> apps, List<String> headerCols,
                                                 String sheetName, List<List<ExtraCellDTO>> extraCells) {

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

        try {
            return toExcelDTO(combinedFuture.get(), headerCols, sheetName, extraCells);
        } catch (ExecutionException | InterruptedException e) {
            log.error("Error printing excel data {}", e);
            throw new TpspException("Failed to download the application file!");
        }
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
                        IntStream.range(0, 4).forEach(i -> list.add(TPSPConstants.EMPTY));
                    }
                    list.add(clazzDTO.getClassType());
                    UserDTO allocatedTutor = userData.get(clazzDTO.getTutorAllocated());
                    list.add(allocatedTutor != null
                            ? TpspUtils.toFullName(allocatedTutor.getFirstName(), allocatedTutor.getLastName())
                            : TPSPConstants.EMPTY);
                    list.add(clazzDTO.getStudyPeriod() + TPSPConstants.HYPHEN + clazzDTO.getYear());
                    list.add(clazzDTO.getDayOfWeek());
                    list.add(clazzDTO.getStartDate());
                } else {
                    IntStream.range(0, 9).forEach(i -> list.add(TPSPConstants.EMPTY));
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

    private ExcelReportDTO toExcelDTO(List<List<Object>> rows, List<String> headerCols, String sheetName,
                                      List<List<ExtraCellDTO>> extraCells) {
        ByteArrayInputStream byteData = excelHelper.toExcelReport(rows, headerCols, sheetName, extraCells);
        log.info("Done creating excel data ...");
        return new ExcelReportDTO(sheetName + TPSPConstants.FILE_EXTENSION, byteData);
    }

    private List<List<ExtraCellDTO>> getExtraAppCells() {
        String formula = MessageFormat.format(TPSPConstants.COUNT_IF_FORMULA,
                approvedColLetter, TPSPConstants.YES);

        ExtraCellDTO dto = ExtraCellDTO.builder()
                .label("Total Approved").value(formula)
                .type(CellType.FORMULA).requiredColIndex(true)
                .criterion(TPSPConstants.YES).build();
        log.info("Prepared extra rows {}", dto);

        return new ArrayList<List<ExtraCellDTO>>() {{
            add(new ArrayList<ExtraCellDTO>() {{
                add(dto);
            }});
        }};
    }
}
