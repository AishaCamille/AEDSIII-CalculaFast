package com.calculafast.dao;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.calculafast.index.hash.IndiceChaveComposta;
import com.calculafast.model.Arquivo;
import com.calculafast.model.Comanda;
import com.calculafast.model.Item;
import com.calculafast.model.PessoaComanda;
import com.calculafast.model.Pessoa_Comanda_Item;
import com.calculafast.model.Registro;

public class Pessoa_Comanda_ItemDAO {

    private Arquivo<Pessoa_Comanda_Item> arqPessoa_Comanda_Item;
    private IndiceChaveComposta indiceChave;
    
    private Arquivo<PessoaComanda> arqPessoaComanda;
    private Arquivo<Comanda> arqComanda;
    private Arquivo<Item> arqItem;

    private PessoaComandaDAO pessoaComandaDAO;
    private ComandaDAO comandaDAO;
    private ItemDAO itemDAO;
    
    public void setComandaDAO(ComandaDAO comandaDAO) {
        this.comandaDAO = comandaDAO;
    }

    public Pessoa_Comanda_ItemDAO() throws Exception {
        File pastaIndices = new File("./indices");
        if (!pastaIndices.exists()) {
            pastaIndices.mkdirs();
        }
        
        arqPessoa_Comanda_Item = new Arquivo<>("Pessoa_Comanda_Item", Pessoa_Comanda_Item.class.getConstructor());
        indiceChave = new IndiceChaveComposta("dados/indice_chave_composta");
      
       // this.pessoaComandaDAO = new PessoaComandaDAO();
      //  this.comandaDAO = new ComandaDAO();
       // this.itemDAO = new ItemDAO();
    }
      private PessoaComandaDAO getPessoaComandaDAO() throws Exception {
        if (pessoaComandaDAO == null) {
            pessoaComandaDAO = new PessoaComandaDAO();
        }
        return pessoaComandaDAO;
    }
    
    private ComandaDAO getComandaDAO() throws Exception {
        if (comandaDAO == null) {
            comandaDAO = new ComandaDAO();
        }
        return comandaDAO;
    }
     private ItemDAO getItemDAO() throws Exception {
        if (itemDAO == null) {
            itemDAO = new ItemDAO();
        }
        return itemDAO;
    }
      public Pessoa_Comanda_ItemDAO(PessoaComandaDAO pessoaComandaDAO, ComandaDAO comandaDAO, ItemDAO itemDAO) throws Exception {
        File pastaIndices = new File("./indices");
        if (!pastaIndices.exists()) {
            pastaIndices.mkdirs();
        }
        
        arqPessoa_Comanda_Item = new Arquivo<>("Pessoa_Comanda_Item", Pessoa_Comanda_Item.class.getConstructor());
        indiceChave = new IndiceChaveComposta("dados/indice_chave_composta");
      
       // this.pessoaComandaDAO = pessoaComandaDAO;
       // this.comandaDAO = comandaDAO;
       // this.itemDAO = itemDAO;
    }
    

