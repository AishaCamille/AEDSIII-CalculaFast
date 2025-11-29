package com.calculafast.dao;

import com.calculafast.model.Arquivo;
import com.calculafast.model.PessoaComanda;
import com.calculafast.model.Pessoa_Comanda_Item;
import com.calculafast.index.hash.HashExtensivel;
import com.calculafast.index.hash.ParIdOffset;
import java.util.List;
import java.util.ArrayList;

public class PessoaComandaDAO {
    private Arquivo<PessoaComanda> arqPessoaComanda;
    private HashExtensivel<ParIdOffset> idxPk;
    private Pessoa_Comanda_ItemDAO pciDAO;
    
    public PessoaComandaDAO() throws Exception {
        arqPessoaComanda = new Arquivo<>("pessoa_comanda", PessoaComanda.class.getConstructor());
        
        this.idxPk = new HashExtensivel<>(
                ParIdOffset.class.getConstructor(), 
                10, 
                "./dados/pessoa_comanda/pessoa_comanda.pkhash_d.db", 
                "./dados/pessoa_comanda/pessoa_comanda.pkhash_b.db"
        );
        
        this.pciDAO = new Pessoa_Comanda_ItemDAO();
        
        rebuildIndex();
    }

    private void rebuildIndex() throws Exception {
        final int[] contadores = {0, 0};
        
        arqPessoaComanda.scanValidRecords((pos, obj) -> {
            try {
                PessoaComanda pc = (PessoaComanda) obj;
                boolean success = idxPk.create(new ParIdOffset(pc.getId(), pos));
                if (!success) {
                    success = idxPk.update(new ParIdOffset(pc.getId(), pos));
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

    public int incluir(PessoaComanda p) throws Exception {
        if (buscar(p.getId()) != null) {
            throw new Exception("PessoaComanda com ID " + p.getId() + " já existe.");
        }

        long off = arqPessoaComanda.createWithOffset(p);
        if (off < 0) return -1;
        
        idxPk.create(new ParIdOffset(p.getId(), off));
        return p.getId();
    }

    public PessoaComanda buscar(int id) throws Exception {
        try {
            ParIdOffset ref = idxPk.read(id);
            if (ref != null) {
                PessoaComanda pc = (PessoaComanda) arqPessoaComanda.readAt(ref.getOffset());
                if (pc != null && pc.getId() == id) {
                    return pc;
                }
            }
        } catch (Exception e) {
        }
        
        return buscarSequencial(id);
    }
    
    private PessoaComanda buscarSequencial(int id) throws Exception {
        final PessoaComanda[] encontrado = {null};
        arqPessoaComanda.scanValidRecords((pos, obj) -> {
            PessoaComanda pc = (PessoaComanda) obj;
            if (pc.getId() == id) {
                encontrado[0] = pc;
            }
        });
        return encontrado[0];
    }

    public boolean atualizar(PessoaComanda p) throws Exception {
        long off = arqPessoaComanda.updateWithOffset(p);
        if (off < 0) return false;
        
        ParIdOffset par = new ParIdOffset(p.getId(), off);
        if (!idxPk.update(par)) {
            idxPk.create(par);
        }
        return true;
    }

    public boolean excluir(int id) throws Exception {
        // Verifica se existem relações em Pessoa_Comanda_Item antes de excluir
        List<Pessoa_Comanda_Item> relacoes = pciDAO.buscarPorPessoaComanda(id);
        if (!relacoes.isEmpty()) {
            throw new Exception("Não é possível excluir PessoaComanda. Existem " + relacoes.size() + 
                              " registro(s) relacionados em Pessoa_Comanda_Item.");
        }
        
        boolean ok = arqPessoaComanda.delete(id);
        if (ok) {
            idxPk.delete(id);
        }
        return ok;
    }

    // Métodos para gerenciar os itens consumidos pela PessoaComanda
    public List<Integer> getItensCompradosPorPessoaComanda(int idPessoaComanda) throws Exception {
        return pciDAO.buscarItensUnicosPorPessoaComanda(idPessoaComanda);
    }
    
    public List<Pessoa_Comanda_Item> getRelacoesDaPessoaComanda(int idPessoaComanda) throws Exception {
        return pciDAO.buscarPorPessoaComanda(idPessoaComanda);
    }
    
    public boolean adicionarItemAPessoaComanda(int idPessoaComanda, int idComanda, int idItem) throws Exception {
        Pessoa_Comanda_Item pci = new Pessoa_Comanda_Item();
        pci.setIdPessoaComanda(idPessoaComanda);
        pci.setIdComanda(idComanda);
        pci.setIdItem(idItem);
        
        return pciDAO.incluirPessoa_Comanda_Item(pci);
    }
    
    public boolean removerItemDaPessoaComanda(int idPessoaComanda, int idComanda, int idItem) throws Exception {
        return pciDAO.excluirPessoa_Comanda_Item(idPessoaComanda, idComanda, idItem);
    }
    
    public List<PessoaComanda> listarPessoasComanda() throws Exception {
        List<PessoaComanda> lista = new ArrayList<>();
        
        arqPessoaComanda.scanValidRecords((pos, obj) -> {
            PessoaComanda pc = (PessoaComanda) obj;
            lista.add(pc);
        });
        
        return lista;
    }
    
    public List<PessoaComanda> buscarPorComanda(int idComanda) throws Exception {
        List<PessoaComanda> resultados = new ArrayList<>();
        arqPessoaComanda.scanValidRecords((pos, obj) -> {
            PessoaComanda pc = (PessoaComanda) obj;
            if (pc.getIdComanda() == idComanda) {
                resultados.add(pc);
            }
        });
        return resultados;
    }

    public void fechar() throws Exception {
        arqPessoaComanda.close();
        pciDAO.fechar();
    }
}