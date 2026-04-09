package com.camara.util;

import com.camara.data.PdfService;
import com.camara.model.Law;

/**
 * Exemplo prático de uso dos novos recursos de validação e tratamento de encoding.
 * Demonstra os 4 novos métodos públicos e como usá-los.
 */
public class PdfValidationExample {
    
    /**
     * Exemplo 1: Validação básica antes de processar
     */
    public static void exemploValidacaoBasica(String pdfPath) {
        System.out.println("=== EXEMPLO 1: Validação Básica ===\n");
        
        PdfService pdf = new PdfService();
        PdfService.PdfValidationResult validation = pdf.validatePdfFormat(pdfPath);
        
        System.out.println("Status de validação:");
        System.out.println(validation);
        System.out.println();
        
        if (validation.isValid) {
            System.out.println("✓ PDF válido! Processando...");
            procesarLeis(pdf, pdfPath);
        } else {
            System.out.println("✗ PDF inválido: " + validation.errorMessage);
        }
    }
    
    /**
     * Exemplo 2: Verificar metadados do PDF
     */
    public static void exemploMetadados(String pdfPath) {
        System.out.println("\n=== EXEMPLO 2: Metadados do PDF ===\n");
        
        PdfService pdf = new PdfService();
        PdfService.PdfMetadata metadata = pdf.getPdfMetadata(pdfPath);
        
        System.out.println("Informações técnicas:");
        System.out.println(metadata);
        System.out.println();
        
        System.out.println("Detalhes:");
        System.out.println("- Arquivo: " + metadata.fileName);
        System.out.println("- Tamanho: " + (metadata.fileSize / 1024) + " KB");
        System.out.println("- Páginas: " + metadata.totalPages);
        System.out.println("- Produtor: " + (metadata.producer != null ? metadata.producer : "Desconhecido"));
        System.out.println("- Encriptado: " + (metadata.isEncrypted ? "Sim" : "Não"));
    }
    
    /**
     * Exemplo 3: Detectar e recuperar de problemas de encoding
     */
    public static void exemploTratamentoEncoding(String pdfPath) {
        System.out.println("\n=== EXEMPLO 3: Tratamento de Encoding ===\n");
        
        PdfService pdf = new PdfService();
        
        // Extrair texto bruto
        String textoBruto = pdf.extractRawText(pdfPath);
        System.out.println("Texto bruto extraído: " + textoBruto.length() + " caracteres");
        
        // Validar
        PdfService.PdfValidationResult validation = pdf.validatePdfFormat(pdfPath);
        
        if (validation.hasEncodingIssues) {
            System.out.println("⚠ Detectados problemas de encoding!");
            System.out.println("  Taxa de caracteres suspeitos: detectada");
            
            // Sanitizar
            String textoLimpo = pdf.sanitizeText(textoBruto);
            System.out.println("  Após sanitização: " + textoLimpo.length() + " caracteres");
            System.out.println("  Caracteres removidos: " + (textoBruto.length() - textoLimpo.length()));
        } else {
            System.out.println("✓ Nenhum problema de encoding detectado");
        }
    }
    
    /**
     * Exemplo 4: Análise de qualidade completa
     */
    public static void exemploAnaliseQualidade(String pdfPath) {
        System.out.println("\n=== EXEMPLO 4: Análise de Qualidade ===\n");
        
        PdfService pdf = new PdfService();
        PdfService.PdfValidationResult result = pdf.validatePdfFormat(pdfPath);
        
        // Score de qualidade
        double qualidade = result.estimatedQuality * 100;
        String statusQualidade = qualidade >= 80 ? "✓ Excelente" : 
                                  qualidade >= 60 ? "↔ Boa" :
                                  qualidade >= 40 ? "⚠ Aceitável" : "✗ Ruim";
        
        System.out.println("Qualidade: " + String.format("%.0f%%", qualidade) + " " + statusQualidade);
        System.out.println();
        
        System.out.println("Análise detalhada:");
        System.out.printf("  Páginas: %d%n", result.totalPages);
        System.out.printf("  Conteúdo: %d caracteres%n", result.textLength);
        System.out.printf("  Tamanho: %d bytes (%.1f KB)%n", result.fileSize, result.fileSize / 1024.0);
        System.out.printf("  Encoding: %s%n", result.hasEncodingIssues ? "✗ Problema detectado" : "✓ OK");
        System.out.printf("  Seções: %s%n", result.hasSections ? "✓ Encontradas" : "✗ Não encontradas");
        System.out.printf("  Encriptado: %s%n", result.isEncrypted ? "Sim" : "Não");
        System.out.println();
        
        if (result.isValid) {
            System.out.println("→ Resultado: PDF pronto para processamento");
        } else {
            System.out.println("→ Resultado: Não recomendado processar este PDF");
        }
    }
    
