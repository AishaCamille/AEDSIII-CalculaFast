package menu;
import model.Item;
import dao.ItemDAO;
import java.util.Scanner;

public class MenuItem {
     private ItemDAO itemDAO;
    private Scanner console = new Scanner(System.in);

    public MenuItem() throws Exception {
        itemDAO = new ItemDAO();
    }

    public void menu() {
        int opcao;
        do {
            System.out.println("\n\nAEDsIII");
            System.out.println("-------");
            System.out.println("> Início > Items");
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
                    buscarItem();
                    break;
                case 2:
                    incluirItem();
                    break;
                case 3:
                    alterarItem();
                    break;
                case 4:
                    excluirItem();
                    break;
                case 0:
                    break;
                default:
                    System.out.println("Opção inválida!");
                    break;
            }
        } while (opcao != 0);
    }

    private void buscarItem() {
        System.out.print("\nID do item: ");
        int id = console.nextInt();
        console.nextLine();
        try {
            Item item = itemDAO.buscarItem(id);
            if (item != null) {
                System.out.println(item);
            } else {
                System.out.println("Item não encontrado.");
            }
        } catch (Exception e) {
            System.out.println("Erro ao buscar item.");
        }
    }

    private void incluirItem() {
        System.out.println("\nInclusão de item");

        System.out.print("\nnome do item: ");
        String descricao = console.nextLine();
        System.out.print("valor: ");
        double valor = console.nextInt();
        console.nextLine();
        System.out.print("Quantidade: ");
        int quantidade = console.nextInt();
        console.nextLine();
        try {
            Item item = new Item(descricao, valor, quantidade);
            if (itemDAO.incluirItem(item)) {
                System.out.println("Item incluído com sucesso.");
            } else {
                System.out.println("Erro ao incluir item.");
            }
        } catch (Exception e) {
            System.out.println("Erro ao incluir item.");
        }
    }

    private void alterarItem() {
        System.out.print("\nID do item a ser alterado: ");
        int id = console.nextInt();
        console.nextLine();

        try {
            Item item = itemDAO.buscarItem(id);
            if (item == null) {
                System.out.println("Item não encontrado.");
                return;
            }

            System.out.print("\nNovo descricao (vazio para manter): ");
            String descricao = console.nextLine();
            if (!descricao.isEmpty()) item.setDescricao(descricao);

            System.out.print("Novo valor (vazio para manter): ");
            String valor = console.nextLine();
            console.nextLine();
            if (!valor.isEmpty()) item.setValor(Double.parseDouble(valor));

            System.out.print("Nova quantidade (vazio para manter): ");
            String quantidadeStr = console.nextLine();
            if (!quantidadeStr.isEmpty()) item.setQuantidade(Integer.parseInt(quantidadeStr));

            
            if (itemDAO.alterarItem(item)) {
                System.out.println("Item alterado com sucesso.");
            } else {
                System.out.println("Erro ao alterar item.");
            }
        } catch (Exception e) {
            System.out.println("Erro ao alterar item.");
        }
    }

    private void excluirItem() {
        System.out.print("\nID do item a ser excluído: ");
        int id = console.nextInt();
        console.nextLine();

        try {
            Item item = itemDAO.buscarItem(id);
            if (item == null) {
                System.out.println("Item não encontrado.");
                return;
            }

            System.out.print("Confirma exclusão? (S/N): ");
            char resp = console.next().charAt(0);
            if (resp == 'S' || resp == 's') {
                if (itemDAO.excluirItem(id)) {
                    System.out.println("Item excluído com sucesso.");
                } else {
                    System.out.println("Erro ao excluir item.");
                }
            }
        } catch (Exception e) {
            System.out.println("Erro ao excluir item.");
        }
    }
}
