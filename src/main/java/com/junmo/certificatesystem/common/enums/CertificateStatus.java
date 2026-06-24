package com.junmo.certificatesystem.common.enums;

public enum CertificateStatus {
    PENDING("승인 대기"),
    APPROVED("발급 승인"),
    REJECTED("발급 거부"),
    ISSUED("발급 완료");

    private final String label;

    CertificateStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
