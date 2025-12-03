/*package com.calculafast.app;
import java.util.Scanner;

import com.calculafast.menu.MenuComanda;
import com.calculafast.menu.MenuConsultas;
import com.calculafast.menu.MenuItem;
import com.calculafast.menu.MenuPagamento;
import com.calculafast.menu.MenuPessoaComanda;
import com.calculafast.menu.MenuPessoa_Comanda_Item;
import com.calculafast.menu.MenuPessoas;



public class Principal {
    public static void main(String[] args) {
        
        Scanner console = new Scanner(System.in);
        int opcao;

        try {
            do {
                System.out.println("\n\nAEDsIII");
                System.out.println("-------");
                System.out.println("> Início");
                System.out.println("\n1 - Pessoas");
                System.out.println("\n2 - Comanda");
                System.out.println("\n3 - Item");
                System.out.println("\n4 - Pessoa_comanda_item");
                System.out.println("\n5 - Pagamento");
                System.out.println("\n6 - Pessoas na Comanda");
                System.out.println("\n7 - Consultas bidirecionais");//da relação n:n
                System.out.println("\n0 - Sair");

                System.out.print("\nOpção: ");
                try {
                    opcao = Integer.valueOf(console.nextLine());
                } catch (NumberFormatException e) {
                    opcao = -1;
                }

                switch (opcao) {
                    case 1:
                        MenuPessoas menuPessoas = new MenuPessoas();
                        menuPessoas.menu();
                        break;
                    case 2:
                        MenuComanda menuComanda = new MenuComanda();
                        menuComanda.menu();
                        break;
                    case 3:
                        MenuItem menuItem = new MenuItem();
                        menuItem.menu();
                        break;
                    case 4:
                        MenuPessoa_Comanda_Item menuPessoa_MenuPessoa_Comanda_Item = new MenuPessoa_Comanda_Item();
                        menuPessoa_MenuPessoa_Comanda_Item.menu();
                        break;
                    case 5:
                        MenuPagamento menuPagamento = new MenuPagamento();
                        menuPagamento.menu();
                        break;
                    case 6:
                        MenuPessoaComanda menuPessoaComanda = new MenuPessoaComanda();
                        menuPessoaComanda.menu();
                        break;  
                    case 7:
                        MenuConsultas menuConsultas = new MenuConsultas();    
                        menuConsultas.menu();
                        break;
                    case 0:
                        System.out.println("Saindo...");
                        break;
                    default:
                        System.out.println("Opção inválida!");
                        break;
                }
            } while (opcao != 0);

        } catch (Exception e) {
            System.err.println("Erro fatal no sistema:");
            e.printStackTrace();
        } finally {
            console.close();
        }
    }
}

*/