package com.university.qpg.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.stereotype.Service;

import com.university.qpg.model.Question;
import com.university.qpg.model.Subject;

@Service
public class ExportService {

    private static final float MARGIN = 50f;
    private static final float LEADING = 16f;
    private static final float FONT_SIZE = 12f;

    public byte[] exportPdf(Subject subject, List<Question> questions) throws IOException {
        try (PDDocument doc = new PDDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);
            PDRectangle box = page.getMediaBox();
            float y = box.getUpperRightY() - MARGIN;

            PDPageContentStream cs = new PDPageContentStream(doc, page);
            y = writeLine(cs, "Question Paper", box, y, 20, new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 16);
            y = writeLine(cs, "Subject: " + safe(subject.getSubjectName()), box, y, LEADING, new PDType1Font(Standard14Fonts.FontName.HELVETICA), FONT_SIZE);
            y = writeLine(cs, "Date: " + LocalDate.now(), box, y, LEADING, new PDType1Font(Standard14Fonts.FontName.HELVETICA), FONT_SIZE);
            y = writeLine(cs, "", box, y, LEADING, new PDType1Font(Standard14Fonts.FontName.HELVETICA), FONT_SIZE);

            int number = 1;
            for (Question q : questions) {
                String line = number + ". [" + q.getMarks() + " marks] " + safe(q.getQuestionText());
                y = writeParagraph(cs, line, box, y, new PDType1Font(Standard14Fonts.FontName.HELVETICA), FONT_SIZE);
                number++;
                if (y < MARGIN + LEADING) {
                    cs.close();
                    page = new PDPage(PDRectangle.A4);
                    doc.addPage(page);
                    box = page.getMediaBox();
                    y = box.getUpperRightY() - MARGIN;
                    cs = new PDPageContentStream(doc, page);
                }
            }
            cs.close();

            doc.save(out);
            return out.toByteArray();
        }
    }

    public byte[] exportDocx(Subject subject, List<Question> questions) throws IOException {
        try (XWPFDocument document = new XWPFDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            XWPFParagraph title = document.createParagraph();
            title.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun titleRun = title.createRun();
            titleRun.setBold(true);
            titleRun.setFontSize(18);
            titleRun.setText("Question Paper");

            XWPFParagraph meta = document.createParagraph();
            XWPFRun metaRun = meta.createRun();
            metaRun.setFontSize(12);
            metaRun.setText("Subject: " + safe(subject.getSubjectName()));
            metaRun.addBreak();
            metaRun.setText("Date: " + LocalDate.now());

            int number = 1;
            for (Question q : questions) {
                XWPFParagraph p = document.createParagraph();
                p.setSpacingAfter(200);
                XWPFRun r = p.createRun();
                r.setFontSize(12);
                r.setText(number + ". " + safe(q.getQuestionText()));
                r.addBreak();
                r.setText("Marks: " + q.getMarks());
                number++;
            }

            document.write(out);
            return out.toByteArray();
        }
    }

    private float writeLine(PDPageContentStream cs, String text, PDRectangle box, float y, float leading,
                            PDFont font, float fontSize) throws IOException {
        if (y <= MARGIN) {
            return y;
        }
        cs.beginText();
        cs.setFont(font, fontSize);
        cs.newLineAtOffset(MARGIN, y);
        cs.showText(text);
        cs.endText();
        return y - leading;
    }

    private float writeParagraph(PDPageContentStream cs, String text, PDRectangle box, float y,
                                 PDFont font, float fontSize) throws IOException {
        float maxWidth = box.getWidth() - 2 * MARGIN;
        String[] words = text.split(" ");
        StringBuilder line = new StringBuilder();
        for (String w : words) {
            String candidate = line.isEmpty() ? w : line + " " + w;
            float width = font.getStringWidth(candidate) / 1000 * fontSize;
            if (width > maxWidth) {
                y = writeLine(cs, line.toString(), box, y, LEADING, font, fontSize);
                line = new StringBuilder(w);
            } else {
                line = new StringBuilder(candidate);
            }
        }
        if (!line.isEmpty()) {
            y = writeLine(cs, line.toString(), box, y, LEADING, font, fontSize);
        }
        return y - (LEADING / 2);
    }

    private String safe(String v) {
        return v == null ? "" : v;
    }
}
