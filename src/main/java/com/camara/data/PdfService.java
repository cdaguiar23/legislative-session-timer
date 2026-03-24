package com.camara.data;

import com.camara.model.Law;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PdfService {

    public List<Law> extractLawsFromPdf(String filePath) {
        List<Law> laws = new ArrayList<>();
        File pdfFile = new File(filePath);
        if (!pdfFile.exists()) {
            return laws;
        }

        try (PDDocument document = PDDocument.load(pdfFile)) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(false); // Desativar ordenação por posição para ver se as colunas ficam separadas

            String text = stripper.getText(document);
             
             String[] lines = text.split("\\r?\\n");

            boolean inOrdemDoDia = false;
            List<String> validLines = new ArrayList<>();

            for (String line : lines) {
                String tl = line.trim();
                String normTl = tl.toUpperCase().replace(" ", "").replace("-", "");
                if (normTl.contains("ORDEMDODIA")) {
                    inOrdemDoDia = true;
                    continue;
                }
                if (inOrdemDoDia) {
                    if (tl.contains("ESTADO DE SANTA CATARINA") ||
                        tl.matches("(?i).*C.MARA\\sMUNICIPAL.*|.*C\\u00C2MARA\\sMUNICIPAL.*") ||
                        tl.contains("www.camaracanelinha.sc.gov.br") ||
                        tl.startsWith("Rua Manoel Francisco") ||
                        tl.startsWith("Fone:") ||
                        tl.matches("(?i)P.gina.*|P\\u00E1gina.*")) {
                        continue;
                    }
                    if (tl.matches("(?i)T.TULO\\sAUTORIA\\sRESUMO\\sDESTINO|T\\u00CDTULO\\sAUTORIA\\sRESUMO\\sDESTINO")) {
                        continue;
                    }
                    validLines.add(tl);
                }
            }

            String fullText = String.join(" ", validLines);

            // Regex para encontrar "Nº XXXX/YYYY" e variações de encoding
            Pattern numPattern = Pattern.compile("(?i)N[^0-9a-zA-Z\\s]{0,5}\\s*(\\d+/\\d{4})");
            Matcher m = numPattern.matcher(fullText);

            // Coletar todos os matches: [start, end] e número
            List<int[]> matches = new ArrayList<>();
            List<String> numbers = new ArrayList<>();
            while (m.find()) {
                matches.add(new int[]{m.start(), m.end()});
                numbers.add(m.group(1));
            }

            for (int i = 0; i < matches.size(); i++) {
                int nStart  = matches.get(i)[0]; // início do "Nº"
                int nEnd    = matches.get(i)[1]; // fim de "Nº XXXX/YYYY"
                String number = numbers.get(i);

                // O gap entre o FIM do Nº anterior e o INÍCIO do Nº atual
                // contém: [autor anterior] [resumo anterior] [destino anterior] [prefixo do título atual]
                int lowerBound = (i == 0) ? 0 : matches.get(i - 1)[1];
                String gap = fullText.substring(lowerBound, nStart);

                // Estratégia melhorada para Canelinha: 
                // O título na coluna TITULO geralmente é: [TIPO] [Nº] [URGENCIA]
                // Ex: "PROJETO DE LEI ORDINÁRIA EXECUTIVO Nº 0027/2026 (URGÊNCIA)"
                // O prefixo é tudo o que estiver em maiúsculo ANTES do Nº e DEPOIS do último destino/autor.
                 String prefix = extractUppercasePrefix(gap);
                 
                 // Início absoluto do bloco deste item
                 int titleAbsStart = (prefix.isEmpty()) ? nStart : (lowerBound + gap.lastIndexOf(prefix));

                // Fim do bloco: início do prefixo do próximo item (ou fim do texto)
                int blockEnd;
                if (i + 1 < matches.size()) {
                    int nextLower  = matches.get(i)[1];
                    int nextNStart = matches.get(i + 1)[0];
                    String nextGap = fullText.substring(nextLower, nextNStart);
                    String nextPrefix = extractUppercasePrefix(nextGap);
                    if (!nextPrefix.isEmpty()) {
                        int nextPrefixIdx = nextGap.lastIndexOf(nextPrefix);
                        blockEnd = nextLower + nextPrefixIdx;
                    } else {
                        blockEnd = nextNStart;
                    }
                } else {
                    blockEnd = fullText.length();
                }

                if (titleAbsStart > blockEnd) titleAbsStart = nStart;
                String rawItem = fullText.substring(titleAbsStart, blockEnd).trim();
                if (rawItem.isEmpty()) continue;

                // Reidentificar o Nº dentro do bloco para separar título e resumo
                Matcher mInside = numPattern.matcher(rawItem);
                if (mInside.find()) {
                    int numEnd = mInside.end();

                    // Verifica (URGÊNCIA) logo após o número
                    Matcher urgMatcher = Pattern.compile("(?i)^\\s*\\(URG.NCIA\\)").matcher(rawItem.substring(numEnd));
                    int titleEnd = numEnd;
                    if (urgMatcher.find()) {
                        titleEnd += urgMatcher.end();
                    }

                    String title   = rawItem.substring(0, titleEnd).trim().replaceAll("\\s+", " ");
                    String summary = rawItem.substring(titleEnd).trim().replaceAll("\\s+", " ");

                    laws.add(new Law(number, title, summary));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return laws;
    }

    /**
     * Varre as palavras do gap de TRÁS PARA FRENTE e coleta palavras em maiúsculas.
     * Para quando encontra uma palavra com letra minúscula (pertence ao resumo/destino anterior).
     * Retorna o prefixo do título do próximo item (ex: "PROJETO DE LEI ORDINÁRIA EXECUTIVO").
     */
    private String extractUppercasePrefix(String gap) {
        if (gap == null || gap.trim().isEmpty()) return "";

        String[] words = gap.trim().split("\\s+");
        int endIdx = words.length - 1;

        // Varrer de trás para frente
        int startIdx = endIdx + 1;
        for (int w = endIdx; w >= 0; w--) {
            if (isAllUppercase(words[w])) {
                startIdx = w;
            } else {
                break; // Primeira palavra com minúscula = limite do resumo anterior
            }
        }

        if (startIdx > endIdx) return ""; // Nada coletado

        StringBuilder sb = new StringBuilder();
        for (int w = startIdx; w <= endIdx; w++) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(words[w]);
        }
        return sb.toString().trim();
    }

    /**
     * Retorna true se a palavra não tem nenhuma letra minúscula.
     * Números, pontuação e letras maiúsculas (incluindo acentuadas) são permitidos.
     */
    private boolean isAllUppercase(String word) {
        if (word == null || word.isEmpty()) return false;
        for (char c : word.toCharArray()) {
            if (Character.isLetter(c) && Character.isLowerCase(c)) {
                return false;
            }
        }
        return true;
    }
}
