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
 * 검정 테두리 하단 아래는 바코드 삽입용 여백(테두리 밖)으로 둔다.
 */
@Service
public class CareerCertificatePdfService {

    private static final float PAGE_WIDTH = 595f;
    private static final float PAGE_HEIGHT = 842f;

    /** 검정 테두리 — 좌·우·상 여백 */
    private static final float BORDER_SIDE = 48f;
    private static final float BORDER_TOP = 52f;
    /** 테두리 하단 Y (페이지 하단 기준, 이 아래는 바코드 영역) */
    private static final float BORDER_BOTTOM_Y = 108f;

    /** 테두리 안쪽 패딩 */
    private static final float INNER_PAD = 16f;

    private static final float TABLE_LEFT = BORDER_SIDE + INNER_PAD;
    private static final float TABLE_WIDTH = PAGE_WIDTH - TABLE_LEFT * 2;
    private static final float COL_LABEL = 66f;
    private static final float COL_VALUE = (TABLE_WIDTH - COL_LABEL * 2) / 2f;

    private static final int FONT_DOC_NO = 9;
    private static final int FONT_TITLE = 20;
    private static final int FONT_BODY = 9;
    private static final int FONT_STATEMENT = 11;
    private static final int FONT_DATE = 10;
    private static final int FONT_ISSUER = 12;

    private static final float ROW_HEIGHT = 22f;
    private static final float GAP_AFTER_DOC_NO = 28f;
    private static final float GAP_AFTER_TITLE = 22f;

    /** 상단 — 문서번호·제목 */
    private static final float TOP_INSET = 24f;

    /** 하단 — 증명 문구·날짜·회사명 (테두리 하단 기준) */
    private static final float FOOTER_BOTTOM_PAD = 32f;
    private static final float FOOTER_LINE_GAP = 14f;

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

                float innerTop = borderTopY() - INNER_PAD;
                float innerBottom = borderBottomY() + INNER_PAD;

                // 상단: 문서번호 · 제목
                float y = innerTop - TOP_INSET;
                writeAt(content, font, FONT_DOC_NO, TABLE_LEFT, y,
                        "문서 번호: " + data.getDocumentNo());
                y -= GAP_AFTER_DOC_NO;
                drawCenteredText(content, font, FONT_TITLE, y, "경 력 증 명 서");

                // 표: 이전(세로 중앙) 배치 기준 위치 유지
                y = tableStartY(innerTop, innerBottom);
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

                // 하단: 증명 문구 · 날짜 · 회사명
                drawFooter(content, font, innerBottom, data);
            }

            document.save(output);
            return output.toByteArray();
        }
    }

    private float borderTopY() {
        return PAGE_HEIGHT - BORDER_TOP;
    }

    private float borderBottomY() {
        return BORDER_BOTTOM_Y;
    }

    /** 표 시작 Y — 본문 전체 세로 중앙 정렬 시 표 상단 위치(기존과 동일) */
    private float tableStartY(float innerTop, float innerBottom) {
        float blockStartY = innerTop - (innerTop - innerBottom - measureBlockHeight()) / 2f;
        float y = blockStartY;
        y -= GAP_AFTER_DOC_NO;
        y -= FONT_TITLE + 8f;
        y -= GAP_AFTER_TITLE;
        return y;
    }

    private float measureBlockHeight() {
        return FONT_DOC_NO
                + GAP_AFTER_DOC_NO
                + FONT_TITLE + 8f
                + GAP_AFTER_TITLE
                + ROW_HEIGHT * 4
                + 36f
                + FONT_STATEMENT + 8f
                + 28f
                + FONT_DATE + 8f
                + 36f
                + FONT_ISSUER;
    }

    private void drawFooter(
            PDPageContentStream content,
            PDType0Font font,
            float innerBottom,
            CertificateViewData data) throws IOException {
        float y = innerBottom + FOOTER_BOTTOM_PAD;
        drawCenteredTextAt(content, font, FONT_ISSUER, y, data.getIssuerName());
        y += FONT_ISSUER + FOOTER_LINE_GAP;
        drawCenteredTextAt(content, font, FONT_DATE, y, formatDate(data.getIssueDate()));
        y += FONT_DATE + FOOTER_LINE_GAP;
        drawCenteredTextAt(content, font, FONT_STATEMENT, y, CertificateViewData.FOOTER_STATEMENT);
    }

    private void drawCenteredTextAt(
            PDPageContentStream content,
            PDType0Font font,
            int fontSize,
            float y,
            String text) throws IOException {
        float textWidth = font.getStringWidth(text) / 1000f * fontSize;
        float x = (PAGE_WIDTH - textWidth) / 2f;
        writeAt(content, font, fontSize, x, y, text);
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
        float top = borderTopY();
        float bottom = borderBottomY();
        content.setLineWidth(1f);
        content.addRect(BORDER_SIDE, bottom, PAGE_WIDTH - BORDER_SIDE * 2, top - bottom);
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
        float bottom = y - ROW_HEIGHT;

        strokeRect(content, TABLE_LEFT, bottom, TABLE_WIDTH, ROW_HEIGHT);
        strokeVLine(content, TABLE_LEFT + COL_LABEL, bottom, y);
        strokeVLine(content, TABLE_LEFT + COL_LABEL + COL_VALUE, bottom, y);
        strokeVLine(content, TABLE_LEFT + COL_LABEL * 2 + COL_VALUE, bottom, y);

        writeAt(content, font, FONT_BODY, TABLE_LEFT + 6f, bottom + 7f, label1);
        writeAt(content, font, FONT_BODY, TABLE_LEFT + COL_LABEL + 6f, bottom + 7f, safe(value1));
        writeAt(content, font, FONT_BODY, TABLE_LEFT + COL_LABEL + COL_VALUE + 6f, bottom + 7f, label2);
        writeAt(content, font, FONT_BODY, TABLE_LEFT + COL_LABEL * 2 + COL_VALUE + 6f, bottom + 7f, safe(value2));

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
        return y - (fontSize + 8f);
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
