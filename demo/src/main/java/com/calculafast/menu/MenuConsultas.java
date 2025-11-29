package com.calculafast.menu;

import com.calculafast.dao.PessoaDAO;
import com.calculafast.dao.ItemDAO;
import com.calculafast.dao.ComandaDAO;
import com.calculafast.dao.Pessoa_Comanda_ItemDAO;
import java.util.List;
import java.util.Scanner;

public class MenuConsultas {
    private Scanner console = new Scanner(System.in);
    private PessoaDAO pessoaDAO;
    private ItemDAO itemDAO;
    private ComandaDAO comandaDAO;
    private Pessoa_Comanda_ItemDAO pciDAO;

    public MenuConsultas() throws Exception {
        pessoaDAO = new PessoaDAO();
        itemDAO = new ItemDAO();
        comandaDAO = new ComandaDAO();
        pciDAO = new Pessoa_Comanda_ItemDAO();
    }

    public void menu() {
        int opcao;
        do {
            System.out.println("\n\n=== CONSULTAS BIDIRECIONAIS ===");
            System.out.println("1 - Itens de uma Pessoa");
            System.out.println("2 - Pessoas que compraram um Item");
            System.out.println("3 - Itens de uma Comanda");
            System.out.println("4 - Pessoas de uma Comanda");
            System.out.println("0 - Voltar");

            System.out.print("\nOpção: ");
            opcao = console.nextInt();
            console.nextLine();

            switch (opcao) {
                case 1:
                    consultarItensDaPessoa();
                    break;
                case 2:
                    consultarPessoasDoItem();
                    break;
                case 3:
                    consultarItensDaComanda();
                    break;
                case 4:
                    consultarPessoasDaComanda();
                    break;
            }
        } while (opcao != 0);
    }

    private void consultarItensDaPessoa() {
        System.out.print("\nID da Pessoa: ");
        int idPessoa = console.nextInt();
        console.nextLine();
        
        try {
            List<Integer> itens = pessoaDAO.getItensCompradosPorPessoa(idPessoa);
            System.out.println("\nItens comprados pela pessoa " + idPessoa + ":");
            for (Integer idItem : itens) {
                System.out.println(" - Item ID: " + idItem);
            }
            System.out.println("Total: " + itens.size() + " itens diferentes");
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    private void consultarPessoasDoItem() {
        System.out.print("\nID do Item: ");
        int idItem = console.nextInt();
        console.nextLine();
        
        try {
            List<Integer> pessoas = itemDAO.getPessoasQueCompraramItem(idItem);
            System.out.println("\nPessoas que compraram o item " + idItem + ":");
            for (Integer idPessoa : pessoas) {
                System.out.println(" - Pessoa ID: " + idPessoa);
            }
            System.out.println("Total: " + pessoas.size() + " pessoas diferentes");
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    private void consultarItensDaComanda() {
        System.out.print("\nID da Comanda: ");
        int idComanda = console.nextInt();
        console.nextLine();
        
        try {
            List<Integer> itens = comandaDAO.getItensDaComanda(idComanda);
            System.out.println("\nItens da comanda " + idComanda + ":");
            for (Integer idItem : itens) {
                System.out.println(" - Item ID: " + idItem);
            }
            System.out.println("Total: " + itens.size() + " itens diferentes");
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    private void consultarPessoasDaComanda() {
        System.out.print("\nID da Comanda: ");
        int idComanda = console.nextInt();
        console.nextLine();
        
        try {
            List<Integer> pessoas = comandaDAO.getPessoasDaComanda(idComanda);
            System.out.println("\nPessoas na comanda " + idComanda + ":");
            for (Integer idPessoa : pessoas) {
                System.out.println(" - Pessoa ID: " + idPessoa);
            }
            System.out.println("Total: " + pessoas.size() + " pessoas diferentes");
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }
}