   public boolean incluirPessoa_Comanda_Item(Pessoa_Comanda_Item pci) throws Exception {
    boolean sucesso = false;
        getPessoaComandaDAO();
        getComandaDAO();
        getItemDAO();
       if (pessoaComandaDAO == null) {
            throw new Exception("PessoaComandaDAO não foi inicializado.");
        }
        if (comandaDAO == null) {
            throw new Exception("ComandaDAO não foi inicializado.");
        }
        if (itemDAO == null) {
            throw new Exception("ItemDAO não foi inicializado.");
        }
    
        if (pessoaComandaDAO.buscar(pci.getIdPessoaComanda()) == null) {
            throw new Exception("Erro de Integridade: PessoaComanda com ID " + pci.getIdPessoaComanda() + " não existe.");
        }
        
        ///verificacao de integridade
        if (getPessoaComandaDAO().buscar(pci.getIdPessoaComanda()) == null) {
            throw new Exception("Erro de Integridade: PessoaComanda com ID " + pci.getIdPessoaComanda() + " não existe.");
        }
        
        if (getComandaDAO().buscarComanda(pci.getIdComanda()) == null) {
            throw new Exception("Erro de Integridade: Comanda com ID " + pci.getIdComanda() + " não existe.");
        }
        
        if (getItemDAO().buscarItem(pci.getIdItem()) == null) {
            throw new Exception("Erro de Integridade: Item com ID " + pci.getIdItem() + " não existe.");
        }
     Pessoa_Comanda_Item existente = buscarPorChaveComposta(pci.getIdPessoaComanda(), pci.getIdComanda(), pci.getIdItem());
    
    if (existente != null) {
        throw new Exception("Já existe registro com esta chave composta!");
    }

    long offset = arqPessoa_Comanda_Item.createWithOffset(pci);
   
    if (offset < 0) {
        int idHash = pci.getChaveComposta();
        Pessoa_Comanda_Item registroArquivo = arqPessoa_Comanda_Item.read(idHash);
        
        if (registroArquivo != null && 
            registroArquivo.getIdPessoaComanda() == pci.getIdPessoaComanda() && 
            registroArquivo.getIdComanda() == pci.getIdComanda() && 
            registroArquivo.getIdItem() == pci.getIdItem()) {
            
            offset = encontrarOffsetNoArquivo(registroArquivo);
            
            if (offset >= 0) {
                indiceChave.inserir(registroArquivo.getIdPessoaComanda(), registroArquivo.getIdComanda(), 
                                   registroArquivo.getIdItem(), offset);
                return true;
            } else {
                throw new Exception("Registro existe no arquivo mas não foi possível encontrar o offset.");
            }
        } else {
            throw new Exception("Erro ao criar registro. ID (hash) já existe para outro registro ou registro não encontrado.");
        }
    }
    
    indiceChave.inserir(pci.getIdPessoaComanda(), pci.getIdComanda(), pci.getIdItem(), offset);
    
    System.out.println("Inserção concluída com sucesso.");
    sucesso = true;
    
    if (sucesso && getComandaDAO() != null) {
        try {
            java.lang.reflect.Method m = getComandaDAO().getClass().getMethod("adicionarPessoaComandaAComanda", int.class, int.class);
            m.invoke(getComandaDAO(), pci.getIdComanda(), pci.getIdPessoaComanda());
        } catch (NoSuchMethodException e1) {
            try {
               java.lang.reflect.Method m2 = getComandaDAO().getClass().getMethod("adicionarPessoaComanda", int.class, int.class);
                m2.invoke(getComandaDAO(), pci.getIdComanda(), pci.getIdPessoaComanda());
            } catch (NoSuchMethodException | IllegalAccessException | java.lang.reflect.InvocationTargetException e2) {
               
                System.out.println("Aviso: método de sincronização não encontrado ou falhou em ComandaDAO.");
            }
        } catch (IllegalAccessException | java.lang.reflect.InvocationTargetException e) {
            System.out.println("Aviso: falha ao invocar método de sincronização em ComandaDAO: " + e.getMessage());
        }
    }
    
    return true;
}

//teste
 public void setPessoaComandaDAO(PessoaComandaDAO pessoaComandaDAO) {
        this.pessoaComandaDAO = pessoaComandaDAO;
    }
public void setItemDAO(ItemDAO itemDAO) {
        this.itemDAO = itemDAO;
    }
//
    public Pessoa_Comanda_Item buscarPorChaveComposta(int idPessoaComanda, int idComanda, int idItem) throws Exception {
        var chave = indiceChave.buscar(idPessoaComanda, idComanda, idItem);
       
        if (chave != null) {
            Pessoa_Comanda_Item resultado = buscarPorOffset(chave.getOffset());
            return resultado;
        }
        return null;
    }

    public boolean alterarPessoa_Comanda_Item(Pessoa_Comanda_Item novo) throws Exception {
        Pessoa_Comanda_Item antigo = buscarPorChaveComposta(novo.getIdPessoaComanda(), novo.getIdComanda(), novo.getIdItem());
        if (antigo == null) return false;

        return arqPessoa_Comanda_Item.update(novo);
    }

    public boolean excluirPessoa_Comanda_Item(int idPessoaComanda, int idComanda, int idItem) throws Exception {
        Pessoa_Comanda_Item registro = buscarPorChaveComposta(idPessoaComanda, idComanda, idItem);
        if (registro == null) return false;

        boolean sucesso = arqPessoa_Comanda_Item.delete(calcularId(registro));
        if (sucesso) {
            indiceChave.remover(idPessoaComanda, idComanda, idItem);
        }
        return sucesso;
    }

