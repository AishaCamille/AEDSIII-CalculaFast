
# 🧮 AEDS III - CalculaFast

> Trabalho da disciplina **Algoritmos e Estruturas de Dados III**

---

## 📘 Descrição do Projeto
O **CalculaFast** é um projeto desenvolvido para a disciplina de AEDS III, com foco em manipulação de dados utilizando **arquivos de bytes** ao invés de bancos de dados relacionais.  
O sistema realiza operações de **CRUD (Create, Read, Update, Delete)** e simula funcionalidades avançadas como **exclusão lógica**, **índices** e **relacionamentos 1:N**.

---
## 📘 Como Utilizar

compilar e executar **Aplicacao.java**, iniciar pág web pelo **index.html**, localizado em **resource/public/index.html**
Página produtos.html há a demonstração da relação n:n de pessoa, comanda e item.

Página produtos.html está com o front utilizando o algoritmo de casamento de padroes.

Página segundaPagina.html há a demonstração da relação 1:n de pagamento e comanda

---

### h)  Como está estruturado o projeto no GitHub (pastas, módulos, arquitetura)?
```text
calculafast/
├── pom.xml
├── README.md
└── src/
    ├── main/
    │   ├── java/
    │   │   └── com/
    │   │       └── calculafast/
    │   │           ├── app/
    │   │           │   └── Aplicacao.java
    │   │           │   └── Principal.java (para debug)
    │   │           │
    │   │           ├── dao/
    │   │           │   ├── PessoaDAO.java
    │   │           │   ├── PessoaComandaDAO.java
    │   │           │   ├── ComandaDAO.java
    │   │           │   ├── ItemDAO.java
    │   │           │   ├── PagamentoDAO.java
    │   │           │   └── Pessoa_Comanda_ItemDAO.java
    │   │           │
    │   │           ├── model/
    │   │           │   ├── Pessoa.java
    │   │           │   ├── PessoaComanda.java
    │   │           │   ├── Comanda.java
    │   │           │   ├── Item.java
    │   │           │   ├── Pagamento.java
    │   │           │   └── Pessoa_Comanda_Item.java
    │   │           │
    │   │           ├── menu/   (utilizado paga debug)
    │   │           │   ├── MenuPessoas.java
    │   │           │   ├── MenuPessoaComanda.java
    │   │           │   ├── MenuComanda.java
    │   │           │   ├── MenuItem.java
    │   │           │   ├── MenuPagamento.java
    │   │           │   |── MenuConsultas.java
    |   |           |   └── MenuPessoa_Comanda_Item.java
    │   │           │
    │   │           ├── index/   
    │   │           │   ├── bptree/
    │   │           │   ├── hash/
    │   │           │   └── inverted/
    │   │           │
    │   │           └── seguranca/
    │   │               └── RSA.java
    │   │           │
    │   │           └── casamentoDePadroes/
    │   │           │   ├── BoyerMoore.java
    │   │               └── KMP.java
    │   │
    │   ├── resources/
    │   │     └── public/    → arquivos front-end 
    │   │           ├── index.html
    │   │           ├── style.css
    │   │           └── script.js
    │   └── dados/                (arquivos binários .db)
    │       ├── pessoas/
    │       ├── pessoasComanda/
    │       ├── comanda/
    │       ├── item/
    │       └── pagamento/
    │        └── pessoa_comanda_item/
    │
    └── test/
        └── java/
            └── com/
                └── calculafast/
                    └── AppTest.java

   
