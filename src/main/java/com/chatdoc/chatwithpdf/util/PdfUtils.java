package com.chatdoc.chatwithpdf.util;

import com.chatdoc.chatwithpdf.model.PageText;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PdfUtils {

    public static List<PageText> extractText(MultipartFile file) throws IOException {
        List<PageText> pages = new ArrayList<>();

        try (PDDocument document = PDDocument.load(file.getInputStream())) {
            PDFTextStripper stripper = new PDFTextStripper();
            int totalPages = document.getNumberOfPages();

            for (int i = 1; i <= totalPages; i++) {
                stripper.setStartPage(i);
                stripper.setEndPage(i);
                String pageContent = stripper.getText(document);

                pages.add(PageText.builder().pageNumber(i).text(pageContent.trim()).build());
            }
            return pages;
        }
    }
}
