package dao;

import index.bptree.BPlusTree;
import index.hash.HashExtensivel;
import index.hash.ParChaveHead;
import index.inverted.InvertedList;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import model.Arquivo;
import model.Pagamento;

public class PagamentoDAO {

    private Arquivo<Pagamento> arqPagamento;
    private HashExtensivel<ParChaveHead> idxComandaHead; // comandaId -> head lista
    private InvertedList listaComandaPagamentos;
    private BPlusTree bptValor; // indexada por valor

    public PagamentoDAO() throws Exception {
        arqPagamento = new Arquivo<>("Pagamentos", Pagamento.class.getConstructor());
        initIndices("./dados/Pagamentos/Pagamentos");
        rebuildIndices();
    }

    public PagamentoDAO(Arquivo<Pagamento> arqPagamento) throws Exception {
        this.arqPagamento = arqPagamento;
        String base = arqPagamento.getNomeArquivo().replace(".db", "");
        initIndices(base);
        rebuildIndices();
    }

    private void initIndices(String base) throws Exception {
        idxComandaHead = new HashExtensivel<>(ParChaveHead.class.getConstructor(), 8,
                base + ".comanda_head_d.db", base + ".comanda_head_b.db");
        listaComandaPagamentos = new InvertedList(base + ".comanda_list.db");
        bptValor = new BPlusTree(base + ".valor_bpt.db"); // Indexa por VALOR
    }

    private void rebuildIndices() throws Exception {
        listaComandaPagamentos.clear();
        arqPagamento.scanValidRecords((pos, obj) -> {
            try {
                Pagamento p = (Pagamento) obj;
                int comandaId = p.getIdComanda();
                
                // Atualiza lista invertida de comandas
                ParChaveHead ph = idxComandaHead.read(Math.abs(comandaId));
                long head = ph == null ? -1L : ph.getHead();
                long novoHead = listaComandaPagamentos.prepend(p.getId(), head);
                
                if (ph == null)
                    idxComandaHead.create(new ParChaveHead(comandaId, novoHead));
                else {
                    ph.setHead(novoHead);
                    idxComandaHead.update(ph);
                }
                
                // Indexa por VALOR na B+ Tree
                int valorCentavos = (int)(p.getValorPago() * 100);
                bptValor.put(valorCentavos, p.getId());
            } catch (Exception e) {
                
            }
        });
    }

    public Pagamento buscarPagamento(int id) throws Exception {
        return arqPagamento.read(id);
    }

    public boolean incluirPagamento(Pagamento pagamento) throws Exception {
        long off = arqPagamento.createWithOffset(pagamento);
        if (off < 0)
            return false;
        
        int comandaId = pagamento.getIdComanda();
        int pagamentoId = pagamento.getId();
        
        //atualiza lista invertida de comandas (Hash + Lista)
        ParChaveHead ph = idxComandaHead.read(Math.abs(comandaId));
        long head = ph == null ? -1L : ph.getHead();
        long novoHead = listaComandaPagamentos.prepend(pagamentoId, head);
        
        if (ph == null)
            idxComandaHead.create(new ParChaveHead(comandaId, novoHead));
        else {
            ph.setHead(novoHead);
            idxComandaHead.update(ph);
        }
        
        //indexa por VALOR na B+ Tree
        int valorCentavos = (int)(pagamento.getValorPago() * 100);
        bptValor.put(valorCentavos, pagamentoId);
        
        return true;
    }

    public boolean alterarPagamento(Pagamento pagamento) throws Exception {
        Pagamento antigo = arqPagamento.read(pagamento.getId());
        boolean ok = arqPagamento.update(pagamento);
        if (!ok)
            return false;
            
        // Se mudou de comanda, reindexar
        if (antigo != null && antigo.getIdComanda() != pagamento.getIdComanda()) {
            int oldCid = antigo.getIdComanda();
            ParChaveHead phOld = idxComandaHead.read(Math.abs(oldCid));
            long head = phOld == null ? -1L : phOld.getHead();
            long prev = -1L, cur = head;
            
            while (cur != -1L) {
                InvertedList.Node n = listaComandaPagamentos.readAt(cur);
                if (n == null)
                    break;
                if (n.childId == pagamento.getId()) {
                    long next = n.next;
                    if (prev == -1L) {
                        if (phOld != null) {
                            phOld.setHead(next);
                            idxComandaHead.update(phOld);
                        }
                    } else {
                        listaComandaPagamentos.updateNext(prev, next);
                    }
                    break;
                }
                prev = cur;
                cur = n.next;
            }
            
            int newCid = pagamento.getIdComanda();
            ParChaveHead phNew = idxComandaHead.read(Math.abs(newCid));
            long newHead = phNew == null ? -1L : phNew.getHead();
            long novo = listaComandaPagamentos.prepend(pagamento.getId(), newHead);
            
            if (phNew == null)
                idxComandaHead.create(new ParChaveHead(newCid, novo));
            else {
                phNew.setHead(novo);
                idxComandaHead.update(phNew);
            }
        }
        
        // Atualiza indice de valor 
        int valorCentavos = (int)(pagamento.getValorPago() * 100);
        bptValor.put(valorCentavos, pagamento.getId());
        
        return true;
    }

    public boolean excluirPagamento(int id) throws Exception {
        Pagamento p = arqPagamento.read(id);
        if (p != null) {
            int cid = p.getIdComanda();
            ParChaveHead ph = idxComandaHead.read(Math.abs(cid));
            long head = ph == null ? -1L : ph.getHead();
            long prev = -1L, cur = head;
            
            while (cur != -1L) {
                InvertedList.Node n = listaComandaPagamentos.readAt(cur);
                if (n == null)
                    break;
                if (n.childId == id) {
                    long next = n.next;
                    if (prev == -1L) {
                        if (ph != null) {
                            ph.setHead(next);
                            idxComandaHead.update(ph);
                        }
                    } else {
                        listaComandaPagamentos.updateNext(prev, next);
                    }
                    break;
                }
                prev = cur;
                cur = n.next;
            }
        }
        return arqPagamento.delete(id);
    }

    // Lista pagamentos de uma comanda (usando Hash + Lista Invertida)
    public List<Integer> listarPorComanda(int comandaId) throws Exception {
        ArrayList<Integer> out = new ArrayList<>();
        HashSet<Long> visitados = new HashSet<>();
        
        ParChaveHead ph = idxComandaHead.read(Math.abs(comandaId));
        long pos = ph == null ? -1L : ph.getHead();
        
        while (pos != -1L) {
            // coreeção do loop circular
            if (visitados.contains(pos)) {
                System.err.println("ERRO: Loop circular detectado!");
                break;
            }
            visitados.add(pos);
            
            InvertedList.Node n = listaComandaPagamentos.readAt(pos);
            if (n == null)
                break;
            
            Pagamento p = arqPagamento.read(n.childId);
            if (p != null)
                out.add(n.childId);
            
            pos = n.next;
        }
        
        return out;
    }

    // busca pagamentos por intervalo de valores (usando B+ Tree)
    public List<Pagamento> buscarPorIntervaloValor(double valorMin, double valorMax) throws Exception {
        int minCentavos = (int)(valorMin * 100);
        int maxCentavos = (int)(valorMax * 100);
        
        List<int[]> resultados = bptValor.range(minCentavos, maxCentavos);
        List<Pagamento> pagamentos = new ArrayList<>();
        
        for (int[] par : resultados) {
            int pagamentoId = par[1];
            Pagamento p = arqPagamento.read(pagamentoId);
            if (p != null)
                pagamentos.add(p);
        }
        
        return pagamentos;
    }
}