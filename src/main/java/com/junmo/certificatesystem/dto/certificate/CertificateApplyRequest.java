package com.junmo.certificatesystem.dto.certificate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CertificateApplyRequest {

    @NotBlank(message = "회사명을 입력해 주세요.")
    @Size(max = 100, message = "회사명은 100자 이내로 입력해 주세요.")
    private String companyName;

    @NotBlank(message = "직위를 입력해 주세요.")
    @Size(max = 50, message = "직위는 50자 이내로 입력해 주세요.")
    private String position;

    @NotBlank(message = "발급 목적을 입력해 주세요.")
    @Size(max = 200, message = "발급 목적은 200자 이내로 입력해 주세요.")
    private String purpose;
}
