# Legislative Session Timer (Camara Timer)

Sistema leve e eficiente para cronometragem de falas e controle de microfones em sessões legislativas (Câmaras de Vereadores). Desenvolvido em Java Swing, o projeto foi otimizado para rodar em hardware modesto (como processadores Celeron com 4GB de RAM).

## 🚀 Funcionalidades

- **Cronômetro de Fala**: Controle preciso do tempo de fala dos vereadores (1, 3, 5, 7 e 10 minutos).
- **Extração de Pauta (PDF)**: Leitura inteligente da pauta da sessão (coluna TÍTULO) para exibição automática no monitor.
- **Validação Robusta de PDFs**: Detecta automaticamente problemas de encoding e formatos anormais.
- **Tratamento de Encoding**: Recupera dados de PDFs com caracteres corrompidos.
- **Controle de Microfones**: Integração via TCP/IP com controladoras de microfone (ex: Votech).
- **Modo Aparte**: Suporte para dois cronômetros simultâneos na tela (orador principal e aparteante).
- **Interface de Monitoramento**: Janela secundária limpa e profissional para exibição em telões/TVs.
- **Modo Tribuna**: Função especial para ativação rápida de microfones fixos sem afetar o telão.
- **Design Responsivo**: Layout que se adapta a diferentes resoluções de tela.

## 🛠️ Requisitos de Hardware e Software

- **Java**: JRE/JDK 8 ou superior.
- **Sistema Operacional**: Compatível com Windows, Linux (testado em Lubuntu) e macOS.
- **Hardware Mínimo**: Processador Celeron e 4GB de RAM.

## ⚙️ Configuração

O sistema utiliza arquivos JSON para configurações locais:

- `config.json`: Define o IP e porta da controladora de microfones e o caminho do PDF da pauta.
- `vereadores.json`: Cadastro dos vereadores (nome, partido, ID do microfone e caminho da foto).

## � Validação e Tratamento de PDFs

O sistema agora inclui validação robusta e tratamento inteligente de PDFs:

### Recursos Principais

- **Validação Automática**: Detecta PDFs inválidos, corrompidos ou com formato anormalo
- **Detecção de Encoding**: Identifica automaticamente problemas de corrupção de caracteres
- **Recuperação Automática**: Limpa e recupera texto de PDFs com encoding quebrado
- **Extração de Metadados**: Obtém informações técnicas do PDF (páginas, produtor, criador, etc)
- **Score de Qualidade**: Avalia qualidade do PDF de 0% a 100%

### Exemplo de Uso

```java
PdfService pdf = new PdfService();

// Validar PDF antes de processar
PdfService.PdfValidationResult validation = pdf.validatePdfFormat("pauta.pdf");
if (validation.isValid) {
    List<Law> laws = pdf.extractLawsFromPdf("pauta.pdf");
} else {
    System.out.println("Erro: " + validation.errorMessage);
}

// Obter informações técnicas
PdfService.PdfMetadata metadata = pdf.getPdfMetadata("pauta.pdf");
System.out.println("Páginas: " + metadata.totalPages);
System.out.println("Produtor: " + metadata.producer);
```

Para documentação completa, veja [VALIDACAO_PDF.md](VALIDACAO_PDF.md).

## �📦 Como Compilar e Rodar

Para gerar o arquivo executável (.jar):
```bash
mvn clean package
```
O arquivo será gerado em `target/camara-timer-1.6.1.jar`.

Para rodar o projeto:
```bash
java -jar target/camara-timer-1.6.1.jar
```

## 🐧 Dica para Linux (Ubuntu ou variantes)
Se estiver rodando Linux Ubuntu ou variantes como Lubuntu ou Xubuntu, certifique-se de instalar o Java:
```bash
sudo apt update && sudo apt install default-jre
```

---
Desenvolvido para proporcionar agilidade e transparência nas sessões legislativas.
# legislative-session-timer
