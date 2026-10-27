# pKits — Plugin de Kits

Um plugin **100% configurável** para criares e gerires kits no teu servidor, com menu paginado, preview do conteúdo e um **editor in-game completo**. Não precisas de mexer em ficheiros para criar ou alterar um kit.

> ⚠️ **Atenção:** O plugin encontra-se atualmente em **fase BETA**. Podes encontrar pequenos bugs ou detalhes a ser ajustados, e novas funções estão a caminho!

---

## 🌟 O que o plugin faz?

- **Menu `/kits` com paginação automática:** setas de navegação e botão de fechar. Se tiveres muitos kits, as páginas criam-se sozinhas.
- **Preview em cada kit:** um botão logo abaixo de cada kit mostra todo o conteúdo antes de resgatar.
- **Informação ao passar o rato:** cooldown, permissão necessária e estado do kit.
- **Armadura nos slots certos:** o preview mostra capacete, peitoral, calças e botas nos slots de armadura.
- **Auto-equipar armadura:** opção por kit para equipar a armadura ao resgatar.
- **Entrega em Shulker Box:** opção por kit para receber tudo dentro de uma shulker, com nome personalizável.
- **Editor in-game:** cria e edita kits sem ficheiros, com menus e botões.
- **100% configurável:** menus, itens, lores, sons e mensagens editáveis, com suporte a **MiniMessage e gradientes**.

---

## 💻 Comandos e Permissões

| Comando | Descrição | Permissão |
| :--- | :--- | :--- |
| `/kits` | Abre o menu principal de kits | `pkits.use` |
| `/kit <nome>` | Resgata um kit diretamente | permissão do kit |
| `/kit criar <nome>` | Cria um kit novo | `pkits.admin` |
| `/kit editar <nome>` | Abre o editor do kit | `pkits.admin` |
| `/kit deletar <nome>` | Remove um kit | `pkits.admin` |
| `/kit valor <texto>` | Responde a uma edição pendente (alternativa ao chat) | `pkits.admin` |
| `/kit reload` | Recarrega configurações e kits | `pkits.admin` |

O comando `/kit` também funciona como `/pkit`.

| Permissão | Descrição | Padrão |
| :--- | :--- | :--- |
| `pkits.use` | Usar o menu `/kits` | todos |
| `pkits.admin` | Criar, editar, apagar e recarregar | OP |
| `pkits.kit.<nome>` | Resgatar o kit (editável por kit) | ninguém |
| `pkits.bypass.cooldown` | Ignorar o cooldown dos kits | OP |
| `pkits.bypass.permission` | Resgatar qualquer kit sem a permissão dele | OP |

---

## 📖 Como funciona cada sistema

### 🗂️ Menu principal (`/kits`)

- Cada kit aparece com o ícone, nome e descrição que definires.
- **Botão esquerdo** resgata o kit. **Botão direito** abre o preview.
- Por baixo de cada kit fica o botão de preview (papel, por padrão).
- Os estados possíveis são **Disponível**, **Sem Permissão**, **Em Cooldown** (com o tempo restante) e **Já resgatado** (kits de uso único).
- A posição de cada kit é definida no editor. Se duas posições coincidirem, o kit seguinte avança para a próxima livre.
- Os slots usados, as setas e os botões ficam em `menu` no `config.yml`.

### 👀 Preview

- Mostra os itens soltos, com encantamentos e lores reais.
- A armadura aparece nos slots de armadura do menu.
- Kits com muitos itens ganham **páginas** com setas.
- Podes definir um **preview diferente** dos itens recebidos, por exemplo para esconder itens surpresa.

### 🛡️ Auto-equipar armadura

Quando está ativo, cada peça do kit é equipada se o slot do jogador estiver livre. Se o slot estiver ocupado, a peça vai para o inventário. Itens a mais do que o inventário aguenta caem aos pés do jogador.

