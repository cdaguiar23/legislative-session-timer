package com.camara.util;

import com.camara.data.PdfService;
import com.camara.model.Law;
import java.util.List;

/**
 * Utilidade de teste para executar a extração de leis de um PDF e exibir resultados.
 */
public class TestPdfRun {
    public static void main(String[] args) {
        PdfService svc = new PdfService();
        List<Law> laws = svc.extractLawsFromPdf("pauta.pdf");
        System.out.println("Processing pauta.pdf");
        System.out.println("Found " + laws.size() + " laws.");
        for (Law l : laws) {
            System.out.println(l.getNumber() + " | " + l.getTitle() + " | " + l.getAuthor() + " | " + l.getSection());
        }
    }
}