    /**
     * Exemplo 5: Processar com diagnostico
     */
    public static void exemploDiagnostico(String pdfPath) {
        System.out.println("\n=== EXEMPLO 5: Diagnóstico Completo ===\n");
        
        PdfService pdf = new PdfService();
        
        // Coletar informações
        PdfService.PdfValidationResult validation = pdf.validatePdfFormat(pdfPath);
        PdfService.PdfMetadata metadata = pdf.getPdfMetadata(pdfPath);
        
        System.out.println("📋 DIAGNÓSTICO COMPLETO");
        System.out.println("─".repeat(50));
        
        System.out.println("\n1. Arquivo:");
        System.out.println("   Nome: " + metadata.fileName);
        System.out.println("   Caminho: " + metadata.filePath);
        System.out.println("   Tamanho: " + (metadata.fileSize / 1024) + " KB");
        
        System.out.println("\n2. Conteúdo do PDF:");
        System.out.println("   Páginas: " + validation.totalPages);
        System.out.println("   Caracteres: " + validation.textLength);
        System.out.println("   Encriptado: " + (validation.isEncrypted ? "Sim" : "Não"));
        
        System.out.println("\n3. Qualidade:");
        System.out.println("   Score: " + String.format("%.0f%%", validation.estimatedQuality * 100));
        System.out.println("   Encoding OK: " + (validation.hasEncodingIssues ? "Não" : "Sim"));
        System.out.println("   Seções: " + (validation.hasSections ? "Sim" : "Não"));
        
        System.out.println("\n4. Metadados:");
        System.out.println("   Produtor: " + (metadata.producer != null ? metadata.producer : "Desconhecido"));
        System.out.println("   Criador: " + (metadata.creator != null ? metadata.creator : "Desconhecido"));
        System.out.println("   Título: " + (metadata.title != null ? metadata.title : "-"));
        
        System.out.println("\n5. Recomendação:");
        if (validation.isValid) {
            System.out.println("   ✓ PDF recomendado para processamento");
        } else {
            System.out.println("   ✗ PDF não recomendado para processamento");
            if (!validation.errorMessage.isEmpty()) {
                System.out.println("   Motivo: " + validation.errorMessage);
            }
        }
        
        System.out.println("\n─".repeat(50));
        
        // Tentar processar de qualquer forma
        System.out.println("\nTentando extrair leis...");
        try {
            int count = procesarLeis(pdf, pdfPath);
            System.out.println("✓ Extraídas " + count + " leis com sucesso");
        } catch (Exception e) {
            System.out.println("✗ Erro ao extrair leis: " + e.getMessage());
        }
    }
    
    /**
     * Método auxiliar para processar e contar leis
     */
    private static int procesarLeis(PdfService pdf, String pdfPath) {
        java.util.List<Law> laws = pdf.extractLawsFromPdf(pdfPath);
        
        if (laws.isEmpty()) {
            System.out.println("Nenhuma lei encontrada");
            return 0;
        }
        
        System.out.println("Leis encontradas:");
        for (Law law : laws) {
            System.out.printf("  - %s: %s (Seção: %s)%n", 
                law.getNumber(), law.getTitle(), law.getSection());
        }
        
        return laws.size();
    }
    
    /**
     * Main: Executar todos os exemplos
     */
    public static void main(String[] args) {
        String pdfPath = "pauta.pdf";
        
        try {
            // Executar exemplos
            exemploValidacaoBasica(pdfPath);
            exemploMetadados(pdfPath);
            exemploTratamentoEncoding(pdfPath);
            exemploAnaliseQualidade(pdfPath);
            exemploDiagnostico(pdfPath);
            
            System.out.println("\n✓ Todos os exemplos executados com sucesso!");
            
        } catch (Exception e) {
            System.out.println("\n✗ Erro ao executar exemplos:");
            e.printStackTrace();
        }
    }
}
