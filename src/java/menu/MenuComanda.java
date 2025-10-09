package menu;
import model.Comanda;
import dao.ComandaDAO;
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
        int id = console.nextInt();
        console.nextLine();
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
        int idPessoa = console.nextInt();
        console.nextLine();
        System.out.print("status: ");
        String status = console.nextLine();
        System.out.print("Consumo pessoa: ");
        float consumoPessoa = console.nextFloat();
        console.nextLine();
       
        try {
            Comanda comanda = new Comanda(idPessoa, status, consumoPessoa);
            if (comandaDAO.incluirComanda(comanda)) {
                System.out.println("Comanda incluído com sucesso.");
            } else {
                System.out.println("Erro ao incluir comanda.");
            }
        } catch (Exception e) {
            System.out.println("Erro ao incluir comanda.");
        }
    }

    private void alterarComanda() {
        System.out.print("\nID do comanda a ser alterado: ");
        int id = console.nextInt();
        console.nextLine();

        try {
            Comanda comanda = comandaDAO.buscarComanda(id);
            if (comanda == null) {
                System.out.println("Comanda não encontrado.");
                return;
            }

            System.out.print("\nNovo isPessoa (vazio para manter): ");
            int idPessoa = console.nextInt();
            console.nextLine();
            if (idPessoa>0) comanda.setIdPessoa(idPessoa);

            System.out.print("Novo status (vazio para manter): ");
            String status = console.nextLine();
            if (!status.isEmpty()) comanda.setStatus(status);

            System.out.print("Novo salário (vazio para manter): ");
            String consumoPessoaStr = console.nextLine();
            if (!consumoPessoaStr.isEmpty()) comanda.setConsumoPessoa(Float.parseFloat(consumoPessoaStr));

          
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
        int id = console.nextInt();
        console.nextLine();

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
}
