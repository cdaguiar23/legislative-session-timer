# Legislative Session Timer (Camara Timer)

Sistema leve e eficiente para cronometragem de falas e controle de microfones em sessões legislativas (Câmaras de Vereadores). Desenvolvido em Java Swing, o projeto foi otimizado para rodar em hardware modesto (como processadores Celeron com 4GB de RAM).

## 🚀 Funcionalidades

- **Cronômetro de Fala**: Controle preciso do tempo de fala dos vereadores (1, 3, 5, 7 e 10 minutos).
- **Extração de Pauta (PDF)**: Leitura inteligente da pauta da sessão (coluna TÍTULO) para exibição automática no monitor.
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

## 📦 Como Compilar e Rodar

Para gerar o arquivo executável (.jar):
```bash
mvn clean package
```
O arquivo será gerado em `target/camara-timer-1.6.1.jar`.

Para rodar o projeto:
```bash
java -jar target/camara-timer-1.6.1.jar
```

## 🐧 Dica para Linux (Lubuntu)
Se estiver rodando via Pen Drive (Modo Live), certifique-se de instalar o Java:
```bash
sudo apt update && sudo apt install default-jre
```

---
Desenvolvido para proporcionar agilidade e transparência nas sessões legislativas.
# legislative-session-timer
