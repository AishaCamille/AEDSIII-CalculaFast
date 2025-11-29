package com.calculafast.dao;
import com.calculafast.model.Arquivo;
import com.calculafast.model.Item;
import com.calculafast.model.Pessoa_Comanda_Item;

import java.util.*;


public class ItemDAO  {
    private Arquivo<Item> arqItem;
   private Pessoa_Comanda_ItemDAO pciDAO;

    public ItemDAO() throws Exception {
        arqItem = new Arquivo<>("itens", Item.class.getConstructor());
        //pciDAO = new Pessoa_Comanda_ItemDAO();
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
        // verifica se tem registros relacionados antes de excluir
     //   List<Pessoa_Comanda_Item> relacoes = pciDAO.buscarPorItem(id);
      /* if (!relacoes.isEmpty()) {
            throw new Exception("Não é possível excluir item. Existem " + relacoes.size() + 
                              " registro(s) relacionados em Pessoa_Comanda_Item. " +
                              "Exclua os registros relacionados primeiro.");
        } */ 
        
        return arqItem.delete(id);
    }

    //mostra todas as pessoas que compraram um item
    public List<Integer> getPessoasQueCompraramItem(int idItem) throws Exception {
        pciDAO = new Pessoa_Comanda_ItemDAO();
        List<Integer> pessoas = new java.util.ArrayList<>();
       List<Pessoa_Comanda_Item> relacoes = pciDAO.buscarPorItem(idItem);
        
        for (Pessoa_Comanda_Item relacao : relacoes) {
            if (!pessoas.contains(relacao.getIdPessoaComanda())) {
                pessoas.add(relacao.getIdPessoaComanda());
            }
        }
        return pessoas;
    }

    //busca quantas comandas o item aparece
    public List<Integer> getComandasDoItem(int idItem) throws Exception {
        pciDAO = new Pessoa_Comanda_ItemDAO();
        List<Integer> comandas = new java.util.ArrayList<>();
        List<Pessoa_Comanda_Item> relacoes = pciDAO.buscarPorItem(idItem);
        
        for (Pessoa_Comanda_Item relacao : relacoes) {
            if (!comandas.contains(relacao.getIdComanda())) {
                comandas.add(relacao.getIdComanda());
            }
        }
        return comandas;
    }
    public void fechar() throws Exception {
        arqItem.close();
        pciDAO.fechar();
    }
}
