package com.calculafast.dao;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import com.calculafast.index.hash.HashExtensivel;
import com.calculafast.index.hash.ParChaveHead;
import com.calculafast.index.inverted.InvertedList;
import com.calculafast.model.Arquivo;
import com.calculafast.model.Comanda;
import com.calculafast.model.Pessoa_Comanda_Item;


public class ComandaDAO {
    private Arquivo<Comanda> arqComandas;
    
    // Relacionamento N:1 - Comanda pertence a uma Pessoa (dono da comanda)
    private HashExtensivel<ParChaveHead> idxPessoaHead; 
    private InvertedList listaPessoaComandas; // nós da lista invertida
    
    // Relacionamento N:N - Pessoas que consumiram itens na comanda
    private HashExtensivel<ParChaveHead> idxComandaPessoaHead; 
    private InvertedList listaComandaPessoas; 
    
    private Pessoa_Comanda_ItemDAO pciDAO; 
    private PessoaDAO pessoaDAO; // Para verificação de integridade

    public ComandaDAO() throws Exception {
        arqComandas = new Arquivo<>("comandas", Comanda.class.getConstructor());
        initIndices("./dados/comandas/comandas");
        this.pciDAO = null;
        this.pessoaDAO = null; 
        rebuildIndices();
    }
    
    public void setPessoaDAO(PessoaDAO pessoaDAO) {
        this.pessoaDAO = pessoaDAO;
    }
    
