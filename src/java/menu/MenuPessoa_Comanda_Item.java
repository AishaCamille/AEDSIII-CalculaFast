package menu;
import model.Pessoa_Comanda_Item;
import dao.Pessoa_Comanda_ItemDAO;
import java.util.Scanner;

public class MenuPessoa_Comanda_Item {
     private Pessoa_Comanda_ItemDAO pessoa_comanda_itemDAO;
    private Scanner console = new Scanner(System.in);

    public MenuPessoa_Comanda_Item() throws Exception {
        pessoa_comanda_itemDAO = new Pessoa_Comanda_ItemDAO();
    }

    public void menu() {
        int opcao;
        do {
            System.out.println("\n\nAEDsIII");
            System.out.println("-------");
            System.out.println("> Início > Pessoa_Comanda_Items");
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
                    buscarPessoa_Comanda_Item();
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
                case 0:
                    break;
                default:
                    System.out.println("Opção inválida!");
                    break;
            }
        } while (opcao != 0);
    }

    private void buscarPessoa_Comanda_Item() {
        System.out.print("\nID do pessoa_comanda_item: ");
        int id = console.nextInt();
        console.nextLine();
        try {
            Pessoa_Comanda_Item pessoa_comanda_item = pessoa_comanda_itemDAO.buscarPessoa_Comanda_Item(id);
            if (pessoa_comanda_item != null) {
                System.out.println(pessoa_comanda_item);
            } else {
                System.out.println("Pessoa_Comanda_Item não encontrado.");
            }
        } catch (Exception e) {
            System.out.println("Erro ao buscar pessoa_comanda_item.");
        }
    }

    private void incluirPessoa_Comanda_Item() {
        System.out.println("\nInclusão de pessoa_comanda_item");

        System.out.print("\nidPessoa: ");
        int idPessoa = console.nextInt();
        console.nextLine();
        System.out.print("IdItem: ");
        int idItem = console.nextInt();
        console.nextLine();
        System.out.print("idComanda ");
        int idComanda = console.nextInt();
        console.nextLine();
        System.out.print("quantidade ");
        int quantidade = console.nextInt();
        console.nextLine();
        System.out.print("Valor unitário ");
        double valorUnitario = console.nextDouble();
        console.nextLine();
       
        try {
            Pessoa_Comanda_Item pessoa_comanda_item = new Pessoa_Comanda_Item(idPessoa, idItem, idComanda, quantidade, valorUnitario);
            if (pessoa_comanda_itemDAO.incluirPessoa_Comanda_Item(pessoa_comanda_item)) {
                System.out.println("Pessoa_Comanda_Item incluído com sucesso.");
            } else {
                System.out.println("Erro ao incluir pessoa_comanda_item.");
            }
        } catch (Exception e) {
            System.out.println("Erro ao incluir pessoa_comanda_item.");
        }
    }

    private void alterarPessoa_Comanda_Item() {
        System.out.print("\nID do pessoa_comanda_item a ser alterado: ");
        int id = console.nextInt();
        console.nextLine();

        try {
            Pessoa_Comanda_Item pessoa_comanda_item = pessoa_comanda_itemDAO.buscarPessoa_Comanda_Item(id);
            if (pessoa_comanda_item == null) {
                System.out.println("Pessoa_Comanda_Item não encontrado.");
                return;
            }
          
            System.out.print("Novo id de pessoa (vazio para manter): ");
            String idPessoaStr = console.nextLine();
            if (!idPessoaStr.isEmpty()) pessoa_comanda_item.setIdPessoa(Integer.parseInt(idPessoaStr));

             System.out.print("Novo id de item (vazio para manter): ");
            String idItemStr = console.nextLine();
            if (!idItemStr.isEmpty()) pessoa_comanda_item.setIdItem(Integer.parseInt(idItemStr));

             System.out.print("Novo id de comanda (vazio para manter): ");
            String idComandaStr = console.nextLine();
            if (!idComandaStr.isEmpty()) pessoa_comanda_item.setIdComanda(Integer.parseInt(idComandaStr));

             System.out.print("Novo valor com desconto (vazio para manter): ");
            String quantidadeStr = console.nextLine();
            if (!quantidadeStr.isEmpty()) pessoa_comanda_item.setQuantidade(Integer.parseInt(quantidadeStr));

             System.out.print("Novo valor com desconto (vazio para manter): ");
            String valorUnitarioStr = console.nextLine();
            if (!valorUnitarioStr.isEmpty()) pessoa_comanda_item.setValorUnitario(Double.parseDouble(valorUnitarioStr));



            if (pessoa_comanda_itemDAO.alterarPessoa_Comanda_Item(pessoa_comanda_item)) {
                System.out.println("Pessoa_Comanda_Item alterado com sucesso.");
            } else {
                System.out.println("Erro ao alterar pessoa_comanda_item.");
            }
        } catch (Exception e) {
            System.out.println("Erro ao alterar pessoa_comanda_item.");
        }
    }

    private void excluirPessoa_Comanda_Item() {
        System.out.print("\nID do pessoa_comanda_item a ser excluído: ");
        int id = console.nextInt();
        console.nextLine();

        try {
            Pessoa_Comanda_Item pessoa_comanda_item = pessoa_comanda_itemDAO.buscarPessoa_Comanda_Item(id);
            if (pessoa_comanda_item == null) {
                System.out.println("Pessoa_Comanda_Item não encontrado.");
                return;
            }

            System.out.print("Confirma exclusão? (S/N): ");
            char resp = console.next().charAt(0);
            if (resp == 'S' || resp == 's') {
                if (pessoa_comanda_itemDAO.excluirPessoa_Comanda_Item(id)) {
                    System.out.println("Pessoa_Comanda_Item excluído com sucesso.");
                } else {
                    System.out.println("Erro ao excluir pessoa_comanda_item.");
                }
            }
        } catch (Exception e) {
            System.out.println("Erro ao excluir pessoa_comanda_item.");
        }
    }
}
