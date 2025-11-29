package com.calculafast.menu;
import com.calculafast.dao.Pessoa_Comanda_ItemDAO;
import java.util.*;
import com.calculafast.model.Pessoa_Comanda_Item;


public class MenuPessoa_Comanda_Item {
    private Pessoa_Comanda_ItemDAO pessoa_comanda_itemDAO;
    private Scanner console = new Scanner(System.in);

    public MenuPessoa_Comanda_Item() throws Exception {
        pessoa_comanda_itemDAO = new Pessoa_Comanda_ItemDAO();
    }

    public void menu() {
        int opcao;
        do {
            System.out.println("\n\nMenu Pessoa-Comanda-Item");
            System.out.println("1 - Buscar por Chave Composta");
            System.out.println("2 - Incluir");
            System.out.println("3 - Alterar");
            System.out.println("4 - Excluir");
            System.out.println("5 - Consultar Itens de uma Pessoa");     
            System.out.println("6 - Consultar Pessoas de um Item");      
            System.out.println("7 - Consultar Itens de uma Comanda");
            System.out.println("0 - Voltar");

            System.out.print("\nOpção: ");
            try {
                opcao = Integer.valueOf(console.nextLine());
            } catch(NumberFormatException e) {
                opcao = -1;
            }

            switch (opcao) {
                case 1:
                    buscarPorChaveComposta();
                    break;
                case 2:
                    incluirPessoa_Comanda_Item();
                    break;
                case 3:
                    alterarPessoa_Comanda_Item();
                    break;
                case 4:
                    excluirPessoa_Comanda_Item();
                    break;
                  case 5: 
                    consultarItensDaPessoa();
                    break;
                case 6: 
                    consultarPessoasDoItem();
                    break;
                case 7: 
                    consultarItensDaComanda();
                    break;
                case 0:
                    break;
                default:
                    System.out.println("Opção inválida!");
                    break;
            }
        } while (opcao != 0);
    }

    private void buscarPorChaveComposta() {
        System.out.println("\nBuscar por Chave Composta");
        
        System.out.print("ID Pessoa: ");
        int idPessoa = console.nextInt();
        console.nextLine();
        
        System.out.print("ID Comanda: ");
        int idComanda = console.nextInt();
        console.nextLine();
        
        System.out.print("ID Item: ");
        int idItem = console.nextInt();
        console.nextLine();
        
        try {
            Pessoa_Comanda_Item pci = pessoa_comanda_itemDAO.buscarPorChaveComposta(idPessoa, idComanda, idItem);
            if (pci != null) {
                System.out.println(pci);
            } else {
                System.out.println("Registro não encontrado.");
            }
        } catch (Exception e) {
            System.out.println("Erro na busca: " + e.getMessage());
        }
    }

