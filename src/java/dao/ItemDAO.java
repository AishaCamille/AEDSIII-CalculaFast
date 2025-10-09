package dao;
import model.Arquivo;
import model.Item;
import java.sql.*;
import java.util.*;

public class ItemDAO  {
    private Arquivo<Item> arqItem;

    public ItemDAO() throws Exception {
        arqItem = new Arquivo<>("itens", Item.class.getConstructor());
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

}