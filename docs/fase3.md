# Fase 3: Centralizar Versões

**Objetivo:** Evitar duplicação de versão entre Gradle, properties e metadados dos loaders, consolidando tudo num *Version Catalog* (`libs.versions.toml`).

## Passos Realizados
1.  **Criação do Version Catalog:** Criado `gradle/libs.versions.toml` contendo todas as dependências do mod, ferramentas do Gradle, versões do Minecraft, Parchment, NeoForge, Fabric e Forge.
2.  **Limpeza do `gradle.properties`:** Removidas todas as chaves de versão hardcoded (`minecraft_version`, `fabric_version`, etc) que geravam duplicação.
3.  **Atualização de Build Scripts:**
    *   Substituídas as referências `group: '...', name: '...', version: '...'` por `libs.mixin`, `libs.bcrypt`, etc, no `common/build.gradle`, `fabric/build.gradle`, `neoforge/build.gradle` e `forge/build.gradle`.
    *   Corrigido uso do plugin Loom no root.
4.  **Injeção de Metadados (`buildSrc`):**
    *   Atualizado o script `multiloader-common.gradle` para extrair os valores do `libs.versions.toml` e passá-los dinamicamente para o `expandProps` do `processResources`.
    *   **Resultado:** Agora os arquivos `fabric.mod.json`, `mods.toml`, `neoforge.mods.toml` e mixins recebem a versão automaticamente do TOML.

## Resultados
A arquitetura de build está madura e modernizada. Todo o gerenciamento de dependências e metadados agora reside num único arquivo limpo, prevenindo erros de esquecimento em atualizações futuras.

*   **Arquivos Alterados:**
    *   `gradle/libs.versions.toml` (Novo)
    *   `gradle.properties`
    *   `build.gradle` (Root)
    *   `common/build.gradle`
    *   `fabric/build.gradle`
    *   `neoforge/build.gradle`
    *   `forge/build.gradle`
    *   `buildSrc/src/main/groovy/multiloader-common.gradle`
*   **Comandos Executados:** `./gradlew build`
*   **Resultado do build/testes:** `BUILD SUCCESSFUL in 13s` em todos os loaders.
*   **Status:** Concluído com Sucesso. A estrutura está livre de duplicações.
