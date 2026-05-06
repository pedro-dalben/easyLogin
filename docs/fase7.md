# Fase 7: GitHub Actions e CI/CD

A última fase foca na automação da distribuição e garantia de qualidade contínua.

## Atividades Planejadas
- **Workflow Multi-Versão**: Criação de uma matriz no GitHub Actions que compila o mod para as versões 1.21.1, 1.21.11 e 26.1.1 simultaneamente.
- **Matrix de JDK**: Uso dinâmico do JDK 21 para versões legadas e JDK 25 para a versão 26.1.x.
- **Publicação de Artefatos**: Geração de JARs nomeados corretamente (ex: `easylogin-neoforge-26.1.1-1.0.0.jar`) como artefatos de build.

## Resultados Esperados
- Entrega contínua e automatizada para todas as versões âncora.
- Detecção imediata de quebras de API em novas atualizações do Minecraft através do CI.
