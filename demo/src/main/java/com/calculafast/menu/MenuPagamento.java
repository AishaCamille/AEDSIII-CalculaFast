package com.calculafast.menu;
import com.calculafast.dao.PagamentoDAO;
import java.util.Scanner;
import com.calculafast.model.Pagamento;

public class MenuPagamento {
    private PagamentoDAO pagamentoDAO;
    private Scanner console = new Scanner(System.in);

    public MenuPagamento() throws Exception {
        pagamentoDAO = new PagamentoDAO();
    }

    public void menu() {
        int opcao;
        do {
            System.out.println("\n\nAEDsIII");
            System.out.println("-------");
            System.out.println("> Início > Pagamentos");
            System.out.println("\n1 - Buscar");
            System.out.println("2 - Incluir");
            System.out.println("3 - Alterar");
            System.out.println("4 - Excluir");
            System.out.println("5 - Listar por Comanda (1:N)");
            System.out.println("6 - Buscar por Intervalo de Valor"); // NOVO
            System.out.println("0 - Voltar");

            System.out.print("\nOpção: ");
            try {
                opcao = Integer.valueOf(console.nextLine());
            } catch(NumberFormatException e) {
                opcao = -1;
            }

            switch (opcao) {
                case 1:
                    buscarPagamento();
                    break;
                case 2:
                    incluirPagamento();
                    break;
                case 3:
                    alterarPagamento();
                    break;
                case 4:
                    excluirPagamento();
                    break;
                case 5:
                    listarPorComanda();
                    break;
                case 6:
                    buscarPorIntervaloValor(); 
                    break;
                case 0:
                    break;
                default:
                    System.out.println("Opção inválida!");
                    break;
            }
        } while (opcao != 0);
    }

    private void buscarPagamento() {
        System.out.print("\nID do pagamento: ");
        int id = console.nextInt();
        console.nextLine();
        try {
            Pagamento pagamento = pagamentoDAO.buscarPagamento(id);
            if (pagamento != null) {
                System.out.println(pagamento);
            } else {
                System.out.println("Pagamento não encontrado.");
            }
        } catch (Exception e) {
            System.out.println("Erro ao buscar pagamento.");
        }
    }

    private void incluirPagamento() {
        System.out.println("\nInclusão de pagamento");

        System.out.print("\nValor pago: ");
        double valorPago = console.nextDouble();
        console.nextLine();
        System.out.print("Valor com desconto do garçom: ");
        double valorComDesconto = console.nextDouble();
        console.nextLine();
        System.out.print("id pessoa: ");
        int idPessoa = console.nextInt();
        console.nextLine();
        System.out.print("id comanda: ");
        int idComanda = console.nextInt();
        console.nextLine();

        try {
            Pagamento pagamento = new Pagamento(valorPago, valorComDesconto, idPessoa, idComanda);
            if (pagamentoDAO.incluirPagamento(pagamento)) {
              
                System.out.println("Pagamento incluído com sucesso.");
            System.out.println(pagamento.toString());
            } else {
                System.out.println("Erro ao incluir pagamento.");
            }
        } catch (Exception e) {
            System.out.println("Erro ao incluir pagamento.");
        }
    }

    private void alterarPagamento() {
        System.out.print("\nID do pagamento a ser alterado: ");
        int id = console.nextInt();
        console.nextLine();

        try {
            Pagamento pagamento = pagamentoDAO.buscarPagamento(id);
            if (pagamento == null) {
                System.out.println("Pagamento não encontrado.");
                return;
            }
            
            System.out.print("Novo valor pago (vazio para manter): ");
            String valorPagoStr = console.nextLine();
            if (!valorPagoStr.isEmpty()) 
                pagamento.setValorPago(Double.parseDouble(valorPagoStr));

            System.out.print("Novo valor com desconto (vazio para manter): ");
            String valorComDescontoStr = console.nextLine();
            if (!valorComDescontoStr.isEmpty()) 
                pagamento.setValorComDesconto(Double.parseDouble(valorComDescontoStr));

            System.out.print("Novo id de pessoa (vazio para manter): ");
            String idPessoaStr = console.nextLine();
            if (!idPessoaStr.isEmpty()) 
                pagamento.setIdPessoa(Integer.parseInt(idPessoaStr));

            System.out.print("Novo id de comanda (vazio para manter): ");
            String idComandaStr = console.nextLine();
            if (!idComandaStr.isEmpty()) 
                pagamento.setIdComanda(Integer.parseInt(idComandaStr));

            if (pagamentoDAO.alterarPagamento(pagamento)) {
                System.out.println("Pagamento alterado com sucesso.");
            } else {
                System.out.println("Erro ao alterar pagamento.");
            }
        } catch (Exception e) {
            System.out.println("Erro ao alterar pagamento.");
            e.printStackTrace();
        }
    }

    private void excluirPagamento() {
        System.out.print("\nID do pagamento a ser excluído: ");
        int id = console.nextInt();
        console.nextLine();

        try {
            Pagamento pagamento = pagamentoDAO.buscarPagamento(id);
            if (pagamento == null) {
                System.out.println("Pagamento não encontrado.");
                return;
            }

            System.out.print("Confirma exclusão? (S/N): ");
            char resp = console.next().charAt(0);
            console.nextLine();
            
            if (resp == 'S' || resp == 's') {
                if (pagamentoDAO.excluirPagamento(id)) {
                    System.out.println("Pagamento excluído com sucesso.");
                } else {
                    System.out.println("Erro ao excluir pagamento.");
                }
            }
        } catch (Exception e) {
            System.out.println("Erro ao excluir pagamento.");
        }
    }

    private void listarPorComanda() {
        System.out.print("\nID da comanda: ");
        int id = console.nextInt();
        console.nextLine();
        try {
            java.util.List<Integer> ids = pagamentoDAO.listarPorComanda(id);
            if (ids.isEmpty()) {
                System.out.println("Sem pagamentos para a comanda " + id);
            } else {
                System.out.println("\nPagamentos da comanda " + id + ":");
                System.out.println("Total: " + ids.size() + " pagamento(s)");
                System.out.println("IDs: " + ids);
                
                // Mostra detalhes de cada pagamento
                System.out.println("\nDetalhes:");
                for (int pagId : ids) {
                    Pagamento p = pagamentoDAO.buscarPagamento(pagId);
                    if (p != null) {
                        System.out.println("  - " + p);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Erro ao listar por comanda.");
            e.printStackTrace();
        }
    }

    // busca por intervalo de valores usando B+ Tree
    private void buscarPorIntervaloValor() {
        System.out.println("\nBuscar pagamentos por intervalo de valor");
        
        System.out.print("Valor mínimo: R$ ");
        double valorMin = console.nextDouble();
        console.nextLine();
        
        System.out.print("Valor máximo: R$ ");
        double valorMax = console.nextDouble();
        console.nextLine();
        
        try {
            java.util.List<Pagamento> pagamentos = pagamentoDAO.buscarPorIntervaloValor(valorMin, valorMax);
            
            if (pagamentos.isEmpty()) {
                System.out.println("\nNenhum pagamento encontrado no intervalo R$ " 
                    + String.format("%.2f", valorMin) + " - R$ " 
                    + String.format("%.2f", valorMax));
            } else {
                System.out.println("\nPagamentos encontrados: " + pagamentos.size());
                System.out.println("\nDetalhes:");
                for (Pagamento p : pagamentos) {
                    System.out.println("  - " + p);
                }
            }
        } catch (Exception e) {
            System.out.println("Erro ao buscar por intervalo de valor.");
            e.printStackTrace();
        }
    }
}