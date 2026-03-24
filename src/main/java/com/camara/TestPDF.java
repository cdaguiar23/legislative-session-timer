package com.camara;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import java.io.File;

public class TestPDF {
    public static void main(String[] args) throws Exception {
        File file = new File("pauta.pdf");
        PDDocument document = PDDocument.load(file);
        PDFTextStripper stripper = new PDFTextStripper();
        String text = stripper.getText(document);
        document.close();
        
        java.nio.file.Files.write(java.nio.file.Paths.get("pdf_output_utf8_java.txt"), text.getBytes("UTF-8"));
        System.out.println("Done, text extracted.");
    }
}
