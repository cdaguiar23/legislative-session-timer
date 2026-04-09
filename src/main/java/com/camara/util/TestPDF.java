package com.camara.util;

import com.camara.data.PdfService;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Utilidade de teste para extrair e salvar texto de um PDF em UTF-8.
 * Refatorado para usar PdfService.extractRawText() eliminando código duplicado.
 */
public class TestPDF {
    public static void main(String[] args) throws Exception {
        PdfService pdfService = new PdfService();
        String text = pdfService.extractRawText("pauta.pdf");
        
        if (!text.isEmpty()) {
            Files.write(Paths.get("pdf_output_utf8_java.txt"), text.getBytes("UTF-8"));
            System.out.println("Done, text extracted. Length: " + text.length() + " characters");
        } else {
            System.out.println("Failed to extract text from PDF");
        }
    }
}
