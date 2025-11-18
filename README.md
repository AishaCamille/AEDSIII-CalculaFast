# 🧮 AEDS III - CalculaFast

> Trabalho da disciplina **Algoritmos e Estruturas de Dados III**

---

## 📘 Descrição do Projeto
O **CalculaFast** é um projeto desenvolvido para a disciplina de AEDS III, com foco em manipulação de dados utilizando **arquivos de bytes** ao invés de bancos de dados relacionais.  
O sistema realiza operações de **CRUD (Create, Read, Update, Delete)** e simula funcionalidades avançadas como **exclusão lógica**, **índices** e **relacionamentos 1:N**.

---
## 📘 Como Utilizar

compilar e executar o principal.java, navegação pelo terminal atraves dos menus indicando com número o que deseja realizar na aplicação

---

### h)  Como está estruturado o projeto no GitHub (pastas, módulos, arquitetura)?
```text
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
