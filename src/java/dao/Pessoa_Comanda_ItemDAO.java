package dao;
import model.Arquivo;
import model.Pessoa_Comanda_Item;


public class Pessoa_Comanda_ItemDAO  {

   private Arquivo<Pessoa_Comanda_Item> arqPessoa_Comanda_Item;

    public Pessoa_Comanda_ItemDAO() throws Exception {
        arqPessoa_Comanda_Item = new Arquivo<>("Pessoa, Comanda, Item", Pessoa_Comanda_Item.class.getConstructor());
    }

    public Pessoa_Comanda_ItemDAO(Arquivo<Pessoa_Comanda_Item> arqPessoa_Comanda_Item) {
        this.arqPessoa_Comanda_Item = arqPessoa_Comanda_Item;
    }

    public Pessoa_Comanda_Item buscarPessoa_Comanda_Item(int id) throws Exception {
        return arqPessoa_Comanda_Item.read(id);
    }

    public boolean incluirPessoa_Comanda_Item(Pessoa_Comanda_Item pessoa_comanda_item) throws Exception {
        return arqPessoa_Comanda_Item.create(pessoa_comanda_item) > 0;
    }

    public boolean alterarPessoa_Comanda_Item(Pessoa_Comanda_Item pessoa_comanda_item) throws Exception {
        return arqPessoa_Comanda_Item.update(pessoa_comanda_item);
    }

    public boolean excluirPessoa_Comanda_Item(int id) throws Exception {
        return arqPessoa_Comanda_Item.delete(id);
    }
}
