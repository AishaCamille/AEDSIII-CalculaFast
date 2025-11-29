package com.calculafast.menu;
import com.calculafast.dao.PessoaDAO;
import java.util.Scanner;
import com.calculafast.model.Pessoa;

public class MenuPessoas {
    private PessoaDAO pessoaDAO;
    private Scanner console= new Scanner(System.in);

    public MenuPessoas() throws Exception {
        pessoaDAO= new PessoaDAO();
    }

    public void menu(){
         int opcao;
        do {
            System.out.println("\n\nAEDsIII");
            System.out.println("-------");
            System.out.println("> Início > Pessoas");
            System.out.println("\n1 - Buscar");
            System.out.println("2 - Incluir");
            System.out.println("3 - Alterar");
            System.out.println("4 - Excluir");
            System.out.println("0 - Voltar");

            System.out.print("\nOpção: ");
            try {
                opcao = Integer.valueOf(console.nextLine());
            } catch(NumberFormatException e) {
                opcao = -1;
            }

            switch (opcao) {
                case 1:
                    buscarPessoa();
                    break;
                case 2:
                    incluirPessoa();
                    break;
                case 3:
                    alterarPessoa();
                    break;
                case 4:
                    excluirPessoa();
                    break;
                case 0:
                    break;
                default:
                    System.out.println("Opção inválida!");
                    break;
            }
        } while (opcao != 0);
    }

    private void buscarPessoa() {
        System.out.print("\nID da pessoa: ");
        int id = console.nextInt();
        console.nextLine();
        try {
            Pessoa pessoa = pessoaDAO.buscarPessoa(id);
            if (pessoa != null) {
                System.out.println(pessoa);
            } else {
                System.out.println("Pessoa não encontrado.");
            }
        } catch (Exception e) {
            System.out.println("Erro ao buscar pessoa.");
        }
    }

    private void incluirPessoa() {
        System.out.println("\nInclusão de pessoa");

      

        System.out.print("Nome: ");
        String nome = console.nextLine();
        System.out.print("email: ");
        String email = console.nextLine();
        System.out.print("senha: ");
        String senha = console.nextLine();
        try {
        Pessoa pessoa = new Pessoa(nome, email, senha);
        if (pessoaDAO.incluirPessoa(pessoa)) {
            System.out.println("Pessoa incluída com sucesso.");
            System.out.println(pessoa.toString());
        } else {
            System.out.println("Erro ao incluir pessoa. (Possível ID duplicado)");
        }
    } catch (IllegalArgumentException e) {
        // Captura erros de validação (como e-mail inválido)
        System.out.println("Erro de validação: " + e.getMessage());
    } catch (Exception e) {
        // Captura outros erros
        System.out.println("Erro ao incluir pessoa: " + e.getMessage());
        // Se quiser ver o erro completo para debug:
        // e.printStackTrace();
    }
    }

    private void alterarPessoa() {
        System.out.print("\nID da pessoa a ser alterada: ");
        String idStr = console.nextLine().trim();
        int id = 0;
        try { id = Integer.parseInt(idStr); } catch (NumberFormatException e) { id = -1; }
        if (id <= 0) {
            System.out.println("ID inválido.");
            return;
        }

        try {
            Pessoa pessoa = pessoaDAO.buscarPessoa(id);
            if (pessoa == null) {
                System.out.println("Pessoa não encontrado.");
                return;
            }

            System.out.print("\nNovo nome (vazio para manter): ");
            String nome = console.nextLine();
            if (!nome.isEmpty()) pessoa.setNome(nome);

            System.out.print("Novo email (vazio para manter): ");
            String email = console.nextLine();
            if (!email.isEmpty()) pessoa.setEmail(email);

            System.out.print("Nova senha (vazio para manter): ");
            String senha = console.nextLine();
            if (!senha.isEmpty()) pessoa.setSenha(senha);

           
            if (pessoaDAO.alterarPessoa(pessoa)) {
                System.out.println("Pessoa alterado com sucesso.");
            } else {
                System.out.println("Erro ao alterar pessoa.");
            }
        } catch (Exception e) {
            System.out.println("Erro ao alterar pessoa.");
        }
    }

    private void excluirPessoa() {
        System.out.print("\nID do pessoa a ser excluído: ");
        int id = console.nextInt();
        console.nextLine();

        try {
            Pessoa pessoa = pessoaDAO.buscarPessoa(id);
            if (pessoa == null) {
                System.out.println("Pessoa não encontrado.");
                return;
            }

            System.out.print("Confirma exclusão? (S/N): ");
            char resp = console.next().charAt(0);
            if (resp == 'S' || resp == 's') {
                if (pessoaDAO.excluirPessoa(id)) {
                    System.out.println("Pessoa excluído com sucesso.");
                } else {
                    System.out.println("Erro ao excluir pessoa.");
                }
            }
        } catch (Exception e) {
            System.out.println("Erro ao excluir pessoa.");
        }
    }
}
