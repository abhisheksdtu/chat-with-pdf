package com.chatdoc.chatwithpdf.util;


import com.chatdoc.chatwithpdf.model.PageText;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class PdfUtilsNew {
    public static List<PageText> extractText(InputStream in) throws IOException {
        log.debug("Extracting text from PDF");
        List<PageText> pages = new ArrayList<>();
        try (PDDocument document = PDDocument.load(in)) {
            if (document.isEncrypted()) {
                throw new IOException("Encrypted PDF is not supported");
            }
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            int totalPages = document.getNumberOfPages();
            for (int i = 1; i <= totalPages; i++) {
                stripper.setStartPage(i);
                stripper.setEndPage(i);
                String pageContent = stripper.getText(document);
                pages.add(PageText.builder()
                        .pageNumber(i)
                        .text(pageContent == null ? "" : pageContent.trim())
                        .build());
            }
        }
        return pages;
    }

}