    public void setPciDAO(Pessoa_Comanda_ItemDAO pciDAO) {
        this.pciDAO = pciDAO;
        try {
            rebuildIndices(); 
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private PessoaDAO getPessoaDAO() throws Exception {
        if (pessoaDAO == null) {
            pessoaDAO = new PessoaDAO();
        }
        return pessoaDAO;
    }
    
    
    private Pessoa_Comanda_ItemDAO getPciDAO() throws Exception {
        if (pciDAO == null) {
            pciDAO = new Pessoa_Comanda_ItemDAO();
        }
        return pciDAO;
    }

    public ComandaDAO(Arquivo<Comanda> arqComandas) throws Exception {
        this.arqComandas = arqComandas;
        String base = arqComandas.getNomeArquivo().replace(".db", "");
        initIndices(base);
        rebuildIndices();
    }

    private void initIndices(String base) throws Exception {
        
        idxPessoaHead = new HashExtensivel<>(
            ParChaveHead.class.getConstructor(), 
            8,
            base + ".pessoa_head_d.db", 
            base + ".pessoa_head_b.db"
        );
        listaPessoaComandas = new InvertedList(base + ".pessoa_list.db");
        idxComandaPessoaHead = new HashExtensivel<>(
            ParChaveHead.class.getConstructor(), 
            8,
            base + ".comanda_pessoa_head_d.db", 
            base + ".comanda_pessoa_head_b.db"
        );
        listaComandaPessoas = new InvertedList(base + ".comanda_pessoa_list.db");
    }

    private void rebuildIndices() throws Exception {
       
        
        listaPessoaComandas.clear();
        listaComandaPessoas.clear();
        
        // Reconstrói índice Pessoa -> Comandas (dono)
        arqComandas.scanValidRecords((pos, obj) -> {
            try {
                Comanda c = (Comanda) obj;
                int pessoaId = c.getIdPessoa();
                ParChaveHead ph = idxPessoaHead.read(Math.abs(pessoaId));
                long head = ph == null ? -1L : ph.getHead();
                long novoHead = listaPessoaComandas.prepend(c.getId(), head);
                
                if (ph == null)
                    idxPessoaHead.create(new ParChaveHead(pessoaId, novoHead));
                else {
                    ph.setHead(novoHead);
                    idxPessoaHead.update(ph);
                }
            } catch (Exception e) {
                System.err.println("Erro ao reconstruir índice de comanda: " + e.getMessage());
            }
        });
        
        // Reconstrói índice Comanda -> Pessoas (consumidores)
        if (pciDAO != null) {
            try {
                List<Pessoa_Comanda_Item> todasRelacoes = pciDAO.buscarTodos();
                for (Pessoa_Comanda_Item pci : todasRelacoes) {
                    int comandaId = pci.getIdComanda();
                    int pessoaComandaId = pci.getIdPessoaComanda();
                    
                    if (!pessoaJaNaComanda(comandaId, pessoaComandaId)) {
                        adicionarPessoaComandaAComanda(comandaId, pessoaComandaId);
                    }
                }
            } catch (Exception e) {
                System.out.println("Aviso: Não foi possível reconstruir relacionamentos N:N: " + e.getMessage());
            }
        }
        
    }

    public Comanda buscarComanda(int id) throws Exception {
        return arqComandas.read(id);
    }

    public boolean incluirComanda(Comanda comanda) throws Exception {
        if (getPessoaDAO().buscarPessoa(comanda.getIdPessoa()) == null) {
            throw new Exception("Erro de Integridade: Pessoa com ID " + 
                              comanda.getIdPessoa() + " não existe. " +
                              "Não é possível criar uma comanda sem um dono válido.");
        }
        
        long off = arqComandas.createWithOffset(comanda);
        if (off < 0) {
            return false;
        }
        
        int pessoaId = comanda.getIdPessoa();
        ParChaveHead ph = idxPessoaHead.read(Math.abs(pessoaId));
        long head = ph == null ? -1L : ph.getHead();
        long novoHead = listaPessoaComandas.prepend(comanda.getId(), head);
        
        if (ph == null) {
            idxPessoaHead.create(new ParChaveHead(pessoaId, novoHead));
        } else {
            ph.setHead(novoHead);
            idxPessoaHead.update(ph);
        }
        
        System.out.println("Comanda " + comanda.getId() + " criada com sucesso para pessoa " + pessoaId);
        return true;
    }

    public boolean alterarComanda(Comanda comanda) throws Exception {
        if (getPessoaDAO().buscarPessoa(comanda.getIdPessoa()) == null) {
            throw new Exception("Erro de Integridade: Pessoa com ID " + 
                              comanda.getIdPessoa() + " não existe.");
        }
        
        Comanda antigo = arqComandas.read(comanda.getId());
        boolean ok = arqComandas.update(comanda);
        if (!ok) return false;
        
        // Se mudou o dono (idPessoa), atualiza os índices
        if (antigo != null && antigo.getIdPessoa() != comanda.getIdPessoa()) {
           /*  System.out.println("Transferindo comanda " + comanda.getId() + 
                             " de pessoa " + antigo.getIdPessoa() + 
                             " para pessoa " + comanda.getIdPessoa());*/
            
            // Remove da lista do dono antigo
            int oldPid = antigo.getIdPessoa();
            ParChaveHead phOld = idxPessoaHead.read(Math.abs(oldPid));
            long head = phOld == null ? -1L : phOld.getHead();
            long prev = -1L, cur = head;
            
            while (cur != -1L) {
                InvertedList.Node n = listaPessoaComandas.readAt(cur);
                if (n == null) break;
                
                if (n.childId == comanda.getId()) {
                    long next = n.next;
                    if (prev == -1L) {
                        if (phOld != null) {
                            phOld.setHead(next);
                            idxPessoaHead.update(phOld);
                        }
                    } else {
                        listaPessoaComandas.updateNext(prev, next);
                    }
                    break;
                }
                prev = cur;
                cur = n.next;
            }
            
            int newPid = comanda.getIdPessoa();
            ParChaveHead phNew = idxPessoaHead.read(Math.abs(newPid));
            long newHead = phNew == null ? -1L : phNew.getHead();
            long novo = listaPessoaComandas.prepend(comanda.getId(), newHead);
            
            if (phNew == null) {
                idxPessoaHead.create(new ParChaveHead(newPid, novo));
            } else {
                phNew.setHead(novo);
                idxPessoaHead.update(phNew);
            }
        }
        
        return true;
    }

    public boolean excluirComanda(int id) throws Exception {
        // Verifica se existem consumos registrados nesta comanda
        List<Pessoa_Comanda_Item> relacoes = getPciDAO().buscarPorComanda(id);
        if (!relacoes.isEmpty()) {
            throw new Exception("Não é possível excluir comanda. Existem " + relacoes.size() + 
                              " registro(s) de consumo em Pessoa_Comanda_Item. " +
                              "Exclua os registros relacionados primeiro.");
        }
        
        Comanda c = arqComandas.read(id);
        if (c != null) {
            int pid = c.getIdPessoa();
            ParChaveHead ph = idxPessoaHead.read(Math.abs(pid));
            long head = ph == null ? -1L : ph.getHead();
            long prev = -1L, cur = head;
            
            while (cur != -1L) {
                InvertedList.Node n = listaPessoaComandas.readAt(cur);
                if (n == null) break;
                
                if (n.childId == id) {
                    long next = n.next;
                    if (prev == -1L) {
                        if (ph != null) {
                            ph.setHead(next);
                            idxPessoaHead.update(ph);
                        }
                    } else {
                        listaPessoaComandas.updateNext(prev, next);
                    }
                    break;
                }
                prev = cur;
                cur = n.next;
            }
            
            // Remove a lista de consumidores da comanda
            ParChaveHead cph = idxComandaPessoaHead.read(Math.abs(id));
            if (cph != null) {
                idxComandaPessoaHead.delete(Math.abs(id));
            }
        }
        
        return arqComandas.delete(id);
    }

    /**
     * Lista todas as comandas de uma pessoa (como dono)
     */
    public List<Integer> listarComandasDaPessoa(int pessoaId) throws Exception {
        ArrayList<Integer> out = new ArrayList<>();
        HashSet<Long> visitados = new HashSet<>();
        
        ParChaveHead ph = idxPessoaHead.read(Math.abs(pessoaId));
        if (ph == null) return out;
        
        long pos = ph.getHead();
        while (pos != -1L) {
            if (visitados.contains(pos)) break;
            visitados.add(pos);
            
            InvertedList.Node n = listaPessoaComandas.readAt(pos);
            if (n == null) break;
            
            out.add(n.childId);
            pos = n.next;
        }
        
        return out;
    }

    /**
     * Lista comandas onde a PessoaComanda consumiu itens
     */
    public List<Integer> listarPorPessoaComanda(int pessoaComandaId) throws Exception {
        ArrayList<Integer> out = new ArrayList<>();
        HashSet<Integer> comandasVerificadas = new HashSet<>();
        
        List<Pessoa_Comanda_Item> relacoes = getPciDAO().buscarPorPessoaComanda(pessoaComandaId);
        
        for (Pessoa_Comanda_Item pci : relacoes) {
            int comandaId = pci.getIdComanda();
            
            if (comandasVerificadas.contains(comandaId)) {
                continue;
            }
            comandasVerificadas.add(comandaId);
            
            Comanda c = arqComandas.read(comandaId);
            if (c != null) {
                out.add(comandaId);
            }
        }
        
        return out;
    }

    /**
     * Busca todos os itens de uma comanda
     */
    public List<Integer> getItensDaComanda(int idComanda) throws Exception {
        List<Integer> itens = new ArrayList<>();
        List<Pessoa_Comanda_Item> relacoes = getPciDAO().buscarPorComanda(idComanda);
        
        for (Pessoa_Comanda_Item relacao : relacoes) {
            if (!itens.contains(relacao.getIdItem())) {
                itens.add(relacao.getIdItem());
            }
        }
        return itens;
    }

    /**
     * Lista todas as PessoasComanda que consumiram itens nesta comanda
     */
    public List<Integer> getPessoasComandasDaComanda(int idComanda) throws Exception {
        return listarPessoasComandasPorComanda(idComanda);
    }
    
    /**
     * Lista todas as PessoasComanda que consumiram na comanda
     */
    public List<Integer> listarPessoasComandasPorComanda(int comandaId) throws Exception {
        ArrayList<Integer> out = new ArrayList<>();
        HashSet<Long> visitados = new HashSet<>();
        HashSet<Integer> pessoasAdicionadas = new HashSet<>();
        
        ParChaveHead cph = idxComandaPessoaHead.read(Math.abs(comandaId));
        if (cph == null) {
            // Se não tem índice, busca de Pessoa_Comanda_Item
            List<Pessoa_Comanda_Item> relacoes = getPciDAO().buscarPorComanda(comandaId);
            for (Pessoa_Comanda_Item pci : relacoes) {
                if (pessoasAdicionadas.add(pci.getIdPessoaComanda())) {
                    out.add(pci.getIdPessoaComanda());
                }
            }
            return out;
        }
        
        long pos = cph.getHead();
        while (pos != -1L) {
            if (visitados.contains(pos)) break;
            visitados.add(pos);
            
            InvertedList.Node n = listaComandaPessoas.readAt(pos);
            if (n == null) break;
            
            if (pessoasAdicionadas.add(n.childId)) {
                out.add(n.childId);
            }
            
            pos = n.next;
        }
        
        return out;
    }
    
    /**
     * Verifica se uma PessoaComanda já consumiu nesta comanda
     */
    private boolean pessoaJaNaComanda(int comandaId, int pessoaComandaId) throws Exception {
        ParChaveHead cph = idxComandaPessoaHead.read(Math.abs(comandaId));
        if (cph == null) return false;
        
        long pos = cph.getHead();
        HashSet<Long> visitados = new HashSet<>();
        
        while (pos != -1L) {
            if (visitados.contains(pos)) break;
            visitados.add(pos);
            
            InvertedList.Node n = listaComandaPessoas.readAt(pos);
            if (n == null) break;
            if (n.childId == pessoaComandaId) return true;
            
            pos = n.next;
        }
        return false;
    }
    
    /**
     * Adiciona uma PessoaComanda à lista de consumidores da comanda
     * Chamado automaticamente quando um registro é inserido em Pessoa_Comanda_Item
     */
    public boolean adicionarPessoaComandaAComanda(int comandaId, int pessoaComandaId) throws Exception {
        if (pessoaJaNaComanda(comandaId, pessoaComandaId)) {
            return false; // Já existe
        }
        
        ParChaveHead cph = idxComandaPessoaHead.read(Math.abs(comandaId));
        long head = cph == null ? -1L : cph.getHead();
        long novoHead = listaComandaPessoas.prepend(pessoaComandaId, head);
        
        if (cph == null) {
            idxComandaPessoaHead.create(new ParChaveHead(comandaId, novoHead));
        } else {
            cph.setHead(novoHead);
            idxComandaPessoaHead.update(cph);
        }
        
        return true;
    }
    
    /**
     * Remove uma PessoaComanda da lista de consumidores
     */
    public boolean removerPessoaComandaDeComanda(int comandaId, int pessoaComandaId) throws Exception {
        ParChaveHead cph = idxComandaPessoaHead.read(Math.abs(comandaId));
        if (cph == null) return false;
        
        long head = cph.getHead();
        long prev = -1L, cur = head;
        HashSet<Long> visitados = new HashSet<>();
        
        while (cur != -1L) {
            if (visitados.contains(cur)) break;
            visitados.add(cur);
            
            InvertedList.Node n = listaComandaPessoas.readAt(cur);
            if (n == null) break;
            
            if (n.childId == pessoaComandaId) {
                long next = n.next;
                if (prev == -1L) {
                    if (cph != null) {
                        cph.setHead(next);
                        idxComandaPessoaHead.update(cph);
                    }
                } else {
                    listaComandaPessoas.updateNext(prev, next);
                }
                return true;
            }
            
            prev = cur;
            cur = n.next;
        }
        
        return false;
    }
    
    /**
     * Sincroniza a lista de PessoasComanda baseado em Pessoa_Comanda_Item
     */
    public void sincronizarPessoasComandasDaComanda(int comandaId) throws Exception {
        // Remove a lista atual
        ParChaveHead cph = idxComandaPessoaHead.read(Math.abs(comandaId));
        if (cph != null) {
            idxComandaPessoaHead.delete(Math.abs(comandaId));
        }
        
        // Reconstrói baseado em Pessoa_Comanda_Item
        List<Pessoa_Comanda_Item> relacoes = getPciDAO().buscarPorComanda(comandaId);
        HashSet<Integer> pessoasUnicas = new HashSet<>();
        
        for (Pessoa_Comanda_Item pci : relacoes) {
            pessoasUnicas.add(pci.getIdPessoaComanda());
        }
        
        // Adiciona cada PessoaComanda única à lista
        for (Integer pessoaComandaId : pessoasUnicas) {
            adicionarPessoaComandaAComanda(comandaId, pessoaComandaId);
        }
    }
    
    public void fechar() throws Exception {
        arqComandas.close();
        if (pciDAO != null) {
            pciDAO.fechar();
        }
    }
}