### 📦 Entrega em Shulker Box

Quando está ativo, todos os itens vão dentro de uma shulker, ideal para kits VIP. Um kit com mais de 27 itens entrega várias shulkers. O **nome da shulker é editável por kit**, com as variáveis `%kit-name%`, `%index%` e `%boxes%`. O preview continua a mostrar os itens soltos. Com a shulker ativa, o auto-equipar é ignorado.

### ⏱️ Cooldown

| Valor | Significado |
| :--- | :--- |
| `7d` | 7 dias |
| `12h30m` | 12 horas e 30 minutos |
| `90` | 90 segundos |
| `0` | sem cooldown |
| `-1` | uso único |

O momento do último resgate de cada jogador é guardado em `data.yml`.

---

## 🛠️ Editor in-game

### Criar: `/kit criar <nome>`

Abre um menu onde colocas os itens do kit. Fecha o menu para salvar. Há espaço para **muitos itens**, com várias páginas, e a armadura vai para os slots próprios, que só aceitam peças que servem naquele slot.

### Editar: `/kit editar <nome>`

| Botão | O que faz |
| :--- | :--- |
| Ícone do kit | Clica com um item no cursor para trocar o ícone do menu. Sem item, escreves o nome do material. |
| Nome de exibição | Altera o nome (aceita gradientes). |
| Descrição (lore) | Esquerdo adiciona linha, direito remove a última, Shift limpa tudo. |
| Cooldown | Altera o tempo de espera entre resgates. |
| Permissão | Define a permissão do kit, ou `nenhuma` para remover. |
| Auto-equipar armadura | Alterna entre Ativado e Desativado. |
| Entrega em Shulker Box | Alterna entre Ativado e Desativado. |
| Nome da Shulker | Altera o nome da shulker deste kit, ou `nenhuma` para o padrão. |
| Itens recebidos | Edita os itens que o jogador ganha. |
| Itens do preview | Edita o que o preview mostra. |
| Brilho no ícone | Dá brilho de encantamento ao ícone. |
| Posição no menu | Esquerdo +1, direito -1, Shift ±5. |

### Escrever valores (nome, lore, cooldown...)

Ao clicar num botão de texto, o menu fecha e podes **escrever no chat**. Se o teu servidor tiver um plugin de chat que interfere, usa **`/kit valor <texto>`**, que dá o mesmo resultado. Também podes clicar na mensagem do plugin para preencher o comando. Escreve `cancelar` para sair sem alterar.

---

## 📂 Configuração (`.yml`)

| Ficheiro | Para que serve |
| :--- | :--- |
| `config.yml` | Menus, slots, materiais, títulos, botões, sons, shulker e formato do tempo |
| `messages.yml` | Todas as mensagens de chat, prompts e ajuda |
| `kits.yml` | Os teus kits (gerado e atualizado automaticamente) |
| `data.yml` | Cooldowns dos jogadores (gerado automaticamente) |

### Variáveis úteis (`config.yml`)

| Variável | Valor |
| :--- | :--- |
| `%kit-name%` | Nome de exibição do kit |
| `%kit-id%` | Identificador do kit |
| `%kit-lore%` | Descrição do kit (como linha própria da lore) |
| `%cooldown%` | Cooldown formatado |
| `%permission%` | Permissão do kit |
| `%items%` | Número de itens |
| `%status%` | Estado do kit para o jogador |
| `%remaining%` | Tempo restante de cooldown |
| `%page%` / `%pages%` | Página atual e total |

---

## 🛠️ Requisitos

- **Servidor:** Paper 1.21.11+ (Java 21)

## 🔨 Compilar

Requer Java 21 e Maven:

```
mvn package
```

O `.jar` fica em `target/pKits.jar`. Coloca-o na pasta `plugins` do servidor e reinicia.

---

<p align="center">
  Desenvolvido por <b>PORTUGAS</b> 🇵🇹
</p>
