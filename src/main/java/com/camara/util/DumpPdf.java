package com.camara.util;

import com.camara.data.PdfService;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Utilidade de teste para extrair texto bruto de um PDF.
 * Consolidado a partir de código duplicado originalmente em TestPDF.java e TestPdfExtract.java.
 */
public class DumpPdf {
    public static void main(String[] args) throws Exception {
        PdfService pdfService = new PdfService();
        String text = pdfService.extractRawText("pauta.pdf");
        
        if (!text.isEmpty()) {
            Files.write(Paths.get("pdf_text_dump.txt"), text.getBytes("UTF-8"));
            System.out.println("Text extracted successfully. Length: " + text.length() + " characters");
        } else {
            System.out.println("Failed to extract text from PDF");
        }
    }
}