    private void incluirPessoa_Comanda_Item() {
        System.out.println("\nInclusão de Pessoa-Comanda-Item");
        
        System.out.print("ID Pessoa: ");
        int idPessoa = console.nextInt();
        console.nextLine();
        
        System.out.print("ID Comanda: ");
        int idComanda = console.nextInt();
        console.nextLine();
        
        System.out.print("ID Item: ");
        int idItem = console.nextInt();
        console.nextLine();
        
        System.out.print("Quantidade: ");
        int quantidade = console.nextInt();
        console.nextLine();
        
        System.out.print("Valor Unitário: ");
        double valorUnitario = console.nextDouble();
        console.nextLine();
       
        try {
            Pessoa_Comanda_Item pci = new Pessoa_Comanda_Item(idPessoa, idComanda, idItem, quantidade, valorUnitario);
            if (pessoa_comanda_itemDAO.incluirPessoa_Comanda_Item(pci)) {
                System.out.println("Registro incluído com sucesso!");
            } else {
                System.out.println("Erro ao incluir registro.");
            }
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    private void alterarPessoa_Comanda_Item() {
        System.out.println("\nAlterar Registro");
        
        System.out.print("ID Pessoa: ");
        int idPessoa = console.nextInt();
        console.nextLine();
        
        System.out.print("ID Comanda: ");
        int idComanda = console.nextInt();
        console.nextLine();
        
        System.out.print("ID Item: ");
        int idItem = console.nextInt();
        console.nextLine();

        try {
            Pessoa_Comanda_Item pci = pessoa_comanda_itemDAO.buscarPorChaveComposta(idPessoa, idComanda, idItem);
            if (pci == null) {
                System.out.println("Registro não encontrado.");
                return;
            }
            
            System.out.println("Dados atuais:");
            System.out.println(pci);
            System.out.println("\nNovos valores:");

            System.out.print("Nova Quantidade [" + pci.getQuantidade() + "]: ");
            String qtdStr = console.nextLine();
            if (!qtdStr.isEmpty()) pci.setQuantidade(Integer.parseInt(qtdStr));

            System.out.print("Novo Valor Unitário [" + pci.getValorUnitario() + "]: ");
            String valorStr = console.nextLine();
            if (!valorStr.isEmpty()) pci.setValorUnitario(Double.parseDouble(valorStr));

            if (pessoa_comanda_itemDAO.alterarPessoa_Comanda_Item(pci)) {
                System.out.println("Registro alterado com sucesso!");
            } else {
                System.out.println("Erro ao alterar registro.");
            }
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    private void excluirPessoa_Comanda_Item() {
        System.out.println("\nExcluir Registro");
        
        System.out.print("ID Pessoa: ");
        int idPessoa = console.nextInt();
        console.nextLine();
        
        System.out.print("ID Comanda: ");
        int idComanda = console.nextInt();
        console.nextLine();
        
        System.out.print("ID Item: ");
        int idItem = console.nextInt();
        console.nextLine();

        try {
            Pessoa_Comanda_Item pci = pessoa_comanda_itemDAO.buscarPorChaveComposta(idPessoa, idComanda, idItem);
            if (pci == null) {
                System.out.println("Registro não encontrado.");
                return;
            }

            System.out.println("Registro a excluir:");
            System.out.println(pci);
            
            System.out.print("Confirma exclusão? (S/N): ");
            char resp = console.next().charAt(0);
            console.nextLine();
            
            if (resp == 'S' || resp == 's') {
                if (pessoa_comanda_itemDAO.excluirPessoa_Comanda_Item(idPessoa, idComanda, idItem)) {
                    System.out.println("Registro excluído com sucesso!");
                } else {
                    System.out.println("Erro ao excluir registro.");
                }
            } else {
                System.out.println("Exclusão cancelada.");
            }
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    public void fechar() {
        try {
            pessoa_comanda_itemDAO.fechar();
        } catch (Exception e) {
            System.out.println("Erro ao fechar DAO: " + e.getMessage());
        }
    }


    //consultas de pessoa, item e comandas
      private void consultarItensDaPessoa() {
        System.out.print("\nID da Pessoa: ");
        int idPessoa = console.nextInt();
        console.nextLine();
        
        try {
            List<Pessoa_Comanda_Item> relacoes = pessoa_comanda_itemDAO.buscarPorPessoa(idPessoa);
            if (relacoes.isEmpty()) {
                System.out.println("Nenhum item encontrado para esta pessoa.");
            } else {
                System.out.println("\n=== Itens da Pessoa " + idPessoa + " ===");
                for (Pessoa_Comanda_Item relacao : relacoes) {
                    System.out.println(relacao);
                    System.out.println("---");
                }
                System.out.println("Total: " + relacoes.size() + " relação(ões)");
            }
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    private void consultarPessoasDoItem() {
        System.out.print("\nID do Item: ");
        int idItem = console.nextInt();
        console.nextLine();
        
        try {
            List<Pessoa_Comanda_Item> relacoes = pessoa_comanda_itemDAO.buscarPorItem(idItem);
            if (relacoes.isEmpty()) {
                System.out.println("Nenhuma pessoa encontrada para este item.");
            } else {
                System.out.println("\n=== Pessoas que compraram o Item " + idItem + " ===");
                for (Pessoa_Comanda_Item relacao : relacoes) {
                    System.out.println(relacao);
                    System.out.println("---");
                }
                System.out.println("Total: " + relacoes.size() + " relação(ões)");
            }
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    private void consultarItensDaComanda() {
        System.out.print("\nID da Comanda: ");
        int idComanda = console.nextInt();
        console.nextLine();
        
        try {
            List<Pessoa_Comanda_Item> relacoes = pessoa_comanda_itemDAO.buscarPorComanda(idComanda);
            if (relacoes.isEmpty()) {
                System.out.println("Nenhum item encontrado para esta comanda.");
            } else {
                System.out.println("\n=== Itens da Comanda " + idComanda + " ===");
                for (Pessoa_Comanda_Item relacao : relacoes) {
                    System.out.println(relacao);
                    System.out.println("---");
                }
                System.out.println("Total: " + relacoes.size() + " relação(ões)");
            }
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }
}