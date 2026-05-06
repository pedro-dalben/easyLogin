# Fase 4: Configuração Multi-Loader e Estabilização

A Fase 4 focou em garantir que o loader NeoForge pudesse consumir o código do módulo Common corretamente em todas as versões.

## Desafios de Build Resolvidos

### 1. Circularidade no BuildSrc
Remoção de dependências diretas de projetos no `buildSrc`, movendo a lógica para scripts Groovy aplicados sob demanda.

### 2. Seleção de Variantes (Matching)
Resolvido o erro "No matching variant" do Gradle através da estratégia de **Shared Source Directory**.
- **Mudança**: Em vez de depender do projeto `:common` via artefato JAR (que causava conflitos de atributos JVM), o projeto `:neoforge` agora aponta diretamente para o diretório de fontes do `common`.
- **Vantagem**: Isso permite que o Stonecutter processe as condicionais diretamente durante a compilação de cada loader, simplificando drasticamente a resolução de dependências.

### 3. Sincronização de Dependências
Garantia de que bibliotecas externas (como BCrypt) são propagadas corretamente para todos os subprojetos variantes.

## Resultados
- Build estável para NeoForge 1.21.1 e 1.21.11.
- Pipeline de compilação simplificado e mais rápido.
