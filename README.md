# 🧮 AEDS III - CalculaFast

> Trabalho da disciplina **Algoritmos e Estruturas de Dados III**

---

## 📘 Descrição do Projeto
O **CalculaFast** é um projeto desenvolvido para a disciplina de AEDS III, com foco em manipulação de dados utilizando **arquivos de bytes** ao invés de bancos de dados relacionais.  
O sistema realiza operações de **CRUD (Create, Read, Update, Delete)** e simula funcionalidades avançadas como **exclusão lógica**, **índices** e **relacionamentos 1:N e N:N**.
---
## 🧩 Instruções
<pre> 
compilar o Principal.java. 
seguir instruções do terminal. 
digitar numero correspondente de cada entidade para realizae inserção, busca, exclusão ou atualização de algum dado. 
dados são acessados pelo seu id. 
"6 - Consultas bidirecionais" para consultas relacionadas a  tabela n:n
 </pre>
---

## 🧩 Questões do Trabalho

###  1.Qual foi o relacionamento N:N escolhido e quais tabelas ele conecta?
Foi escolhido o relacionamento n:n de pessoa, comanda e itens, que utiliza a quantidade e mapeia o valor unitário das tabelas.


### 2. Qual estrutura de índice foi utilizada (B+ ou Hash Extensível)? Justifique a escolha.
Hash Extensível. A hash apresenta uma melhor perfomace para busca exatas de chaves primárias

### 3. Como foi implementada a chave composta da tabela intermediária?
combinação ods tres campos idPessoa, idComanda e idItem, tem a função propria Math.abs((idPessoa * 31) + (idComanda * 17) + (idItem * 7)). Adicionamos o arquivo ChaveCompostaPCI que armazena as tres chaves a o offset.
Antes de inserir qualquer novo objeto, é analisado se já tem o registro com a mesma chave


### 4. Como é feita a busca eficiente de registros por meio do índice?
<pre> // cria a hash da chave composta
int hash = Math.abs((idPessoa * 31) + (idComanda * 17) + (idItem * 7));

// busca no indice hash extensível
ChaveCompostaPCI chave = hash.read(hash);

// recupera os artigos usando o offset
if (chave != null) {
    return arquivoPrincipal.read(chave.getOffset());
}

 </pre>

### 5. Como o sistema trata a integridade referencial (remoção/atualização) entre as tabelas?
Na inserção vai verificar se ja existe um registro com a mesma chave e lança uma exceção em caso de duplicidade.
Na atualização a chave composta não pode ser mudada apenas os campos que não são chaves.
Na exclusão, remove do arquivo principal e do indice, se falhar um, não completa o outro


### 6. Como foi organizada a persistência dos dados dessa nova tabela (mesmo padrão de cabeçalho e lápide)?
Cabeçalho: 4 bytes para último ID + 8 bytes para lista de excluídos
Lápide: Byte ' ' para ativo, '*' para excluído
Tamanho fixo: 24 bytes por registro (3 int + 1 int + 1 double)
Armazenamento em: ./dados/Pessoa_Comanda_Item/Pessoa_Comanda_Item.db


### 7. Descreva como o código da tabela intermediária se integra com o CRUD das tabelas principais.
Nos DAOs principais tem a referencia ao DAO da tabela intermediária pessoa_comanda_item.

### 8. Descreva como está organizada a estrutura de diretórios e módulos no repositório após esta fase.

```html

📁 src/
├── 📁 java/
│   ├── 📁 model/
│   │   ├── Registro.java
│   │   ├── Arquivo.java
│   │   ├── Pessoa.java
│   │   ├── Comanda.java
│   │   ├── Item.java
│   │   └── Pessoa_Comanda_Item.java 
│   ├── 📁 dao/
│   │   ├── PessoaDAO.java
│   │   ├── ComandaDAO.java
│   │   ├── ItemDAO.java
│   │   └── Pessoa_Comanda_ItemDAO.java 
│   ├── 📁 menu/
│   │   ├── MenuPessoa.java
│   │   ├── MenuComanda.java
│   │   ├── MenuItem.java
│   │   └── MenuPessoa_Comanda_Item.java 
│   │   └── MenuConsultas.java 
│   └── 📁 index/hash/
│       ├── RegistroHashExtensivel.java
│       ├── HashExtensivel.java
│       ├── ChaveCompostaPCI.java
│       └── IndiceChaveComposta.java 
├── 📁 dados/ (gerado automaticamente)
│   ├── 📁 Pessoa/
│   ├── 📁 Comanda/
│   ├── 📁 Item/
│   └── 📁 Pessoa_Comanda_Item/ 
└── 📁 indices/  (gerado automaticamente)
    ├── indice_chave_composta_dir.db 
    ├── indice_chave_composta_buckets.db 
    └── ...

