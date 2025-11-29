package com.calculafast.dao;

import com.calculafast.index.hash.HashExtensivel;
import com.calculafast.index.hash.ParChaveHead;
import com.calculafast.index.inverted.InvertedList;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import com.calculafast.model.Arquivo;
import com.calculafast.model.Comanda;
import com.calculafast.model.Pessoa_Comanda_Item;

public class ComandaDAO {
    private Arquivo<Comanda> arqComandas;
    private HashExtensivel<ParChaveHead> idxPessoaHead; // pessoaId -> head lista
    private InvertedList listaPessoaComandas; // nós da lista invertida
    private Pessoa_Comanda_ItemDAO pciDAO; //para tabela intermediaria e relacionamento n:n
    
    // Relacionamento 1:N Comanda -> Pessoas (usando hash extensível + lista invertida)
    private HashExtensivel<ParChaveHead> idxComandaPessoaHead; // comandaId -> head lista
    private InvertedList listaComandaPessoas; // nós da lista invertida de pessoas

  public ComandaDAO() throws Exception {
    arqComandas = new Arquivo<>("comandas", Comanda.class.getConstructor());
    initIndices("./dados/comandas/comandas");
    this.pciDAO = null;
    rebuildIndices();
}
public void setPciDAO(Pessoa_Comanda_ItemDAO pciDAO) {
        this.pciDAO = pciDAO;
        try {
            rebuildIndices(); 
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public ComandaDAO(Arquivo<Comanda> arqComandas) throws Exception {
        this.arqComandas = arqComandas;
        String base = arqComandas.getNomeArquivo().replace(".db", "");
        initIndices(base);
        rebuildIndices();
    }

    private void initIndices(String base) throws Exception {
        idxPessoaHead = new HashExtensivel<>(ParChaveHead.class.getConstructor(), 8,
                base + ".pessoa_head_d.db", base + ".pessoa_head_b.db");
        listaPessoaComandas = new InvertedList(base + ".pessoa_list.db");
       // bptPessoa = new BPlusTree(base + ".pessoa_bpt.db");
        
        // Inicializa índices para relacionamento Comanda -> Pessoas (1:N)
        idxComandaPessoaHead = new HashExtensivel<>(ParChaveHead.class.getConstructor(), 8,
                base + ".comanda_pessoa_head_d.db", base + ".comanda_pessoa_head_b.db");
        listaComandaPessoas = new InvertedList(base + ".comanda_pessoa_list.db");
    }

      private void rebuildIndices() throws Exception {
        // Limpeza simples
        listaPessoaComandas.clear();
        listaComandaPessoas.clear();
        
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
                /* ignora */ }
        });
         if (pciDAO != null) {
            try {
                List<Pessoa_Comanda_Item> todasRelacoes = pciDAO.buscarTodos();
                for (Pessoa_Comanda_Item pci : todasRelacoes) {
                    int comandaId = pci.getIdComanda();
                    int pessoaId = pci.getIdPessoaComanda();
                    
                    if (!pessoaJaNaComanda(comandaId, pessoaId)) {
                        ParChaveHead cph = idxComandaPessoaHead.read(Math.abs(comandaId));
                        long head = cph == null ? -1L : cph.getHead();
                        long novoHead = listaComandaPessoas.prepend(pessoaId, head);
                        
                        if (cph == null)
                            idxComandaPessoaHead.create(new ParChaveHead(comandaId, novoHead));
                        else {
                            cph.setHead(novoHead);
                            idxComandaPessoaHead.update(cph);
                        }
                    }
                }
            } catch (Exception e) {
                // Se buscarTodos falhar, ignora 
                System.out.println("Aviso: Não foi possível reconstruir relacionamentos: " + e.getMessage());
            }
        }
    }

    private boolean pessoaJaNaComanda(int comandaId, int pessoaId) throws Exception {
        ParChaveHead cph = idxComandaPessoaHead.read(Math.abs(comandaId));
        if (cph == null) return false;
        
        long pos = cph.getHead();
        HashSet<Long> visitados = new HashSet<>();
        
        while (pos != -1L) {
            if (visitados.contains(pos)) break;
            visitados.add(pos);
            
            InvertedList.Node n = listaComandaPessoas.readAt(pos);
            if (n == null) break;
            if (n.childId == pessoaId) return true;
            
            pos = n.next;
        }
        return false;
    }

    public Comanda buscarComanda(int id) throws Exception {
        return arqComandas.read(id);
    }

    public boolean incluirComanda(Comanda comanda) throws Exception {
    long off = arqComandas.createWithOffset(comanda);
    if (off < 0)
        return false;
    
    // Atualiza índice Pessoa -> Comandas
    int pessoaId = comanda.getIdPessoa();
    ParChaveHead ph = idxPessoaHead.read(Math.abs(pessoaId));
    long head = ph == null ? -1L : ph.getHead();
    long novoHead = listaPessoaComandas.prepend(comanda.getId(), head);
    if (ph == null)
        idxPessoaHead.create(new ParChaveHead(pessoaId, novoHead));
    else {
        ph.setHead(novoHead);
        idxPessoaHead.update(ph);
    }
    
    return true;
}

    public boolean alterarComanda(Comanda comanda) throws Exception {
        // Reindexa se idPessoa mudar: remove do antigo e insere no novo
        Comanda antigo = arqComandas.read(comanda.getId());
        boolean ok = arqComandas.update(comanda);
        if (!ok) return false;
        if (antigo != null && antigo.getIdPessoa() != comanda.getIdPessoa()) {
            int oldPid = antigo.getIdPessoa();
            ParChaveHead phOld = idxPessoaHead.read(Math.abs(oldPid));
            long head = phOld == null ? -1L : phOld.getHead();
            long prev = -1L, cur = head;
            while (cur != -1L) {
                InvertedList.Node n = listaPessoaComandas.readAt(cur);
                if (n == null) break;
                if (n.childId == comanda.getId()) {
                    long next = n.next;
                    if (prev == -1L) { if (phOld != null) { phOld.setHead(next); idxPessoaHead.update(phOld); } }
                    else { listaPessoaComandas.updateNext(prev, next); }
                    break;
                }
                prev = cur; cur = n.next;
            }
            // adiciona ao novo pai
            int newPid = comanda.getIdPessoa();
            ParChaveHead phNew = idxPessoaHead.read(Math.abs(newPid));
            long newHead = phNew == null ? -1L : phNew.getHead();
            long novo = listaPessoaComandas.prepend(comanda.getId(), newHead);
            if (phNew == null) idxPessoaHead.create(new ParChaveHead(newPid, novo));
            else { phNew.setHead(novo); idxPessoaHead.update(phNew); }
          //  bptPessoa.put(newPid, novo);
        }
        return true;
    }

    public boolean excluirComanda(int id) throws Exception {
        List<Pessoa_Comanda_Item> relacoes = pciDAO.buscarPorComanda(id);
        if (!relacoes.isEmpty()) {
            throw new Exception("Não é possível excluir comanda. Existem " + relacoes.size() + 
                              " registro(s) relacionados em Pessoa_Comanda_Item. " +
                              "Exclua os registros relacionados primeiro.");
        }
        
        // desvincula da lista invertida (Pessoa -> Comandas)
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
                    if (prev == -1L) { if (ph != null) { ph.setHead(next); idxPessoaHead.update(ph); } }
                    else { listaPessoaComandas.updateNext(prev, next); }
                    break;
                }
                prev = cur; cur = n.next;
            }
            
            // Remove a lista de pessoas da comanda (Comanda -> Pessoas)
            ParChaveHead cph = idxComandaPessoaHead.read(Math.abs(id));
            if (cph != null) {
                // Remove o índice do hash (a lista ficará órfã mas não será mais acessada)
                idxComandaPessoaHead.delete(Math.abs(id));
            }
        }
        return arqComandas.delete(id);
    }

    // Listagem: retorna ids de comandas onde uma pessoa faz parte (usando relacionamento 1:N)
    // Percorre todas as comandas e verifica se a pessoa está na lista de pessoas da comanda
    public java.util.List<Integer> listarPorPessoa(int pessoaId) throws Exception {
        java.util.ArrayList<Integer> out = new java.util.ArrayList<>();
        java.util.HashSet<Integer> comandasVerificadas = new java.util.HashSet<>();
        
        // Usa Pessoa_Comanda_Item para encontrar todas as comandas onde a pessoa aparece
        List<Pessoa_Comanda_Item> relacoes = pciDAO.buscarPorPessoaComanda(pessoaId);
        
        for (Pessoa_Comanda_Item pci : relacoes) {
            int comandaId = pci.getIdComanda();
            
            // Evita duplicatas
            if (comandasVerificadas.contains(comandaId)) {
                continue;
            }
            comandasVerificadas.add(comandaId);
            
            // Verifica se a pessoa realmente está na lista invertida da comanda (relacionamento 1:N)
            List<Integer> pessoasDaComanda = listarPessoasPorComanda(comandaId);
            if (pessoasDaComanda.contains(pessoaId)) {
                // Verifica se a comanda ainda existe e está ativa
                Comanda c = arqComandas.read(comandaId);
                if (c != null) {
                    out.add(comandaId);
                }
            }
        }
        
        return out;
    }

    //busca todos os itens de uma comanda
     public List<Integer> getItensDaComanda(int idComanda) throws Exception {
        List<Integer> itens = new java.util.ArrayList<>();
        List<Pessoa_Comanda_Item> relacoes = pciDAO.buscarPorComanda(idComanda);
        
        for (Pessoa_Comanda_Item relacao : relacoes) {
            if (!itens.contains(relacao.getIdItem())) {
                itens.add(relacao.getIdItem());
            }
        }
        return itens;
    }

    //busca todas as pessoas de uma comanda (usando hash extensível + lista invertida)
    public List<Integer> getPessoasDaComanda(int idComanda) throws Exception {
        return listarPessoasPorComanda(idComanda);
    }
    
    public List<Integer> listarPessoasPorComanda(int comandaId) throws Exception {
    ArrayList<Integer> out = new ArrayList<>();
    HashSet<Long> visitados = new HashSet<>();
    HashSet<Integer> pessoasAdicionadas = new HashSet<>(); // Evita duplicatas
    
    ParChaveHead cph = idxComandaPessoaHead.read(Math.abs(comandaId));
    if (cph == null) {
        // Se não tem índice, tenta buscar de Pessoa_Comanda_Item
        List<Pessoa_Comanda_Item> relacoes = pciDAO.buscarPorComanda(comandaId);
        for (Pessoa_Comanda_Item pci : relacoes) {
            if (pessoasAdicionadas.add(pci.getIdPessoaComanda())) {
                out.add(pci.getIdPessoaComanda());
            }
        }
        return out;
    }
    
    long pos = cph.getHead();
    
    while (pos != -1L) {
        if (visitados.contains(pos)) {
            break;
        }
        visitados.add(pos);
        
        InvertedList.Node n = listaComandaPessoas.readAt(pos);
        if (n == null)
            break;
        
        if (pessoasAdicionadas.add(n.childId)) {
            out.add(n.childId);
        }
        
        pos = n.next;
    }
    
    return out;
}
    // Adiciona uma pessoa a uma comanda (atualiza lista invertida)
    public boolean adicionarPessoaAComanda(int comandaId, int pessoaId) throws Exception {
        // Verifica se já existe para evitar duplicatas
        if (pessoaJaNaComanda(comandaId, pessoaId)) {
            return false; // Já existe
        }
        
        ParChaveHead cph = idxComandaPessoaHead.read(Math.abs(comandaId));
        long head = cph == null ? -1L : cph.getHead();
        long novoHead = listaComandaPessoas.prepend(pessoaId, head);
        
        if (cph == null)
            idxComandaPessoaHead.create(new ParChaveHead(comandaId, novoHead));
        else {
            cph.setHead(novoHead);
            idxComandaPessoaHead.update(cph);
        }
        
        return true;
    }
    
    // Remove uma pessoa de uma comanda (atualiza lista invertida)
    public boolean removerPessoaDeComanda(int comandaId, int pessoaId) throws Exception {
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
            
            if (n.childId == pessoaId) {
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
    
    // Sincroniza a lista de pessoas de uma comanda baseado em Pessoa_Comanda_Item
    // Útil para manter o índice atualizado quando Pessoa_Comanda_Item é modificado
    public void sincronizarPessoasDaComanda(int comandaId) throws Exception {
        // Remove a lista atual
        ParChaveHead cph = idxComandaPessoaHead.read(Math.abs(comandaId));
        if (cph != null) {
            idxComandaPessoaHead.delete(Math.abs(comandaId));
        }
        
        // Reconstrói baseado em Pessoa_Comanda_Item
        List<Pessoa_Comanda_Item> relacoes = pciDAO.buscarPorComanda(comandaId);
        HashSet<Integer> pessoasUnicas = new HashSet<>();
        
        for (Pessoa_Comanda_Item pci : relacoes) {
            pessoasUnicas.add(pci.getIdPessoaComanda());
        }
        
        // Adiciona cada pessoa única à lista
        for (Integer pessoaId : pessoasUnicas) {
            adicionarPessoaAComanda(comandaId, pessoaId);
        }
    }
}

