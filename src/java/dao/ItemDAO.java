package dao;
import model.Arquivo;
import model.Item;
import model.Pessoa_Comanda_Item;

import java.sql.*;
import java.util.*;


public class ItemDAO  {
    private Arquivo<Item> arqItem;
   private Pessoa_Comanda_ItemDAO pciDAO;

    public ItemDAO() throws Exception {
        arqItem = new Arquivo<>("itens", Item.class.getConstructor());
        pciDAO = new Pessoa_Comanda_ItemDAO();
    }

    public ItemDAO(Arquivo<Item> arqItem) {
        this.arqItem = arqItem;
    }

    public Item buscarItem(int id) throws Exception {
        return arqItem.read(id);
    }

    public boolean incluirItem(Item item) throws Exception {
        return arqItem.create(item) > 0;
    }

    public boolean alterarItem(Item item) throws Exception {
        return arqItem.update(item);
    }

    public boolean excluirItem(int id) throws Exception {
        return arqItem.delete(id);
    }

    //mostra todas as pessoas que compraram um item
    public List<Integer> getPessoasQueCompraramItem(int idItem) throws Exception {
        List<Integer> pessoas = new java.util.ArrayList<>();
        List<Pessoa_Comanda_Item> relacoes = pciDAO.buscarPorItem(idItem);
        
        for (Pessoa_Comanda_Item relacao : relacoes) {
            if (!pessoas.contains(relacao.getIdPessoa())) {
                pessoas.add(relacao.getIdPessoa());
            }
        }
        return pessoas;
    }

    //busca quantas comandas o item aparece
    public List<Integer> getComandasDoItem(int idItem) throws Exception {
        List<Integer> comandas = new java.util.ArrayList<>();
        List<Pessoa_Comanda_Item> relacoes = pciDAO.buscarPorItem(idItem);
        
        for (Pessoa_Comanda_Item relacao : relacoes) {
            if (!comandas.contains(relacao.getIdComanda())) {
                comandas.add(relacao.getIdComanda());
            }
        }
        return comandas;
    }
}
