package com.junmo.certificatesystem.service.certificate;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.springframework.stereotype.Service;

import com.junmo.certificatesystem.dto.certificate.CertificateViewData;

/**
 * PDFBox로 경력증명서 서식(PDF)을 생성하는 클래스.
 * 텍스트 기반 데이터({@link CertificateViewData})를 받아 경력증명서 레이아웃을 PDF로 변환한다.
 */
@Service
public class CareerCertificatePdfService {

    private static final float PAGE_WIDTH = 595f;
    private static final float PAGE_HEIGHT = 842f;
    private static final float MARGIN = 48f;
    private static final float TABLE_LEFT = 56f;
    private static final float TABLE_WIDTH = PAGE_WIDTH - 112f;
    private static final float COL_LABEL = 72f;
    private static final float COL_VALUE = (TABLE_WIDTH - COL_LABEL * 2) / 2f;
    private static final DateTimeFormatter KOREAN_DATE =
            DateTimeFormatter.ofPattern("yyyy년 MM월 dd일");

    public byte[] generate(CertificateViewData data) throws IOException {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {

            PDPage page = new PDPage();
            document.addPage(page);
            PDType0Font font = loadKoreanFont(document);

            try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                drawBorder(content);

                float y = PAGE_HEIGHT - 62f;
                writeAt(content, font, 10, TABLE_LEFT, y,
                        "문서 번호: " + data.getDocumentNo());
                y -= 36f;

                y = drawCenteredText(content, font, 22, y, "경 력 증 명 서");
                y -= 28f;

                y = drawQuadRow(content, font, y,
                        "성명", data.getName(),
                        "생년월일", data.getBirthDateDisplay());
                y = drawQuadRow(content, font, y,
                        "근무부서", data.getDepartment(),
                        "직급", data.getPosition());
                y = drawQuadRow(content, font, y,
                        "근무시작일", data.getFormattedWorkStartDate(),
                        "근무종료일", data.getWorkEndDateDisplay());
                y = drawQuadRow(content, font, y,
                        "근무기간", data.getWorkPeriod(),
                        "용도", data.getPurpose());
                y = drawFullRow(content, font, y, "담당업무", data.getAssignedTask());
                y = drawFullRow(content, font, y, "비고", data.getRemarks());
                y -= 48f;

                drawCenteredText(content, font, 12, y, CertificateViewData.FOOTER_STATEMENT);
                y -= 36f;
                drawCenteredText(content, font, 11, y, formatDate(data.getIssueDate()));
                y -= 48f;

                drawCenteredText(content, font, 14, y, data.getIssuerName());
            }

            document.save(output);
            return output.toByteArray();
        }
    }

    private PDType0Font loadKoreanFont(PDDocument document) throws IOException {
        List<Path> candidates = List.of(
                Path.of("C:/Windows/Fonts/malgun.ttf"),
                Path.of("C:/Windows/Fonts/malgunsl.ttf")
        );

        for (Path path : candidates) {
            if (Files.exists(path)) {
                return PDType0Font.load(document, path.toFile());
            }
        }

        throw new IllegalStateException(
                "한글 PDF 생성을 위해 Windows 맑은 고딕(malgun.ttf) 폰트가 필요합니다.");
    }

    private void drawBorder(PDPageContentStream content) throws IOException {
        content.setLineWidth(1f);
        content.addRect(MARGIN, MARGIN, PAGE_WIDTH - MARGIN * 2, PAGE_HEIGHT - MARGIN * 2);
        content.stroke();
    }

    private float drawQuadRow(
            PDPageContentStream content,
            PDType0Font font,
            float y,
            String label1,
            String value1,
            String label2,
            String value2) throws IOException {
        float rowHeight = 26f;
        float bottom = y - rowHeight;

        strokeRect(content, TABLE_LEFT, bottom, TABLE_WIDTH, rowHeight);
        strokeVLine(content, TABLE_LEFT + COL_LABEL, bottom, y);
        strokeVLine(content, TABLE_LEFT + COL_LABEL + COL_VALUE, bottom, y);
        strokeVLine(content, TABLE_LEFT + COL_LABEL * 2 + COL_VALUE, bottom, y);

        writeAt(content, font, 10, TABLE_LEFT + 8f, bottom + 9f, label1);
        writeAt(content, font, 10, TABLE_LEFT + COL_LABEL + 8f, bottom + 9f, safe(value1));
        writeAt(content, font, 10, TABLE_LEFT + COL_LABEL + COL_VALUE + 8f, bottom + 9f, label2);
        writeAt(content, font, 10, TABLE_LEFT + COL_LABEL * 2 + COL_VALUE + 8f, bottom + 9f, safe(value2));

        return bottom;
    }

    private float drawFullRow(
            PDPageContentStream content,
            PDType0Font font,
            float y,
            String label,
            String value) throws IOException {
        float rowHeight = 26f;
        float bottom = y - rowHeight;

        strokeRect(content, TABLE_LEFT, bottom, TABLE_WIDTH, rowHeight);
        strokeVLine(content, TABLE_LEFT + COL_LABEL, bottom, y);

        writeAt(content, font, 10, TABLE_LEFT + 8f, bottom + 9f, label);
        writeAt(content, font, 10, TABLE_LEFT + COL_LABEL + 10f, bottom + 9f, safe(value));

        return bottom;
    }

    private void strokeRect(PDPageContentStream content, float x, float y, float w, float h)
            throws IOException {
        content.setLineWidth(0.6f);
        content.addRect(x, y, w, h);
        content.stroke();
    }

    private void strokeVLine(PDPageContentStream content, float x, float bottom, float top)
            throws IOException {
        content.moveTo(x, bottom);
        content.lineTo(x, top);
        content.stroke();
    }

    private float drawCenteredText(
            PDPageContentStream content,
            PDType0Font font,
            int fontSize,
            float y,
            String text) throws IOException {
        float textWidth = font.getStringWidth(text) / 1000f * fontSize;
        float x = (PAGE_WIDTH - textWidth) / 2f;
        writeAt(content, font, fontSize, x, y, text);
        return y - (fontSize + 10f);
    }

    private void writeAt(
            PDPageContentStream content,
            PDType0Font font,
            int fontSize,
            float x,
            float y,
            String text) throws IOException {
        content.beginText();
        content.setFont(font, fontSize);
        content.newLineAtOffset(x, y);
        content.showText(text);
        content.endText();
    }

    private String formatDate(java.time.LocalDate date) {
        return date.format(KOREAN_DATE);
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? " " : value;
    }
}
