# Fase 7: GitHub Actions e CI/CD

A Fase 7 automatizou o ciclo de vida do EasyLogin, garantindo que todas as versões alvo sejam testadas e distribuídas sem intervenção manual.

## Atividades Realizadas
- **Workflow de Build com Matriz**: Implementamos uma matriz de estratégia que executa builds paralelos para as versões `1.21.1`, `1.21.11` e `26.1`.
- **Gerenciamento Dinâmico de JDK**: O workflow detecta a versão do Minecraft e seleciona automaticamente o JDK 21 (legado) ou JDK 25 (moderno).
- **Stonecutter CLI no CI**: Utilizamos o comando `./gradlew chv <version>` para trocar o contexto do projeto antes de cada build na matriz.
- **Release Consolidado**: Configuramos o workflow de tags para compilar todas as variantes e anexar todos os JARs em um único release do GitHub, facilitando a distribuição.

## Configuração Técnica
Os arquivos de workflow estão localizados em:
- `.github/workflows/build.yml`: Validado a cada push/PR.
- `.github/workflows/release.yml`: Acionado em tags `v*`.

## Próximos Passos
- **Publicação em Lojas**: No futuro, integrar com as APIs do Modrinth e CurseForge para upload automático (Minotaur/CurseGradle).
- **Testes Automatizados**: Integrar GameTests na matriz de CI para validar lógica de login em tempo real.
