package com.junmo.certificatesystem.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.smart-cert")
public class SmartCertProperties {

    /** sga_np 루트 (TrustCertificate.xml 등) */
    private String baseDir = "C:/app/sga_np";

    /** HTML5 뷰어가 읽을 PDF 폴더 (gs_pdf) */
    private String pdfDir = "C:/app/sga_np/gs_pdf";

    private String trustCertificateXml = "C:/app/sga_np/TrustCertificate.xml";

    /** np_reader.jsp — 바코드 생성 (y/n) */
    private String viewerBarcode = "y";

    /** np_reader.jsp — 보이스아이 바코드 (y/n) */
    private String viewerVoiceye = "y";

    /** 보이스아이 BMP 출력 폴더 */
    private String barcodeVoiceyeDir = "C:/app/sga_np/barcode/voiceye";

    /** 바코드 삽입용 HTML 데이터 파일 */
    private String barcodeDataPath = "C:/app/sga_np/test.html";

    /** np_reader.jsp meta resourceFolder (JS용, /html5view URL 접근 시 빈 문자열) */
    private String viewerResourceFolder = "";
}