    // Métodos de busca modificados para usar PessoaComanda
    public List<Pessoa_Comanda_Item> buscarPorPessoaComanda(int idPessoaComanda) throws Exception {
        List<Pessoa_Comanda_Item> resultados = new ArrayList<>();
        arqPessoa_Comanda_Item.scanValidRecords((Long pos, Pessoa_Comanda_Item registro) -> {
            if (registro.getIdPessoaComanda() == idPessoaComanda) {
                resultados.add(registro);
            }
        });
        return resultados;
    }

    public List<Pessoa_Comanda_Item> buscarPorItem(int idItem) throws Exception {
        List<Pessoa_Comanda_Item> resultados = new ArrayList<>();
        arqPessoa_Comanda_Item.scanValidRecords((Long pos, Pessoa_Comanda_Item registro) -> {
            if (registro.getIdItem() == idItem) {
                resultados.add(registro);
            }
        });
        return resultados;
    }

    public List<Pessoa_Comanda_Item> buscarPorComanda(int idComanda) throws Exception {
        List<Pessoa_Comanda_Item> resultados = new ArrayList<>();
        arqPessoa_Comanda_Item.scanValidRecords((Long pos, Pessoa_Comanda_Item registro) -> {
            if (registro.getIdComanda() == idComanda) {
                resultados.add(registro);
            }
        });
        return resultados;
    }

    public List<Integer> buscarItensUnicosPorPessoaComanda(int idPessoaComanda) throws Exception {
        List<Integer> itensUnicos = new ArrayList<>();
        List<Pessoa_Comanda_Item> relacoes = buscarPorPessoaComanda(idPessoaComanda);
        
        for (Pessoa_Comanda_Item relacao : relacoes) {
            if (!itensUnicos.contains(relacao.getIdItem())) {
                itensUnicos.add(relacao.getIdItem());
            }
        }
        return itensUnicos;
    }

    public List<Pessoa_Comanda_Item> buscarTodos() throws Exception {
        List<Pessoa_Comanda_Item> todos = new ArrayList<>();
        arqPessoa_Comanda_Item.scanValidRecords((Long pos, Pessoa_Comanda_Item registro) -> {
            todos.add(registro);
        });
        return todos;
    }

    private long calcularOffset(int id) {
        int tamanhoRegistro = new Pessoa_Comanda_Item().size();
        int tamanhoCabecalho = 12; 
        return tamanhoCabecalho + (id - 1) * (1 + 2 + tamanhoRegistro);
    }
    
    private int calcularId(Pessoa_Comanda_Item pci) {
        return pci.getChaveComposta();
    }
    
    private long encontrarOffsetNoArquivo(Pessoa_Comanda_Item pci) throws Exception {
        final long[] offsetEncontrado = {-1};
        arqPessoa_Comanda_Item.scanValidRecords((Long pos, Pessoa_Comanda_Item registro) -> {
            if (registro.getIdPessoaComanda() == pci.getIdPessoaComanda() && 
                registro.getIdComanda() == pci.getIdComanda() && 
                registro.getIdItem() == pci.getIdItem()) {
                offsetEncontrado[0] = pos;
            }
        });
        return offsetEncontrado[0];
    }

    private Pessoa_Comanda_Item buscarPorOffset(long offset) throws Exception {
        try {
            Registro registro = arqPessoa_Comanda_Item.readAt(offset);
            if (registro instanceof Pessoa_Comanda_Item) {
                return (Pessoa_Comanda_Item) registro;
            }
            return null;
        } catch (Exception e) {
            System.out.println("Erro no buscarPorOffset: " + e.getMessage());
            return buscarPorOffsetSequencial(offset);
        }
    }

    private Pessoa_Comanda_Item buscarPorOffsetSequencial(long offsetAlvo) throws Exception {
        int tentativas = 0;
        while (tentativas < 10000) { 
            try {
                tentativas++;
                Pessoa_Comanda_Item registro = arqPessoa_Comanda_Item.read(tentativas);
                if (registro != null) {
                    long offsetCalculado = calcularOffset(tentativas);
                    if (offsetCalculado == offsetAlvo) {
                        return registro;
                    }
                }
            } catch (Exception e) {
                break; 
            }
        }
        return null;
    }

    public void fechar() throws Exception {
        indiceChave.fechar();
        arqPessoa_Comanda_Item.close();
        
        // Fecha os DAOs se eles foram criados por este objeto
        if (pessoaComandaDAO != null) {
            pessoaComandaDAO.fechar();
        }
        if (comandaDAO != null) {
            comandaDAO.fechar();
        }
        if (itemDAO != null) {
            itemDAO.fechar();
        }
    }
}