# Fase 5: Planejamento Minecraft/NeoForge 26.1.x (Java 25)

A Fase 5 estabelece as diretrizes para o próximo grande salto tecnológico do projeto.

## Diretrizes Principais
- **Java 25**: A versão 26.1 do Minecraft/NeoForge exige Java 25 para build e execução. O Gradle toolchain será configurado para forçar essa versão nos subprojetos 26.1.x.
- **Novas APIs**: Antecipação de mudanças no sistema de componentes de dados e registros que podem exigir novas camadas de abstração.
- **Arquitetura de Transição**: Manutenção do `PlatformHelper` manual, evitando a complexidade da Architectury API a menos que a duplicação se torne insustentável.

## Próximos Passos
1. Adicionar `26.1` às chains do Stonecutter.
2. Criar variantes iniciais e mapear as primeiras quebras de compilação.
3. Testar a compatibilidade do Gradle 8.10+ com o JDK 25.
