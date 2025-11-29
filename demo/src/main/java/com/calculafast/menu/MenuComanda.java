package com.calculafast.menu;
import com.calculafast.model.Comanda;
import com.calculafast.dao.ComandaDAO;
import java.util.Scanner;

public class MenuComanda {
     private ComandaDAO comandaDAO;
    private Scanner console = new Scanner(System.in);

    public MenuComanda() throws Exception {
        comandaDAO = new ComandaDAO();
    }

    public void menu() {
        int opcao;
        do {
            System.out.println("\n\nAEDsIII");
            System.out.println("-------");
            System.out.println("> Início > Comandas");
            System.out.println("\n1 - Buscar");
            System.out.println("2 - Incluir");
            System.out.println("3 - Alterar");
            System.out.println("4 - Excluir");
            System.out.println("5 - Listar por Pessoa (1:N)");
            System.out.println("0 - Voltar");

            System.out.print("\nOpção: ");
            try {
                opcao = Integer.valueOf(console.nextLine());
            } catch(NumberFormatException e) {
                opcao = -1;
            }

            switch (opcao) {
                case 1:
                    buscarComanda();
                    break;
                case 2:
                    incluirComanda();
                    break;
                case 3:
                    alterarComanda();
                    break;
                case 4:
                    excluirComanda();
                    break;
                case 5:
                    listarPorPessoa();
                    break;
                case 6:
                    listarPessoasDaComanda();
                    break;
                case 0:
                    break;
                default:
                    System.out.println("Opção inválida!");
                    break;
            }
        } while (opcao != 0);
    }

    private void buscarComanda() {
        System.out.print("\nID do comanda: ");
        String idStr = console.nextLine().trim();
        int id;
        try { id = Integer.parseInt(idStr); } catch (NumberFormatException e) { System.out.println("ID inválido."); return; }
        try {
            Comanda comanda = comandaDAO.buscarComanda(id);
            if (comanda != null) {
                System.out.println(comanda);
            } else {
                System.out.println("Comanda não encontrado.");
            }
        } catch (Exception e) {
            System.out.println("Erro ao buscar comanda.");
        }
    }

    private void incluirComanda() {
        System.out.println("\nInclusão de comanda");

        System.out.print("\nId Pessoa: ");
        String idPessoaStr = console.nextLine().trim();
        int idPessoa;
        try { idPessoa = Integer.parseInt(idPessoaStr); } catch (NumberFormatException e) { System.out.println("ID de pessoa inválido."); return; }
        System.out.print("status: ");
        String status = console.nextLine();
        System.out.print("Consumo pessoa: ");
        String consumoStr = console.nextLine().trim();
        double consumoPessoa;
        try { consumoPessoa = Double.parseDouble(consumoStr); } catch (NumberFormatException e) { System.out.println("Consumo inválido."); return; }
       
        try {
            Comanda comanda = new Comanda(idPessoa, status, consumoPessoa);
            if (comandaDAO.incluirComanda(comanda)) {
                System.out.println("Comanda incluído com sucesso.");
                System.out.println(comanda.toString());
            } else {
                System.out.println("Erro ao incluir comanda.");
            }
        } catch (Exception e) {
            System.out.println("Erro ao incluir comanda.");
        }
    }

