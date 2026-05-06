# Fase 1: Preparação e Centralização (Common)

Nesta fase, o objetivo foi desacoplar a lógica do mod das APIs específicas de cada loader (NeoForge/Fabric) e centralizá-la no módulo `:common`.

## Atividades Realizadas
- **Análise de Dependências**: Identificação de bibliotecas externas (BCrypt, JetBrains Annotations) e APIs do Minecraft que mudariam entre versões.
- **Criação do Módulo Common**: Centralização de toda a lógica de autenticação (`AuthManager`), utilitários de mensagem (`MessageFormatter`) e utilitários de jogador (`PlayerUtil`).
- **Abstração de API**: Preparação do código para receber condicionais de pré-processamento.
- **Refatoração de Build**: Configuração inicial do Gradle para suportar um projeto multi-módulo básico.

## Resultados
- Código unificado em um único local, reduzindo a necessidade de duplicar correções de bugs entre loaders.
- Base sólida para a implementação do Stonecutter.
