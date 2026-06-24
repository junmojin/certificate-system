package com.junmo.certificatesystem.dto.certificate;

import java.time.LocalDateTime;

import com.junmo.certificatesystem.common.enums.CertificateStatus;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CertificateAdminSummaryResponse {

    private final Long id;
    private final String applicantUserId;
    private final String applicantName;
    private final String companyName;
    private final String position;
    private final String purpose;
    private final CertificateStatus status;
    private final String statusLabel;
    private final LocalDateTime appliedAt;
}
