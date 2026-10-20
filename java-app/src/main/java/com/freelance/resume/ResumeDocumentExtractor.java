package com.freelance.resume;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Locale;

@Component
public class ResumeDocumentExtractor {

    public String extract(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Please upload a non-empty resume file");
        }

        String filename = file.getOriginalFilename() == null
            ? ""
            : file.getOriginalFilename().toLowerCase(Locale.ROOT);

        try {
            if (filename.endsWith(".pdf")) {
                return extractPdf(file);
            }
            if (filename.endsWith(".docx")) {
                return extractDocx(file);
            }
            if (filename.endsWith(".doc")) {
                return extractDoc(file);
            }
        } catch (IOException ex) {
            throw new IllegalArgumentException("The resume file could not be read. Please upload a valid document.");
        }

        throw new IllegalArgumentException("Unsupported resume format. Upload a PDF, DOC, or DOCX file.");
    }

    private String extractPdf(MultipartFile file) throws IOException {
        try (var document = Loader.loadPDF(file.getBytes())) {
            return new PDFTextStripper().getText(document);
        }
    }

    private String extractDocx(MultipartFile file) throws IOException {
        try (var document = new XWPFDocument(file.getInputStream());
             var extractor = new XWPFWordExtractor(document)) {
            return extractor.getText();
        }
    }

    private String extractDoc(MultipartFile file) throws IOException {
        try (var document = new HWPFDocument(file.getInputStream());
             var extractor = new WordExtractor(document)) {
            return extractor.getText();
        }
    }
}
