package com.junmo.certificatesystem.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import com.junmo.certificatesystem.config.properties.SmartCertProperties;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.ServletContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableConfigurationProperties(SmartCertProperties.class)
@RequiredArgsConstructor
public class SmartCertConfig {

    private final SmartCertProperties smartCertProperties;
    private final ServletContext servletContext;

    @PostConstruct
    void init() throws IOException {
        Path pdfDir = Path.of(smartCertProperties.getPdfDir());
        Files.createDirectories(pdfDir);
        Files.createDirectories(Path.of(smartCertProperties.getBarcodeVoiceyeDir()));

        servletContext.setAttribute("sgaNpPdfDir", smartCertProperties.getPdfDir());
        servletContext.setAttribute("sgaNpTrustCertificateXml", smartCertProperties.getTrustCertificateXml());
        servletContext.setAttribute("sgaNpBarcodeVoiceyeDir", smartCertProperties.getBarcodeVoiceyeDir());
        servletContext.setAttribute("sgaNpBarcodeDataPath", smartCertProperties.getBarcodeDataPath());
        servletContext.setAttribute("html5viewResourceFolder", smartCertProperties.getViewerResourceFolder());

        log.info("SmartCert PDF dir: {}", pdfDir.toAbsolutePath());
    }
}
