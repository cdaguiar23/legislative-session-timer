package com.camara;

import com.camara.data.PdfService;
import com.camara.model.Law;
import java.util.List;

public class TestPdfExtract {
    public static void main(String[] args) {
        PdfService service = new PdfService();
        List<Law> laws = service.extractLawsFromPdf("pauta.pdf");
        
        System.out.println("Extracted Laws: " + laws.size());
        for (Law law : laws) {
            System.out.println("===============");
            System.out.println("NUMBER: " + law.getNumber());
            System.out.println("TITLE: " + law.getTitle());
            System.out.println("SUMMARY: " + law.getSummary());
        }
    }
}
