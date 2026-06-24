package com.junmo.certificatesystem.dto.board;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BoardWriteRequest {

    @NotBlank(message = "제목을 입력해 주세요.")
    @Size(max = 200, message = "제목은 200자 이내로 입력해 주세요.")
    private String title;

    @NotBlank(message = "본문을 입력해 주세요.")
    @Size(max = 5000, message = "본문은 5000자 이내로 입력해 주세요.")
    private String content;
}
