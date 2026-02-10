### Como criar uma nova release do mod (Automático)

Agora, o processo de release está totalmente automatizado ao fazer push para o `master`:

1.  **Atualize a versão**:
    Altere a versão no arquivo `gradle.properties` (ex: `version=1.1.0`).

2.  **Faça o Push para master**:
    Ao dar o push para a branch `master`, o GitHub Actions irá:
    - Buildar o projeto.
    - Ler a versão do `gradle.properties`.
    - Criar uma tag automaticamente (ex: `v1.1.0`).
    - Criar a Release com os arquivos buildados.

### Usando o script local (Opcional):

Para facilitar, criei um script `publish.sh` na raiz. Ele facilita o commit e o push com a tag correta:

// turbo
```bash
./publish.sh
```

Este script detecta a versão, pergunta se deseja commitar e faz o push já com as tags para você.