    private void alterarComanda() {
        System.out.print("\nID do comanda a ser alterado: ");
        String idStr = console.nextLine().trim();
        int id;
        try { id = Integer.parseInt(idStr); } catch (NumberFormatException e) { System.out.println("ID inválido."); return; }

        try {
            Comanda comanda = comandaDAO.buscarComanda(id);
            if (comanda == null) {
                System.out.println("Comanda não encontrado.");
                return;
            }

            System.out.print("\nNovo id da Pessoa (vazio para manter): ");
            String idPessoaStr = console.nextLine().trim();
            if (!idPessoaStr.isEmpty()) {
                try {
                    int novoIdPessoa = Integer.parseInt(idPessoaStr);
                    if (novoIdPessoa > 0) comanda.setIdPessoa(novoIdPessoa);
                } catch (NumberFormatException ignored) {}
            }

            System.out.print("Novo status (vazio para manter): ");
            String status = console.nextLine();
            if (!status.isEmpty()) comanda.setStatus(status);

            System.out.print("Novo consumo pessoa (vazio para manter): ");
            String consumoPessoaStr = console.nextLine().trim();
            if (!consumoPessoaStr.isEmpty()) {
                try {
                    double novoConsumo = Double.parseDouble(consumoPessoaStr);
                    comanda.setConsumoPessoa(novoConsumo);
                } catch (NumberFormatException ignored) {}
            }

          
            if (comandaDAO.alterarComanda(comanda)) {
                System.out.println("Comanda alterado com sucesso.");
            } else {
                System.out.println("Erro ao alterar comanda.");
            }
        } catch (Exception e) {
            System.out.println("Erro ao alterar comanda.");
        }
    }

    private void excluirComanda() {
        System.out.print("\nID do comanda a ser excluído: ");
        String idStr = console.nextLine().trim();
        int id;
        try { id = Integer.parseInt(idStr); } catch (NumberFormatException e) { System.out.println("ID inválido."); return; }

        try {
            Comanda comanda = comandaDAO.buscarComanda(id);
            if (comanda == null) {
                System.out.println("Comanda não encontrado.");
                return;
            }

            System.out.print("Confirma exclusão? (S/N): ");
            char resp = console.next().charAt(0);
            if (resp == 'S' || resp == 's') {
                if (comandaDAO.excluirComanda(id)) {
                    System.out.println("Comanda excluído com sucesso.");
                } else {
                    System.out.println("Erro ao excluir comanda.");
                }
            }
        } catch (Exception e) {
            System.out.println("Erro ao excluir comanda.");
        }
    }

    private void listarPorPessoa() {
        System.out.print("\nID da pessoa: ");
        String idStr = console.nextLine().trim();
        int idPessoa;
        try { idPessoa = Integer.parseInt(idStr); } catch (NumberFormatException e) { System.out.println("ID inválido."); return; }
        try {
            java.util.List<Integer> ids = comandaDAO.listarPorPessoa(idPessoa);
            if (ids.isEmpty()) {
                System.out.println("Sem comandas para a pessoa " + idPessoa);
            } else {
                System.out.println("Comandas da pessoa " + idPessoa + ": " + ids);
            }
        } catch (Exception e) {
            System.out.println("Erro ao listar por pessoa.");
        }
    }

    private void listarPessoasDaComanda() {
        System.out.print("\nID da comanda: ");
        String idStr = console.nextLine().trim();
        int idComanda;
        try { idComanda = Integer.parseInt(idStr); } catch (NumberFormatException e) { 
            System.out.println("ID inválido."); 
            return; 
        }
        
        try {
            // Verifica se a comanda existe
            Comanda comanda = comandaDAO.buscarComanda(idComanda);
            if (comanda == null) {
                System.out.println("Comanda não encontrada.");
                return;
            }
            
            // Lista as pessoas da comanda usando o relacionamento 1:N
            java.util.List<Integer> pessoas = comandaDAO.listarPessoasPorComanda(idComanda);
            
            System.out.println("\n=== Pessoas da Comanda " + idComanda + " ===");
            if (pessoas.isEmpty()) {
                System.out.println("Nenhuma pessoa faz parte desta comanda.");
            } else {
                System.out.println("Pessoas que fazem parte da comanda:");
                for (Integer idPessoa : pessoas) {
                    System.out.println("  - Pessoa ID: " + idPessoa);
                }
                System.out.println("\nTotal: " + pessoas.size() + " pessoa(s)");
            }
        } catch (Exception e) {
            System.out.println("Erro ao listar pessoas da comanda: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
