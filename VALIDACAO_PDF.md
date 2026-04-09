# 📋 Guia de Validação e Tratamento de Encoding - PdfService

## 🎯 Visão Geral

A refatoração do `PdfService` introduz funcionalidades robustas para:
- **Validação de PDFs** antes do processamento
- **Detecção automática de problemas de encoding/caracteres corrompidos**
- **Tratamento e recuperação** de PDFs malformados
- **Extração de metadados** técnicos do PDF

---

## 📚 Novos Métodos

### 1. `validatePdfFormat(String filePath): PdfValidationResult`

Valida um arquivo PDF e retorna informações detalhadas sobre sua qualidade.

**Retorna:**
- `isValid` - Se o PDF é válido para processamento
- `totalPages` - Número de páginas
- `fileSize` - Tamanho em bytes
- `textLength` - Tamanho do texto extraído
- `isEncrypted` - Se o PDF está encriptado
- `hasEncodingIssues` - Se há problemas de encoding
- `hasSections` - Se contém seções esperadas
- `estimatedQuality` - Score de 0.0 a 1.0
- `errorMessage` - Descrição do erro, se houver

**Exemplo:**

```java
PdfService pdfService = new PdfService();
PdfValidationResult result = pdfService.validatePdfFormat("pauta.pdf");

if (result.isValid) {
    System.out.println("✓ PDF válido!");
    System.out.println("Páginas: " + result.totalPages);
    System.out.println("Qualidade: " + (result.estimatedQuality * 100) + "%");
} else {
    System.out.println("✗ PDF inválido: " + result.errorMessage);
    if (result.hasEncodingIssues) {
        System.out.println("⚠ Detectados problemas de encoding");
    }
}

System.out.println(result); // String formatada
```

---

### 2. `getPdfMetadata(String filePath): PdfMetadata`

Extrai informações técnicas sobre o arquivo PDF.

**Retorna:**
- `fileName` - Nome do arquivo
- `filePath` - Caminho completo
- `fileSize` - Tamanho em bytes
- `lastModified` - Timestamp da última modificação
- `totalPages` - Número de páginas
- `isEncrypted` - Se está encriptado
- `producer` - App que criou o PDF
- `creator` - Autor do PDF
- `title` - Título do documento

**Exemplo:**

```java
PdfMetadata metadata = pdfService.getPdfMetadata("pauta.pdf");

System.out.println("Arquivo: " + metadata.fileName);
System.out.println("Páginas: " + metadata.totalPages);
System.out.println("Produtor: " + metadata.producer);
System.out.println("Tamanho: " + (metadata.fileSize / 1024) + " KB");
System.out.println(metadata); // String formatada
```

---

### 3. `sanitizeText(String text): String`

Limpa caracteres corrompidos e normaliza espaços em branco.

**O que faz:**
- Remove caracteres de controle
- Corrige UTF-8 double-encoded
- Remove símbolos estranhos
- Normaliza espaços em branco múltiplos

**Exemplo:**

```java
String textoBruto = pdfService.extractRawText("pauta.pdf");
String textoLimpo = pdfService.sanitizeText(textoBruto);

System.out.println("Antes: " + textoBruto.length() + " caracteres");
System.out.println("Depois: " + textoLimpo.length() + " caracteres");
```

---

### 4. `extractLawsFromPdf(String filePath): List<Law>` (MELHORADO)

Agora valida e sanitiza o PDF automaticamente:

```java
// Valida antes de processar
// Se houver problemas de encoding, aplica sanitização automaticamente
// Retorna lista com leis extraídas
List<Law> laws = pdfService.extractLawsFromPdf("pauta.pdf");

for (Law law : laws) {
    System.out.println(law.getNumber() + " | " + law.getTitle());
}
```

---

## 🔍 Métodos Privados (Internos)

### `detectEncodingIssues(String text): boolean`

Detecta problemas de encoding analisando:
- Caracteres de controle incomuns
- Caracteres ASCII estendido (127-160)
- Caracteres de substituição UTF-8
- Limiar: > 5% de caracteres suspeitos = problema

---

### `validateSections(String text): boolean`

Verifica se o PDF contém seções esperadas:
- "EXPEDIENTE"
- "ORDEM DO DIA"
- "PAUTA"
- "SESSÃO"

---

### `calculateQualityScore(String text, PdfValidationResult): double`

Calcula score de qualidade (0.0 a 1.0) baseado em:
- Tamanho do texto (>1000 chars = +0.2)
- Múltiplas páginas = +0.15
- Sem problemas de encoding = +0.2
- Presença de seções esperadas = +0.2
- Base: 0.5

---

## 📊 Logging

Todos os eventos são registrados com `java.util.logging.Logger`:

