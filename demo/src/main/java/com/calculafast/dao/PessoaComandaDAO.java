package com.calculafast.dao;

import com.calculafast.model.Arquivo;
import com.calculafast.model.PessoaComanda;
import com.calculafast.model.Comanda;
import com.calculafast.model.Pessoa_Comanda_Item;
import com.calculafast.index.hash.HashExtensivel;
import com.calculafast.index.hash.ParIdOffset;
import java.util.List;
import java.util.ArrayList;

public class PessoaComandaDAO {
    private Arquivo<PessoaComanda> arqPessoaComanda;
    private HashExtensivel<ParIdOffset> idxPk;
    private Pessoa_Comanda_ItemDAO pciDAO;
    private ComandaDAO comandaDAO; // Adicionar referência ao ComandaDAO
    
    public PessoaComandaDAO() throws Exception {
        arqPessoaComanda = new Arquivo<>("pessoa_comanda", PessoaComanda.class.getConstructor());
        
        this.idxPk = new HashExtensivel<>(
                ParIdOffset.class.getConstructor(), 
                10, 
                "./dados/pessoa_comanda/pessoa_comanda.pkhash_d.db", 
                "./dados/pessoa_comanda/pessoa_comanda.pkhash_b.db"
        );
        
        this.pciDAO = new Pessoa_Comanda_ItemDAO();
        this.comandaDAO = null; // Será inicializado quando necessário
        
        rebuildIndex();
    }
    
    // Método para definir o ComandaDAO (para evitar dependência circular)
    public void setComandaDAO(ComandaDAO comandaDAO) {
        this.comandaDAO = comandaDAO;
    }
    
    // Método auxiliar para obter ComandaDAO
    private ComandaDAO getComandaDAO() throws Exception {
        if (comandaDAO == null) {
            comandaDAO = new ComandaDAO();
        }
        return comandaDAO;
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

    /**
     * 
     * VALIDA se a comanda existe e está com status aberta
     */
    public int incluir(PessoaComanda p) throws Exception {
        // Verifica se já existe
        if (buscar(p.getId()) != null) {
            throw new Exception("PessoaComanda com ID " + p.getId() + " já existe.");
        }
        
        // VALIDAÇÃO: Verifica se a comanda existe
        Comanda comanda = getComandaDAO().buscarComanda(p.getIdComanda());
        if (comanda == null) {
            throw new Exception("Erro de Integridade: Comanda com ID " + 
                              p.getIdComanda() + " não existe. " +
                              "Não é possível adicionar pessoa a uma comanda inexistente.");
        }
        
        // VALIDAÇÃO: Verifica se a comanda está aberta
        if (!"aberta".equalsIgnoreCase(comanda.getStatus())) {
            throw new Exception("Erro de Negócio: A comanda " + p.getIdComanda() + 
                              " está com status '" + comanda.getStatus() + "'. " +
                              "Só é possível adicionar pessoas a comandas com status 'aberta'.");
        }

        // Se passou nas validações, insere no banco
        long off = arqPessoaComanda.createWithOffset(p);
        if (off < 0) return -1;
        
        idxPk.create(new ParIdOffset(p.getId(), off));
        
        System.out.println("PessoaComanda " + p.getId() + 
                         " adicionada com sucesso à comanda " + p.getIdComanda());
        
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

    /**
     * Atualiza uma PessoaComanda
     * Se mudar de comanda, valida se a nova comanda está aberta
     */
    public boolean atualizar(PessoaComanda p) throws Exception {
        PessoaComanda antiga = buscar(p.getId());
        if (antiga == null) {
            throw new Exception("PessoaComanda com ID " + p.getId() + " não existe.");
        }
        
        // Se mudou de comanda, valida a nova comanda
        if (antiga.getIdComanda() != p.getIdComanda()) {
            Comanda novaComanda = getComandaDAO().buscarComanda(p.getIdComanda());
            
            if (novaComanda == null) {
                throw new Exception("Erro de Integridade: Comanda com ID " + 
                                  p.getIdComanda() + " não existe.");
            }
            
            if (!"aberta".equalsIgnoreCase(novaComanda.getStatus())) {
                throw new Exception("Erro de Negócio: A comanda " + p.getIdComanda() + 
                                  " está com status '" + novaComanda.getStatus() + "'. " +
                                  "Só é possível mover pessoas para comandas abertas.");
            }
            
            System.out.println("Movendo PessoaComanda " + p.getId() + 
                             " da comanda " + antiga.getIdComanda() + 
                             " para comanda " + p.getIdComanda());
        }
        
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
                              " registro(s) relacionados em Pessoa_Comanda_Item. " +
                              "Exclua os itens consumidos primeiro.");
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
    
    /**
     * Adiciona um item ao consumo de uma PessoaComanda
     * Valida se a comanda ainda está aberta
     */
    public boolean adicionarItemAPessoaComanda(int idPessoaComanda, int idComanda, int idItem) throws Exception {
        // Valida se a PessoaComanda existe
        PessoaComanda pc = buscar(idPessoaComanda);
        if (pc == null) {
            throw new Exception("PessoaComanda com ID " + idPessoaComanda + " não existe.");
        }
        
        // Valida se a comanda está aberta
        Comanda comanda = getComandaDAO().buscarComanda(idComanda);
        if (comanda == null) {
            throw new Exception("Comanda com ID " + idComanda + " não existe.");
        }
        
        if (!"aberta".equalsIgnoreCase(comanda.getStatus())) {
            throw new Exception("Não é possível adicionar itens. " +
                              "A comanda está com status '" + comanda.getStatus() + "'. " +
                              "Só comandas abertas podem receber novos itens.");
        }
        
        // Valida se a PessoaComanda pertence a essa comanda
        if (pc.getIdComanda() != idComanda) {
            throw new Exception("A PessoaComanda " + idPessoaComanda + 
                              " não pertence à comanda " + idComanda);
        }
        
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
    
    /**
     * Lista todas as PessoasComanda de uma comanda específica
     */
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
    
    /**
     * Calcula o consumo total de uma PessoaComanda
     * (soma dos preços dos itens consumidos)
     */
    public double calcularConsumoTotal(int idPessoaComanda) throws Exception {
        List<Pessoa_Comanda_Item> itens = pciDAO.buscarPorPessoaComanda(idPessoaComanda);
        double total = 0.0;
        
        // Você precisará buscar o preço de cada item
        // Isso depende de como sua classe Item está implementada
        for (Pessoa_Comanda_Item pci : itens) {
            // total += itemDAO.buscarItem(pci.getIdItem()).getPreco();
        }
        
        return total;
    }

    public void fechar() throws Exception {
        arqPessoaComanda.close();
        pciDAO.fechar();
    }
}