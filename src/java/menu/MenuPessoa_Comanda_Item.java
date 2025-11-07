package menu;
import dao.Pessoa_Comanda_ItemDAO;
import java.util.List;
import java.util.Scanner;
import model.Pessoa_Comanda_Item;

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
            System.out.println("\n1 - Buscar por ID");
            System.out.println("2 - Buscar por Valor Unitário"); // NOVA OPÇÃO
            System.out.println("3 - Incluir");
            System.out.println("4 - Alterar");
            System.out.println("5 - Excluir");
            System.out.println("0 - Voltar");

            System.out.print("\nOpção: ");
            try {
                opcao = Integer.valueOf(console.nextLine());
            } catch(NumberFormatException e) {
                opcao = -1;
            }

            switch (opcao) {
                case 1:
                    buscarPessoa_Comanda_ItemPorId();
                    break;
                case 2:
                    buscarPessoa_Comanda_ItemPorValorUnitario(); // NOVO MÉTODO
                    break;
                case 3:
                    incluirPessoa_Comanda_Item();
                    break;
                case 4:
                    alterarPessoa_Comanda_Item();
                    break;
                case 5:
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

    private void buscarPessoa_Comanda_ItemPorId() {
        System.out.print("\nID do Pessoa_Comanda_Item: ");
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
            System.out.println("Erro ao buscar Pessoa_Comanda_Item: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // NOVO MÉTODO: Buscar por valor unitário usando o índice hash
    private void buscarPessoa_Comanda_ItemPorValorUnitario() {
        System.out.print("\nValor Unitário para buscar: ");
        double valorUnitario = console.nextDouble();
        console.nextLine();
        
        try {
            List<Pessoa_Comanda_Item> resultados = pessoa_comanda_itemDAO.buscarPorValorUnitario(valorUnitario);
            
            if (resultados.isEmpty()) {
                System.out.println("Nenhum Pessoa_Comanda_Item encontrado com valor unitário: " + valorUnitario);
            } else {
                System.out.println("\n=== " + resultados.size() + " registro(s) encontrado(s) ===");
                for (Pessoa_Comanda_Item pci : resultados) {
                    System.out.println(pci);
                    System.out.println("---");
                }
            }
        } catch (Exception e) {
            System.out.println("Erro ao buscar por valor unitário: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void incluirPessoa_Comanda_Item() {
        System.out.println("\nInclusão de Pessoa_Comanda_Item");

        System.out.print("ID Pessoa: ");
        int idPessoa = console.nextInt();
        console.nextLine();
        
        System.out.print("ID Item: ");
        int idItem = console.nextInt();
        console.nextLine();
        
        System.out.print("ID Comanda: ");
        int idComanda = console.nextInt();
        console.nextLine();
        
        System.out.print("Quantidade: ");
        int quantidade = console.nextInt();
        console.nextLine();
        
        System.out.print("Valor Unitário: ");
        double valorUnitario = console.nextDouble();
        console.nextLine();
       
        try {
            Pessoa_Comanda_Item pessoa_comanda_item = new Pessoa_Comanda_Item(idPessoa, idItem, idComanda, quantidade, valorUnitario);
            int idGerado = pessoa_comanda_itemDAO.incluirPessoa_Comanda_Item(pessoa_comanda_item); // AGORA RETORNA int
            
            if (idGerado > 0) {
                System.out.println("Pessoa_Comanda_Item incluído com sucesso. ID: " + idGerado);
            } else {
                System.out.println("Erro ao incluir Pessoa_Comanda_Item.");
            }
        } catch (Exception e) {
            System.out.println("Erro ao incluir Pessoa_Comanda_Item: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void alterarPessoa_Comanda_Item() {
        System.out.print("\nID do Pessoa_Comanda_Item a ser alterado: ");
        int id = console.nextInt();
        console.nextLine();

        try {
            Pessoa_Comanda_Item pessoa_comanda_item = pessoa_comanda_itemDAO.buscarPessoa_Comanda_Item(id);
            if (pessoa_comanda_item == null) {
                System.out.println("Pessoa_Comanda_Item não encontrado.");
                return;
            }
            
            System.out.println("Dados atuais:");
            System.out.println(pessoa_comanda_item);
            System.out.println("\nDigite os novos valores (enter para manter o atual):");

            System.out.print("Novo ID Pessoa [" + pessoa_comanda_item.getIdPessoa() + "]: ");
            String idPessoaStr = console.nextLine();
            if (!idPessoaStr.isEmpty()) pessoa_comanda_item.setIdPessoa(Integer.parseInt(idPessoaStr));

            System.out.print("Novo ID Item [" + pessoa_comanda_item.getIdItem() + "]: ");
            String idItemStr = console.nextLine();
            if (!idItemStr.isEmpty()) pessoa_comanda_item.setIdItem(Integer.parseInt(idItemStr));

            System.out.print("Novo ID Comanda [" + pessoa_comanda_item.getIdComanda() + "]: ");
            String idComandaStr = console.nextLine();
            if (!idComandaStr.isEmpty()) pessoa_comanda_item.setIdComanda(Integer.parseInt(idComandaStr));

            System.out.print("Nova Quantidade [" + pessoa_comanda_item.getQuantidade() + "]: ");
            String quantidadeStr = console.nextLine();
            if (!quantidadeStr.isEmpty()) pessoa_comanda_item.setQuantidade(Integer.parseInt(quantidadeStr));

            System.out.print("Novo Valor Unitário [" + pessoa_comanda_item.getValorUnitario() + "]: ");
            String valorUnitarioStr = console.nextLine();
            if (!valorUnitarioStr.isEmpty()) pessoa_comanda_item.setValorUnitario(Double.parseDouble(valorUnitarioStr));

            if (pessoa_comanda_itemDAO.alterarPessoa_Comanda_Item(pessoa_comanda_item)) {
                System.out.println("Pessoa_Comanda_Item alterado com sucesso.");
            } else {
                System.out.println("Erro ao alterar Pessoa_Comanda_Item.");
            }
        } catch (Exception e) {
            System.out.println("Erro ao alterar Pessoa_Comanda_Item: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void excluirPessoa_Comanda_Item() {
        System.out.print("\nID do Pessoa_Comanda_Item a ser excluído: ");
        int id = console.nextInt();
        console.nextLine();

        try {
            Pessoa_Comanda_Item pessoa_comanda_item = pessoa_comanda_itemDAO.buscarPessoa_Comanda_Item(id);
            if (pessoa_comanda_item == null) {
                System.out.println("Pessoa_Comanda_Item não encontrado.");
                return;
            }

            System.out.println("Registro a ser excluído:");
            System.out.println(pessoa_comanda_item);
            
            System.out.print("\nConfirma exclusão? (S/N): ");
            char resp = console.next().charAt(0);
            console.nextLine();
            
            if (resp == 'S' || resp == 's') {
                if (pessoa_comanda_itemDAO.excluirPessoa_Comanda_Item(id)) {
                    System.out.println("Pessoa_Comanda_Item excluído com sucesso.");
                } else {
                    System.out.println("Erro ao excluir Pessoa_Comanda_Item.");
                }
            } else {
                System.out.println("Exclusão cancelada.");
            }
        } catch (Exception e) {
            System.out.println("Erro ao excluir Pessoa_Comanda_Item: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Método para fechar recursos (importante chamar qnd  sair)
    public void fechar() {
        try {
            pessoa_comanda_itemDAO.fechar();
        } catch (Exception e) {
            System.out.println("Erro ao fechar DAO: " + e.getMessage());
        }
    }
}