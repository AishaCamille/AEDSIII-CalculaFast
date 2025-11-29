package com.calculafast.dao;

import java.util.ArrayList;
import java.util.List;

import com.calculafast.index.hash.HashExtensivel;
import com.calculafast.index.hash.ParIdOffset;
import com.calculafast.model.Arquivo;
import com.calculafast.model.Pessoa;
import com.calculafast.model.Pessoa_Comanda_Item;

public class PessoaDAO {

    private Arquivo<Pessoa> arqPessoas;
    private HashExtensivel<ParIdOffset> idxPk;
    private Pessoa_Comanda_ItemDAO pciDAO;
    
    public PessoaDAO() throws Exception {
        this.arqPessoas = new Arquivo<>("pessoas", Pessoa.class.getConstructor());
        
        this.idxPk = new HashExtensivel<>(
                ParIdOffset.class.getConstructor(), 
                10, 
                "./dados/pessoas/pessoas.pkhash_d.db", 
                "./dados/pessoas/pessoas.pkhash_b.db"
        );
        
        this.pciDAO = new Pessoa_Comanda_ItemDAO();
        
        rebuildIndex();
    }

  private void rebuildIndex() throws Exception {
    final int[] contadores = {0, 0};
    
    arqPessoas.scanValidRecords((pos, obj) -> {
        try {
            Pessoa p = (Pessoa) obj;
            boolean success = idxPk.create(new ParIdOffset(p.getId(), pos));
            if (!success) {
                success = idxPk.update(new ParIdOffset(p.getId(), pos));
            }
            if (success) {
                contadores[0]++;
            } else {
                contadores[1]++;
            }
        } catch (Exception e) {
            contadores[1]++;
        }
    });
}

    public Pessoa buscarPessoa(int id) throws Exception {
        try {
            ParIdOffset ref = idxPk.read(id);
            if (ref != null) {
                Pessoa p = (Pessoa) arqPessoas.readAt(ref.getOffset());
                if (p != null && p.getId() == id) {
                    return p;
                }
            }
        } catch (Exception e) {
        }
        
        return buscarSequencial(id);
    }
    
    private Pessoa buscarSequencial(int id) throws Exception {
        final Pessoa[] encontrado = {null};
        arqPessoas.scanValidRecords((pos, obj) -> {
            Pessoa p = (Pessoa) obj;
            if (p.getId() == id) {
                encontrado[0] = p;
            }
        });
        return encontrado[0];
    }

    public boolean incluirPessoa(Pessoa pessoa) throws Exception {
        if (buscarPessoa(pessoa.getId()) != null) {
             throw new Exception("Pessoa com ID " + pessoa.getId() + " já existe.");
        }

        long off = arqPessoas.createWithOffset(pessoa);
        if (off < 0) return false;
        
        idxPk.create(new ParIdOffset(pessoa.getId(), off));
        return true;
    }

    public boolean alterarPessoa(Pessoa pessoa) throws Exception {
        long off = arqPessoas.updateWithOffset(pessoa);
        if (off < 0) return false;
        
        ParIdOffset par = new ParIdOffset(pessoa.getId(), off);
        if (!idxPk.update(par)) {
            idxPk.create(par);
        }
        return true;
    }

    public boolean excluirPessoa(int id) throws Exception {
        List<Pessoa_Comanda_Item> relacoes = pciDAO.buscarPorPessoa(id);
        if (!relacoes.isEmpty()) {
            throw new Exception("Não é possível excluir pessoa. Existem " + relacoes.size() + 
                              " registro(s) relacionados em Pessoa_Comanda_Item.");
        } 
        
        boolean ok = arqPessoas.delete(id);
        if (ok) {
            idxPk.delete(id);
        }
        return ok;
    }

     public List<Integer> getItensCompradosPorPessoa(int idPessoa) throws Exception {
        return pciDAO.buscarItensUnicosPorPessoa(idPessoa);
    }
    
    public List<Pessoa_Comanda_Item> getRelacoesDaPessoa(int idPessoa) throws Exception {
        return pciDAO.buscarPorPessoa(idPessoa);
    }
    
    public void fechar() throws Exception {
        arqPessoas.close();
        pciDAO.fechar();
    }
    public List<Pessoa> listarPessoas() throws Exception {
    List<Pessoa> listaPessoas = new ArrayList<>();
    
    arqPessoas.scanValidRecords((pos, obj) -> {
        Pessoa p = (Pessoa) obj;
        listaPessoas.add(p);
    });
    
    return listaPessoas;
}
}