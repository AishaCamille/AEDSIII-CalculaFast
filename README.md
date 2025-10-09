# 🧮 AEDS III - CalculaFast

> Trabalho da disciplina **Algoritmos e Estruturas de Dados III**

---

## 📘 Descrição do Projeto
O **CalculaFast** é um projeto desenvolvido para a disciplina de AEDS III, com foco em manipulação de dados utilizando **arquivos de bytes** ao invés de bancos de dados relacionais.  
O sistema realiza operações de **CRUD (Create, Read, Update, Delete)** e simula funcionalidades avançadas como **exclusão lógica**, **índices** e **relacionamentos 1:N**.

---

## 🧩 Questões do Trabalho

### a) Qual a estrutura usada para representar os registros?
Os registros foram representados em arquivos binários, seguindo um formato fixo para cada campo. Cada tabela possui um arquivo .db contendo os registros serializados. Cada registro inclui um cabeçalho com informações de controle, como o número total de registros e o último ID utilizado.

---

### b) Como atributos multivalorados do tipo string foram tratados?
Os atributos multivalorados do tipo string foram armazenados como strings separadas por delimitadores de quantidade de caracteres da string.

---

### c) Como foi implementada a exclusão lógica?
A exclusão lógica foi implementada como um valor booleano onde “ “ significa que o registro está ativo e “*” quando o registro está excluído.

---

### d) Além das PKs, quais outras chaves foram utilizadas nesta etapa?


---

### e) Quais tipos de estruturas (hash, B+ Tree, extensível, etc.) foram utilizadas para cada chave de pesquisa?



---

### f) Como foi implementado o relacionamento 1:N (explique a lógica da navegação entre registros e integridade referencial)?


---

### g) Como os índices são persistidos em disco? (formato, atualização, sincronização com os dados)
Cada índice é armazenado em arquivos binários separados, mantendo sincronização automática a cada operação de inserção, atualização ou exclusão.

---

### h)  Como está estruturado o projeto no GitHub (pastas, módulos, arquitetura)?
📂 CalculaFast
┣ 📂 src/
  ┣ 📂 java/
    ┣ 📂 app/
      📝Principal
    ┣ 📂 dados/
      arquivos.db
    ┣ 📂 dao/
      arquivos tipo dao
    ┣ 📂 menu/
      menu de navegação
    ┣ 📂 model/
      classes das entidades


