package dao;

import model.Arquivo;
import model.Pagamento;
import index.hash.HashExtensivel;
import index.hash.ParChaveHead;
import index.inverted.InvertedList;
import index.bptree.BPlusTree;

public class PagamentoDAO {

    private Arquivo<Pagamento> arqPagamento;
    private HashExtensivel<ParChaveHead> idxComandaHead; // comandaId -> head lista
    private InvertedList listaComandaPagamentos;
    private BPlusTree bptComanda;

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
        bptComanda = new BPlusTree(base + ".comanda_bpt.db");
    }

    private void rebuildIndices() throws Exception {
        listaComandaPagamentos.clear();
        arqPagamento.scanValidRecords((pos, obj) -> {
            try {
                Pagamento p = (Pagamento) obj;
                int comandaId = p.getIdComanda();
                ParChaveHead ph = idxComandaHead.read(Math.abs(comandaId));
                long head = ph == null ? -1L : ph.getHead();
                long novoHead = listaComandaPagamentos.prepend(p.getId(), head);
                if (ph == null)
                    idxComandaHead.create(new ParChaveHead(comandaId, novoHead));
                else {
                    ph.setHead(novoHead);
                    idxComandaHead.update(ph);
                }
                bptComanda.put(comandaId, novoHead);
            } catch (Exception e) {
                /* ignore */ }
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
        ParChaveHead ph = idxComandaHead.read(Math.abs(comandaId));
        long head = ph == null ? -1L : ph.getHead();
        long novoHead = listaComandaPagamentos.prepend(pagamento.getId(), head);
        if (ph == null)
            idxComandaHead.create(new ParChaveHead(comandaId, novoHead));
        else {
            ph.setHead(novoHead);
            idxComandaHead.update(ph);
        }
        bptComanda.put(comandaId, novoHead);
        return true;
    }

    public boolean alterarPagamento(Pagamento pagamento) throws Exception {
        Pagamento antigo = arqPagamento.read(pagamento.getId());
        boolean ok = arqPagamento.update(pagamento);
        if (!ok)
            return false;
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
            bptComanda.put(newCid, novo);
        }
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

    public java.util.List<Integer> listarPorComanda(int comandaId) throws Exception {
        java.util.ArrayList<Integer> out = new java.util.ArrayList<>();
        ParChaveHead ph = idxComandaHead.read(Math.abs(comandaId));
        long pos = ph == null ? -1L : ph.getHead();
        while (pos != -1L) {
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
}
