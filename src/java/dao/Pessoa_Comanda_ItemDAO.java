package dao;

import index.hash.*;
import java.util.List;
import model.Arquivo;
import model.Pessoa_Comanda_Item;

public class Pessoa_Comanda_ItemDAO {

    private Arquivo<Pessoa_Comanda_Item> arqPessoa_Comanda_Item;
    private IndiceValorUnitario indiceValorUnitario;

    public Pessoa_Comanda_ItemDAO() throws Exception {
        arqPessoa_Comanda_Item = new Arquivo<>("Pessoa_Comanda_Item", Pessoa_Comanda_Item.class.getConstructor());
        indiceValorUnitario = new IndiceValorUnitario("indice_valor_unitario");
    }

    // busca por valor unitario
    public List<Pessoa_Comanda_Item> buscarPorValorUnitario(double valorUnitario) throws Exception {
        List<Pessoa_Comanda_Item> resultados = new java.util.ArrayList<>();
        
        // busca id com esses valores unitarios
        List<Integer> ids = indiceValorUnitario.buscarTodosIds(valorUnitario);
        
        // Busca os registros completos
        for (int id : ids) {
            Pessoa_Comanda_Item registro = arqPessoa_Comanda_Item.read(id);
            if (registro != null) {
                resultados.add(registro);
            }
        }
        
        return resultados;
    }

    // bruscar o primeiro registro com o valor unitario
    public Pessoa_Comanda_Item buscarPrimeiroPorValorUnitario(double valorUnitario) throws Exception {
        var par = indiceValorUnitario.buscar(valorUnitario);
        if (par != null) {
            return arqPessoa_Comanda_Item.read(par.getId());
        }
        return null;
    }

    
    public int incluirPessoa_Comanda_Item(Pessoa_Comanda_Item pessoa_comanda_item) throws Exception {
        int id = arqPessoa_Comanda_Item.create(pessoa_comanda_item);
        if (id > 0) {
            pessoa_comanda_item.setId(id);
            indiceValorUnitario.inserir(pessoa_comanda_item.getValorUnitario(), id);
            return id;
        }
        return -1;
    }

    // atualizado o alterar
    public boolean alterarPessoa_Comanda_Item(Pessoa_Comanda_Item pessoa_comanda_item) throws Exception {
        Pessoa_Comanda_Item antigo = arqPessoa_Comanda_Item.read(pessoa_comanda_item.getId());
        
        if (antigo != null) {
            double valorAntigo = antigo.getValorUnitario();
            double valorNovo = pessoa_comanda_item.getValorUnitario();
            
            boolean sucesso = arqPessoa_Comanda_Item.update(pessoa_comanda_item);
            
            if (sucesso && valorAntigo != valorNovo) {
                // Remove do índice antigo e insere no novo
                indiceValorUnitario.remover(valorAntigo, pessoa_comanda_item.getId());
                indiceValorUnitario.inserir(valorNovo, pessoa_comanda_item.getId());
            }
            return sucesso;
        }
        return false;
    }

    // Excluir  atualizado
    public boolean excluirPessoa_Comanda_Item(int id) throws Exception {
        Pessoa_Comanda_Item registro = arqPessoa_Comanda_Item.read(id);
        
        if (registro != null) {
            double valorUnitario = registro.getValorUnitario();
            boolean sucesso = arqPessoa_Comanda_Item.delete(id);
            
            if (sucesso) {
                indiceValorUnitario.remover(valorUnitario, id);
            }
            return sucesso;
        }
        return false;
    }

    
    public Pessoa_Comanda_Item buscarPessoa_Comanda_Item(int id) throws Exception {
        return arqPessoa_Comanda_Item.read(id);
    }

    public List<Pessoa_Comanda_Item> buscarPorRangeValorUnitario(double min, double max) throws Exception {
        List<Pessoa_Comanda_Item> resultados = new java.util.ArrayList<>();
        int ultimoID = 10000;
        
        for (int id = 1; id <= ultimoID; id++) {
            Pessoa_Comanda_Item registro = arqPessoa_Comanda_Item.read(id);
            if (registro != null && registro.getValorUnitario() >= min && registro.getValorUnitario() <= max) {
                resultados.add(registro);
            }
        }
        return resultados;
    }

    public void fechar() throws Exception {
        arqPessoa_Comanda_Item.close();
    }
}