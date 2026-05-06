# Fase 2: Implementação do Stonecutter

A Fase 2 focou na implementação do **Stonecutter 0.6.1** para gerenciar múltiplas versões do Minecraft simultaneamente.

## Atividades Realizadas
- **Configuração do Stonecutter**: Registro de cadeias de versão (chains) no `settings.gradle` para os módulos `:common` e `:neoforge`.
- **Criação de Variantes**: Inicialização das pastas de versão (`versions/1.21.1` e `versions/1.21.11`) que funcionam como projetos Gradle independentes mas compartilham o mesmo código-fonte.
- **Integração com BuildSrc**: Centralização da lógica de plugins (Loom, ModDev) no `buildSrc` para garantir consistência entre as versões.
- **Ativação de Condicionais**: Configuração do ambiente para reconhecer diretivas `//? if` e `//? else`.

## Resultados
- Capacidade de alternar o contexto de desenvolvimento entre versões (`./gradlew chv <version>`).
- Estrutura de diretórios organizada para suportar futuras versões (ex: 26.1.x) sem poluir a raiz do projeto.