```
INFO: PDF validado: 3 páginas, 5200 caracteres, qualidade: 85%, encoding OK: true, seções: true
WARNING: PDF não contém seções 'EXPEDIENTE' ou 'ORDEM DO DIA'
SEVERE: Erro ao validar PDF: File not found
FINE: Lei duplicada removida: 0059/2026 - PROJETO DE LEI
```

Para ver os logs, configure o nivel do logger em sua aplicação:
```java
java.util.logging.Logger.getLogger("com.camara.data.PdfService")
    .setLevel(java.util.logging.Level.INFO);
```

---

## 💡 Casos de Uso

### Caso 1: Validar PDF antes de processar

```java
PdfService pdf = new PdfService();
PdfValidationResult validation = pdf.validatePdfFormat("pauta.pdf");

if (!validation.isValid) {
    if (validation.hasEncodingIssues) {
        System.out.println("⚠ PDF tem problemas de encoding, tentando sanitizar...");
    } else {
        System.out.println("✗ Não é um PDF válido");
        return;
    }
}

List<Law> laws = pdf.extractLawsFromPdf("pauta.pdf");
System.out.println("✓ Extraídas " + laws.size() + " leis");
```

---

### Caso 2: Verificar qualidade antes de exibir dados

```java
PdfService pdf = new PdfService();
PdfValidationResult result = pdf.validatePdfFormat("pauta.pdf");

if (result.estimatedQuality >= 0.7) {
    // Qualidade boa, processar normalmente
    List<Law> laws = pdf.extractLawsFromPdf("pauta.pdf");
} else if (result.estimatedQuality >= 0.5) {
    // Qualidade aceitável, avisar usuário
    System.out.println("⚠ PDF com qualidade moderada (" + 
        (result.estimatedQuality * 100) + "%), alguns dados podem estar incorretos");
    List<Law> laws = pdf.extractLawsFromPdf("pauta.pdf");
} else {
    // Qualidade ruim, não processar
    System.out.println("✗ PDF com qualidade muito baixa, não é possível processar");
}
```

---

### Caso 3: Diagnosticar problemas com PDFs anormais

```java
PdfService pdf = new PdfService();

// Validar
PdfValidationResult validation = pdf.validatePdfFormat("pauta.pdf");
System.out.println("Validação: " + validation);

// Metadados
PdfMetadata metadata = pdf.getPdfMetadata("pauta.pdf");
System.out.println("Metadados: " + metadata);

// Diagnóstico detalhado
if (!validation.isValid) {
    System.out.println("\n📋 DIAGNÓSTICO:");
    System.out.println("- Arquivo existe: " + (validation.fileSize > 0));
    System.out.println("- Tem páginas: " + (validation.totalPages > 0));
    System.out.println("- Tem conteúdo: " + (validation.textLength > 0));
    System.out.println("- Encoding OK: " + !validation.hasEncodingIssues);
    System.out.println("- Tem seções: " + validation.hasSections);
    
    if (!validation.errorMessage.isEmpty()) {
        System.out.println("- Erro: " + validation.errorMessage);
    }
}
```

---

### Caso 4: Usar em UI para feedback ao usuário

```java
// Em SelectionPanel ou MainFrame
PdfService pdf = new PdfService();
PdfValidationResult validation = pdf.validatePdfFormat(pdfPath);

if (validation.hasEncodingIssues) {
    showWarningDialog("⚠ Encoding",
        "PDF detectado com problemas de codificação.\n" +
        "Tentando recuperação automática...");
}

List<Law> laws = pdf.extractLawsFromPdf(pdfPath);

if (laws.isEmpty() && validation.textLength > 100) {
    showErrorDialog("⚠ Aviso",
        "Nenhuma lei encontrada.\n" +
        "PDF pode ter formato diferente.");
} else if (!laws.isEmpty()) {
    showInfoDialog("✓ Sucesso",
        "Extraídas " + laws.size() + " leis\n" +
        "Qualidade: " + (validation.estimatedQuality * 100) + "%");
}
```

---

## 🚀 Integração Contínua

As validações são automáticas ao chamar `extractLawsFromPdf()`:

1. ✅ Valida o formato do PDF
2. ✅ Detecta problemas de encoding
3. ✅ Se há problema, aplica sanitização
4. ✅ Extrai as leis
5. ✅ Log de todas as etapas

---

## 📈 Melhorias Futuras

- [ ] Suporte a OCR para PDFs scaneados
- [ ] Integração com Apache Tika para detecção de encoding automática
- [ ] Cache de PDFs validados
- [ ] UI dashboard com estatísticas de qualidade
- [ ] Suporte a diferentes idiomas

---

## 📝 Resumo das Classes Internas

### `PdfValidationResult`
Resultado completo da validação de um PDF com 9 atributos.

### `PdfMetadata`
Informações técnicas sobre o arquivo PDF extraídas do documento.

---

**Versão:** 1.0  
**Data:** Abril 2026  
**Status:** ✅ Pronte para produção
