package com.camara.data;

import com.camara.model.Law;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PdfService {
    private static final Logger LOGGER = Logger.getLogger(PdfService.class.getName());
    
    // Configurable header/footer filters - loaded at startup
    private static final Set<String> DEFAULT_FILTERS = new HashSet<>(Arrays.asList(
        "ESTADO DE SANTA CATARINA",
        "CÂMARA MUNICIPAL",
        "www.camaracanelinha.sc.gov.br",
        "Rua Manoel Francisco",
        "Fone:",
        "Página",
        "TÍTULO AUTORIA RESUMO DESTINO"
    ));
    
    private Set<String> headerFilters = DEFAULT_FILTERS;
    private Set<String> authorPatterns = new HashSet<>(Arrays.asList(
        "PODER EXECUTIVO",
        "MESA DIRETORA",
        "PREFEITO MUNICIPAL"
    ));

    
    /**
     * Extrai texto bruto de um arquivo PDF com suporte a diferentes codificações.
     * @param filePath caminho do arquivo PDF
     * @return texto extraído do PDF, ou string vazia se houver erro
     */
    public String extractRawText(String filePath) {
        try {
            File pdfFile = new File(filePath);
            if (!pdfFile.exists()) {
                LOGGER.warning("Arquivo PDF não encontrado: " + filePath);
                return "";
            }
            
            try (PDDocument document = PDDocument.load(pdfFile)) {
                PDFTextStripper stripper = new PDFTextStripper();
                stripper.setSortByPosition(false);
                String text = stripper.getText(document);
                
                LOGGER.info("Texto extraído com sucesso de: " + filePath + " (tamanho: " + text.length() + " caracteres)");
                return text;
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao extrair texto do PDF: " + filePath, e);
            return "";
        }
    }
    
    /**
     * Extrai leis de um arquivo PDF, processando seções EXPEDIENTE e ORDEM DO DIA.
     * @param filePath caminho do arquivo PDF
     * @return lista de leis extraídas
     */
    public List<Law> extractLawsFromPdf(String filePath) {
        List<Law> laws = new ArrayList<>();
        
        String text = extractRawText(filePath);
        if (text.isEmpty()) {
            LOGGER.warning("Não foi possível extrair texto do PDF: " + filePath);
            return laws;
        }
        
        try {
            // Validar que o PDF contém as seções esperadas
            String normText = text.toUpperCase();
            if (!normText.contains("EXPEDIENTE") && !normText.contains("ORDEM DO DIA")) {
                LOGGER.warning("PDF não contém seções 'EXPEDIENTE' ou 'ORDEM DO DIA': " + filePath);
            }
            
            // Separate by sections
            String[] sectionNames = { "EXPEDIENTE", "ORDEM DO DIA" };
            for (String sectionName : sectionNames) {
                List<Law> sectionLaws = extractFromSection(text, sectionName);
                laws.addAll(sectionLaws);
                LOGGER.info("Seção '" + sectionName + "' processada: " + sectionLaws.size() + " leis extraídas");
            }
            
            LOGGER.info("Total de leis extraídas do PDF: " + laws.size());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao processar PDF: " + filePath, e);
        }

        return laws;
    }

    private List<Law> extractFromSection(String fullText, String sectionName) {
        List<Law> laws = new ArrayList<>();

        // Find section range using case-insensitive regex pattern
        Pattern sectionPattern = Pattern.compile("(?i)\\b" + sectionName + "\\b");
        Matcher sectionMatcher = sectionPattern.matcher(fullText);
        
        if (!sectionMatcher.find()) {
            LOGGER.warning("Seção '" + sectionName + "' não encontrada no PDF");
            return laws;
        }
        
        int startIdx = sectionMatcher.end();

        // End of section is the start of the next section or end of file
        int endIdx = fullText.length();
        String nextSectionName = sectionName.equals("EXPEDIENTE") ? "ORDEM DO DIA" : null;
        
        if (nextSectionName != null) {
            Pattern nextPattern = Pattern.compile("(?i)\\b" + nextSectionName + "\\b");
            Matcher nextMatcher = nextPattern.matcher(fullText);
            if (nextMatcher.find(startIdx)) {
                endIdx = nextMatcher.start();
            }
        }

        String sectionContent = fullText.substring(startIdx, endIdx);
        String[] lines = sectionContent.split("\\r?\\n");
        List<String> validLines = new ArrayList<>();

        for (String line : lines) {
            String tl = line.trim();
            if (tl.isEmpty())
                continue;

            // Filter out headers/footers using configurable patterns
            boolean isFiltered = false;
            for (String filter : headerFilters) {
                if (tl.contains(filter) || tl.matches("(?i).*" + Pattern.quote(filter.toLowerCase()) + ".*")) {
                    isFiltered = true;
                    break;
                }
            }
            
            if (!isFiltered) {
                validLines.add(tl);
            }
        }

        String cleanSectionText = String.join(" ", validLines);
        Pattern numPattern = Pattern.compile("(?i)N[^0-9a-zA-Z\\s]{0,5}\\s*(\\d+/\\d{4})");
        Matcher m = numPattern.matcher(cleanSectionText);

        List<int[]> matches = new ArrayList<>();
        List<String> numbers = new ArrayList<>();
        while (m.find()) {
            String numStr = m.group(1);
            matches.add(new int[] { m.start(), m.end() });
            numbers.add(numStr);
        }

        for (int i = 0; i < matches.size(); i++) {
            int nStart = matches.get(i)[0];
            String number = numbers.get(i);

            int lowerBound = (i == 0) ? 0 : matches.get(i - 1)[1];
            String gap = cleanSectionText.substring(lowerBound, nStart);
            String prefix = extractUppercasePrefix(gap);

            int titleAbsStart = (prefix.isEmpty()) ? nStart : (lowerBound + gap.lastIndexOf(prefix));

            int blockEnd;
            if (i + 1 < matches.size()) {
                int nextLower = matches.get(i)[1];
                int nextNStart = matches.get(i + 1)[0];
                String nextGap = cleanSectionText.substring(nextLower, nextNStart);
                String nextPrefix = extractUppercasePrefix(nextGap);
                blockEnd = nextLower
                        + (nextPrefix.isEmpty() ? (nextNStart - nextLower) : nextGap.lastIndexOf(nextPrefix));
            } else {
                blockEnd = cleanSectionText.length();
            }

            if (titleAbsStart > blockEnd)
                titleAbsStart = nStart;
            String rawItem = cleanSectionText.substring(titleAbsStart, blockEnd).trim();
            if (rawItem.isEmpty())
                continue;

            Matcher mInside = numPattern.matcher(rawItem);
            if (mInside.find()) {
                int numEnd = mInside.end();
                Matcher urgMatcher = Pattern.compile("(?i)^\\s*\\(URG.NCIA\\)").matcher(rawItem.substring(numEnd));
                int titleEnd = numEnd + (urgMatcher.find() ? urgMatcher.end() : 0);

                String title = rawItem.substring(0, titleEnd).trim().replaceAll("\\s+", " ");
                String remaining = rawItem.substring(titleEnd).trim();

                // Extract author and summary using dedicated method
                AuthorInfo authorInfo = extractAuthorInfo(remaining);
                String author = authorInfo.author;
                String summary = authorInfo.summary;

                Law newLaw = new Law(number, title, summary, author, sectionName);

                // Improved deduplication: check against ALL laws in the same section, not just the last one
                if (isNotDuplicate(laws, newLaw, sectionName)) {
                    laws.add(newLaw);
                } else {
                    LOGGER.fine("Lei duplicada removida: " + number + " - " + title);
                }
            }
        }
        return laws;
    }

    
    /**
     * Extrai autor e resumo de um texto contendo essas informações.
     * @param remaining texto após o número e título
     * @return objeto contendo autor e resumo processados
     */
    private AuthorInfo extractAuthorInfo(String remaining) {
        String author = "";
        String summary = remaining;
        
        if (remaining.isEmpty()) {
            return new AuthorInfo(author, summary);
        }
        
        String remUpper = remaining.toUpperCase();
        
        // Check hardcoded author patterns first
        for (String pattern : authorPatterns) {
            if (remUpper.startsWith(pattern)) {
                author = pattern.substring(0, 1) + pattern.substring(1).toLowerCase();
                summary = remaining.substring(pattern.length()).trim().replaceAll("\\s+", " ");
                return new AuthorInfo(author, summary);
            }
        }
        
        // Fallback: heuristic - authors are usually fully uppercase at the start of the summary
        String[] words = remaining.split("\\s+");
        StringBuilder authorBuilder = new StringBuilder();
        int summaryStart = 0;

        Set<String> stopWords = new HashSet<>(Arrays.asList(
                "PROJETO", "INDICAÇÃO", "REQUERIMENTO", "MOÇÃO",
                "DECRETO", "PORTARIA", "OFÍCIO"));

        for (int w = 0; w < words.length; w++) {
            String cleanWord = words[w].toUpperCase().replaceAll("[^A-ZÇÃÕÁÉÍÓÚÂÊÎÔÛ]", "");
            if (stopWords.contains(words[w].toUpperCase()) || stopWords.contains(cleanWord)) {
                break;
            }

            if (isAllUppercase(words[w]) && !words[w].matches(".*\\d+.*")
                    && !words[w].matches(".*[\\.,;]+.*")) {
                if (authorBuilder.length() > 0)
                    authorBuilder.append(" ");
                authorBuilder.append(words[w]);
                summaryStart += words[w].length() + 1;
            } else {
                break;
            }
        }
        
        author = authorBuilder.toString().trim();
        if (summaryStart < remaining.length()) {
            summary = remaining.substring(summaryStart).trim().replaceAll("\\s+", " ");
        }
        
        return new AuthorInfo(author, summary);
    }
    
    /**
     * Verifica se uma lei é duplicada comparando com todas as leis já extraídas da mesma seção.
     * @param laws lista de leis já extraídas
     * @param newLaw lei a ser verificada
     * @param sectionName seção atual
     * @return true se a lei não é duplicada
     */
    private boolean isNotDuplicate(List<Law> laws, Law newLaw, String sectionName) {
        String newNumber = newLaw.getNumber();
        String newTitle = newLaw.getTitle();
        String newKeyword = getBaseKeyword(newTitle);
        
        // If no recognizable keyword, consider it not a duplicate
        if (newKeyword.isEmpty()) {
            return true;
        }
        
        try {
            int newNum = Integer.parseInt(newNumber.split("/")[0]);
            
            for (Law law : laws) {
                if (!law.getSection().equals(sectionName)) {
                    continue;
                }
                
                String existingNumber = law.getNumber();
                String existingTitle = law.getTitle();
                String existingKeyword = getBaseKeyword(existingTitle);
                
                try {
                    int existingNum = Integer.parseInt(existingNumber.split("/")[0]);
                    
                    // Same number + same keyword pattern = duplicate
                    if (existingNum == newNum && existingKeyword.equalsIgnoreCase(newKeyword)) {
                        return false;
                    }
                } catch (Exception e) {
                    // Skip if can't parse existing number
                }
            }
        } catch (Exception e) {
            // Skip if can't parse new number
        }
        
        return true;
    }
    
    /**
     * Classe interna para retornar autor e resumo como um par.
     */
    private static class AuthorInfo {
        String author;
        String summary;
        
        AuthorInfo(String author, String summary) {
            this.author = author;
            this.summary = summary;
        }
    }

    private String getBaseKeyword(String title) {
        if (title == null) return "";
        String upper = title.toUpperCase();
        if (upper.contains("PROJETO")) return "PROJETO";
        if (upper.contains("INDICA")) return "INDICAÇÃO";
        if (upper.contains("REQUERIMENTO")) return "REQUERIMENTO";
        if (upper.contains("MOÇ")) return "MOÇÃO";
        if (upper.contains("DECRETO")) return "DECRETO";
        if (upper.contains("PORTARIA")) return "PORTARIA";
        return "";
    }

    /**
     * Varre as palavras do gap de TRÁS PARA FRENTE e coleta palavras em maiúsculas.
     * Para quando encontra uma palavra com letra minúscula (pertence ao
     * resumo/destino anterior).
     * Retorna o prefixo do título do próximo item (ex: "PROJETO DE LEI ORDINÁRIA
     * EXECUTIVO").
     */
    private String extractUppercasePrefix(String gap) {
        if (gap == null || gap.trim().isEmpty())
            return "";

        String[] words = gap.trim().split("\\s+");
        int endIdx = words.length - 1;

        // Keywords that usually start a title in this project
        java.util.Set<String> keywords = new java.util.HashSet<>(java.util.Arrays.asList(
                "PROJETO", "INDICAÇÃO", "INDICA\\u00C7\\u00C3O", "REQUERIMENTO", "MOÇÃO", "MO\\u00C7\\u00C3O",
                "DECRETO", "PORTARIA"));

        // Varrer de trás para frente
        int startIdx = endIdx + 1;
        for (int w = endIdx; w >= 0; w--) {
            String upperWord = words[w].toUpperCase();
            if (isAllUppercase(words[w])) {
                startIdx = w;
                // If we hit a starting keyword, we stop harvesting more words to avoid catching
                // the previous author
                if (keywords.contains(upperWord) || keywords.contains(upperWord.replaceAll("[^A-Z]", ""))) {
                    break;
                }
            } else {
                break;
            }
        }

        if (startIdx > endIdx)
            return ""; // Nada coletado

        StringBuilder sb = new StringBuilder();
        for (int w = startIdx; w <= endIdx; w++) {
            if (sb.length() > 0)
                sb.append(" ");
            sb.append(words[w]);
        }
        return sb.toString().trim();
    }

    /**
     * Retorna true se a palavra não tem nenhuma letra minúscula.
     * Números, pontuação e letras maiúsculas (incluindo acentuadas) são permitidos.
     */
    private boolean isAllUppercase(String word) {
        if (word == null || word.isEmpty())
            return false;
        for (char c : word.toCharArray()) {
            if (Character.isLetter(c) && Character.isLowerCase(c)) {
                return false;
            }
        }
        return true;
    }
}
