package com.junmo.certificatesystem.dto.certificate;

import com.junmo.certificatesystem.common.enums.CertificateStatus;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CertificateSummaryResponse {

    private final Long id;
    private final String companyName;
    private final String position;
    private final CertificateStatus status;
    private final String statusLabel;
}
