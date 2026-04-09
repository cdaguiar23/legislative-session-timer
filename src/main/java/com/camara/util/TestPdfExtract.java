package com.camara.util;

import com.camara.data.PdfService;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Utilidade de teste para extrair e salvar texto de um PDF com logging detalhado.
 * Refatorado para usar PdfService.extractRawText() eliminando código duplicado.
 */
public class TestPdfExtract {
    public static void main(String[] args) {
        java.io.File pdfFile = new java.io.File("pauta.pdf");
        System.out.println("Processing pauta.pdf exists? " + pdfFile.exists());
        
        try {
            PdfService pdfService = new PdfService();
            String text = pdfService.extractRawText("pauta.pdf");
            
            if (!text.isEmpty()) {
                Files.write(Paths.get("pdf_text_dump.txt"), text.getBytes("UTF-8"));
                System.out.println("Extracted text len = " + text.length());
            } else {
                System.out.println("Failed to extract text");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
