package dao;
import model.Arquivo;
import model.Comanda;




public class ComandaDAO {
    private Arquivo<Comanda> arqComandas;

    public ComandaDAO() throws Exception {
        arqComandas = new Arquivo<>("comandas", Comanda.class.getConstructor());
    }

    public ComandaDAO(Arquivo<Comanda> arqComandas) {
        this.arqComandas = arqComandas;
    }

    public Comanda buscarComanda(int id) throws Exception {
        return arqComandas.read(id);
    }

    public boolean incluirComanda(Comanda comanda) throws Exception {
        return arqComandas.create(comanda) > 0;
    }

    public boolean alterarComanda(Comanda comanda) throws Exception {
        return arqComandas.update(comanda);
    }

    public boolean excluirComanda(int id) throws Exception {
        return arqComandas.delete(id);
    }
}
