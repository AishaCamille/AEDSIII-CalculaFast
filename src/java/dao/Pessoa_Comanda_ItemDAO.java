package dao;

import index.hash.IndiceChaveComposta;
import model.Arquivo;
import model.Pessoa_Comanda_Item;

public class Pessoa_Comanda_ItemDAO {

    private Arquivo<Pessoa_Comanda_Item> arqPessoa_Comanda_Item;
    private IndiceChaveComposta indiceChave;

    public Pessoa_Comanda_ItemDAO() throws Exception {
        arqPessoa_Comanda_Item = new Arquivo<>("Pessoa_Comanda_Item", Pessoa_Comanda_Item.class.getConstructor());
        indiceChave = new IndiceChaveComposta("indice_chave_composta");
    }

    // INCLUIR - usando chave composta
    public boolean incluirPessoa_Comanda_Item(Pessoa_Comanda_Item pci) throws Exception {
        // Verifica se já existe
        if (buscarPorChaveComposta(pci.getIdPessoa(), pci.getIdComanda(), pci.getIdItem()) != null) {
            throw new Exception("Já existe registro com esta chave composta!");
        }

        // Insere no arquivo principal
        int id = arqPessoa_Comanda_Item.create(pci);
        if (id > 0) {
            long offset = calcularOffset(id);
            indiceChave.inserir(pci.getIdPessoa(), pci.getIdComanda(), pci.getIdItem(), offset);
            return true;
        }
        return false;
    }

    // BUSCA PRINCIPAL - por chave composta
    public Pessoa_Comanda_Item buscarPorChaveComposta(int idPessoa, int idComanda, int idItem) throws Exception {
        var chave = indiceChave.buscar(idPessoa, idComanda, idItem);
        return (chave != null) ? buscarPorOffset(chave.getOffset()) : null;
    }

    // ALTERAR
    public boolean alterarPessoa_Comanda_Item(Pessoa_Comanda_Item novo) throws Exception {
        Pessoa_Comanda_Item antigo = buscarPorChaveComposta(novo.getIdPessoa(), novo.getIdComanda(), novo.getIdItem());
        if (antigo == null) return false;

        return arqPessoa_Comanda_Item.update(novo);
    }

    // EXCLUIR
    public boolean excluirPessoa_Comanda_Item(int idPessoa, int idComanda, int idItem) throws Exception {
        Pessoa_Comanda_Item registro = buscarPorChaveComposta(idPessoa, idComanda, idItem);
        if (registro == null) return false;

        boolean sucesso = arqPessoa_Comanda_Item.delete(calcularId(registro));
        if (sucesso) {
            indiceChave.remover(idPessoa, idComanda, idItem);
        }
        return sucesso;
    }

    // Métodos auxiliares
    private long calcularOffset(int id) {
        return id * new Pessoa_Comanda_Item().size();
    }

    private int calcularId(Pessoa_Comanda_Item pci) {
        return pci.getChaveComposta(); // Usa o hash como ID
    }

    private Pessoa_Comanda_Item buscarPorOffset(long offset) throws Exception {
        int id = (int) (offset / new Pessoa_Comanda_Item().size());
        return arqPessoa_Comanda_Item.read(id);
    }

    public void fechar() throws Exception {
        indiceChave.fechar();
        arqPessoa_Comanda_Item.close();
    }
}