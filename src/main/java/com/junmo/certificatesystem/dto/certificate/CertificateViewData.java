package com.junmo.certificatesystem.dto.certificate;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.junmo.certificatesystem.common.enums.CertificateStatus;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CertificateViewData {

    public static final String FOOTER_STATEMENT = "상기 내용이 사실임을 증명합니다.";

    private final Long certificateId;
    private final String documentNo;
    private final String userId;
    private final String name;
    private final String birthDateDisplay;
    private final String department;
    private final String companyName;
    private final String position;
    private final LocalDate hireDate;
    private final String formattedWorkStartDate;
    private final String workEndDateDisplay;
    private final String workPeriod;
    private final String employmentStatus;
    private final String purpose;
    private final String assignedTask;
    private final String remarks;
    private final CertificateStatus status;
    private final LocalDate issueDate;
    private final LocalDateTime appliedAt;
    private final String issuerName;
}
