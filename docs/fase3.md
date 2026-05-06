# Fase 3: Migração de API (1.21.1 -> 1.21.11)

Nesta fase, resolvemos as quebras de API introduzidas pelo Minecraft 1.21.11, utilizando condicionais do Stonecutter para manter a compatibilidade com a 1.21.1.

## Quebras de API Resolvidas

### 1. ResourceLocation para Identifier
O Minecraft renomeou `ResourceLocation` para `Identifier` em pacotes e classes.
- **Solução**: Uso de `//? if` para importar a classe correta e instanciar usando os nomes específicos de cada versão.

### 2. Sistema de Permissões
O acesso a níveis de comando mudou de simples `int` para um sistema baseado em `PermissionSet`.
- **Solução**: Refatoração do `AuthCommand` para usar `Permission.HasCommandLevel` na 1.21.11 e verificações manuais na 1.21.1.

### 3. Teleportação de Jogadores
A assinatura do método `teleportTo` no `ServerPlayer` mudou drasticamente (passando de 3 para 8 argumentos).
- **Solução**: Implementação de condicionais no `AuthManager` e `PlayerUtil` para passar o `ServerLevel`, flags de movimento e parâmetros de orientação exigidos pela nova API.

### 4. Sistema de Sons
Mudanças no escopo de som em entidades exigiram o uso de `level().playSound(...)` em vez de métodos diretos de notificação.

## Resultados
- Código-fonte único capaz de compilar para ambas as versões com zero duplicação de lógica de negócios.
- Garantia de que bugs corrigidos em uma versão serão automaticamente aplicados na outra.
