package com.calculafast.menu;

import java.util.Scanner;

import com.calculafast.dao.PessoaComandaDAO;
import com.calculafast.model.PessoaComanda;

public class MenuPessoaComanda {
    private PessoaComandaDAO pessoaComandaDAO;
    private Scanner console = new Scanner(System.in);

    public MenuPessoaComanda() throws Exception {
        pessoaComandaDAO = new PessoaComandaDAO();
    }

    public void menu() {
        int opcao;
        do {
            System.out.println("\n\nAEDsIII");
            System.out.println("-------");
            System.out.println("> Início > Pessoas na Comanda");
            System.out.println("\n1 - Buscar");
            System.out.println("2 - Incluir");
            System.out.println("3 - Alterar");
            System.out.println("4 - Excluir");
            System.out.println("5 - Listar Todas");
            System.out.println("6 - Buscar por Comanda");
            System.out.println("0 - Voltar");

            System.out.print("\nOpção: ");
            try {
                opcao = Integer.valueOf(console.nextLine());
            } catch(NumberFormatException e) {
                opcao = -1;
            }

            switch (opcao) {
                case 1:
                    buscarPessoaComanda();
                    break;
                case 2:
                    incluirPessoaComanda();
                    break;
                case 3:
                    alterarPessoaComanda();
                    break;
                case 4:
                    excluirPessoaComanda();
                    break;
                case 5:
                    listarPessoasComanda();
                    break;
                case 6:
                    buscarPorComanda();
                    break;
                case 0:
                    break;
                default:
                    System.out.println("Opção inválida!");
                    break;
            }
        } while (opcao != 0);
    }

    private void buscarPessoaComanda() {
        System.out.print("\nID da Pessoa na Comanda: ");
        int id = console.nextInt();
        console.nextLine();
        try {
            PessoaComanda pessoaComanda = pessoaComandaDAO.buscar(id);
            if (pessoaComanda != null) {
                System.out.println(pessoaComanda);
            } else {
                System.out.println("Pessoa na Comanda não encontrada.");
            }
        } catch (Exception e) {
            System.out.println("Erro ao buscar pessoa na comanda: " + e.getMessage());
        }
    }

    private void incluirPessoaComanda() {
        System.out.println("\nInclusão de Pessoa na Comanda");

        System.out.print("\nNome: ");
        String nome = console.nextLine();
        
        System.out.print("ID da Comanda: ");
        int idComanda = console.nextInt();
        console.nextLine();
        
        System.out.print("Consumo Inicial: ");
        double consumoPessoa = console.nextDouble();
        console.nextLine();

        try {
            PessoaComanda pessoaComanda = new PessoaComanda();
            pessoaComanda.setNome(nome);
            pessoaComanda.setIdComanda(idComanda);
           // pessoaComanda.setConsumoPessoa(consumoPessoa);
            
            int idGerado = pessoaComandaDAO.incluir(pessoaComanda);
            if (idGerado > 0) {
                System.out.println("Pessoa incluída na comanda com sucesso. ID: " + idGerado);
            } else {
                System.out.println("Erro ao incluir pessoa na comanda.");
            }
        } catch (Exception e) {
            System.out.println("Erro ao incluir pessoa na comanda: " + e.getMessage());
        }
    }

    private void alterarPessoaComanda() {
        System.out.print("\nID da Pessoa na Comanda a ser alterada: ");
        int id = console.nextInt();
        console.nextLine();

        try {
            PessoaComanda pessoaComanda = pessoaComandaDAO.buscar(id);
            if (pessoaComanda == null) {
                System.out.println("Pessoa na Comanda não encontrada.");
                return;
            }

            System.out.println("Dados atuais:");
            System.out.println(pessoaComanda);
            
            System.out.print("\nNovo nome (vazio para manter '" + pessoaComanda.getNome() + "'): ");
            String nome = console.nextLine();
            if (!nome.isEmpty()) pessoaComanda.setNome(nome);

            System.out.print("Novo ID da Comanda (vazio para manter " + pessoaComanda.getIdComanda() + "): ");
            String idComandaStr = console.nextLine();
            if (!idComandaStr.isEmpty()) pessoaComanda.setIdComanda(Integer.parseInt(idComandaStr));

           

           
            if (pessoaComandaDAO.atualizar(pessoaComanda)) {
                System.out.println("Pessoa na Comanda alterada com sucesso.");
            } else {
                System.out.println("Erro ao alterar pessoa na comanda.");
            }
        } catch (Exception e) {
            System.out.println("Erro ao alterar pessoa na comanda: " + e.getMessage());
        }
    }

    private void excluirPessoaComanda() {
        System.out.print("\nID da Pessoa na Comanda a ser excluída: ");
        int id = console.nextInt();
        console.nextLine();

        try {
            PessoaComanda pessoaComanda = pessoaComandaDAO.buscar(id);
            if (pessoaComanda == null) {
                System.out.println("Pessoa na Comanda não encontrada.");
                return;
            }

            System.out.println("Dados da pessoa a ser excluída:");
            System.out.println(pessoaComanda);
            
            System.out.print("Confirma exclusão? (S/N): ");
            char resp = console.next().charAt(0);
            console.nextLine();
            
            if (resp == 'S' || resp == 's') {
                if (pessoaComandaDAO.excluir(id)) {
                    System.out.println("Pessoa na Comanda excluída com sucesso.");
                } else {
                    System.out.println("Erro ao excluir pessoa na comanda.");
                }
            } else {
                System.out.println("Exclusão cancelada.");
            }
        } catch (Exception e) {
            System.out.println("Erro ao excluir pessoa na comanda: " + e.getMessage());
        }
    }

    private void listarPessoasComanda() {
        try {
            var pessoasComanda = pessoaComandaDAO.listarPessoasComanda();
            if (pessoasComanda.isEmpty()) {
                System.out.println("Nenhuma pessoa encontrada nas comandas.");
            } else {
                System.out.println("\nLista de Pessoas nas Comandas:");
                for (PessoaComanda pc : pessoasComanda) {
                    System.out.println(pc);
                    System.out.println("---");
                }
            }
        } catch (Exception e) {
            System.out.println("Erro ao listar pessoas nas comandas: " + e.getMessage());
        }
    }

    private void buscarPorComanda() {
        System.out.print("\nID da Comanda: ");
        int idComanda = console.nextInt();
        console.nextLine();
        
        try {
            var pessoasComanda = pessoaComandaDAO.buscarPorComanda(idComanda);
            if (pessoasComanda.isEmpty()) {
                System.out.println("Nenhuma pessoa encontrada para esta comanda.");
            } else {
                System.out.println("\nPessoas na Comanda " + idComanda + ":");
                for (PessoaComanda pc : pessoasComanda) {
                    System.out.println(pc);
                    System.out.println("---");
                }
            }
        } catch (Exception e) {
            System.out.println("Erro ao buscar pessoas por comanda: " + e.getMessage());
        }
    }
}