package dao;

import model.Arquivo;
import model.Comanda;
import index.hash.HashExtensivel;
import index.hash.ParChaveHead;
import index.inverted.InvertedList;
import index.bptree.BPlusTree;

public class ComandaDAO {
    private Arquivo<Comanda> arqComandas;
    private HashExtensivel<ParChaveHead> idxPessoaHead; // pessoaId -> head lista
    private InvertedList listaPessoaComandas; // nós da lista invertida
    private BPlusTree bptPessoa; // ordenação por idPessoa

    public ComandaDAO() throws Exception {
        arqComandas = new Arquivo<>("comandas", Comanda.class.getConstructor());
        initIndices("./dados/comandas/comandas");
        rebuildIndices();
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
        bptPessoa = new BPlusTree(base + ".pessoa_bpt.db");
    }

    private void rebuildIndices() throws Exception {
        // limpeza simples
        listaPessoaComandas.clear();
        arqComandas.scanValidRecords((pos, obj) -> {
            try {
                Comanda c = (Comanda) obj;
                int pessoaId = c.getIdPessoa();
                // hash: obtém head atual
                ParChaveHead ph = idxPessoaHead.read(Math.abs(pessoaId));
                long head = ph == null ? -1L : ph.getHead();
                long novoHead = listaPessoaComandas.prepend(c.getId(), head);
                if (ph == null)
                    idxPessoaHead.create(new ParChaveHead(pessoaId, novoHead));
                else {
                    ph.setHead(novoHead);
                    idxPessoaHead.update(ph);
                }
                bptPessoa.put(pessoaId, novoHead);
            } catch (Exception e) {
                /* ignora */ }
        });
    }

    public Comanda buscarComanda(int id) throws Exception {
        return arqComandas.read(id);
    }

    public boolean incluirComanda(Comanda comanda) throws Exception {
        long off = arqComandas.createWithOffset(comanda);
        if (off < 0)
            return false;
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
        bptPessoa.put(pessoaId, novoHead);
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
            bptPessoa.put(newPid, novo);
        }
        return true;
    }

    public boolean excluirComanda(int id) throws Exception {
        // desvincula da lista invertida imediatamente e marca lápide nos dados
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
        }
        return arqComandas.delete(id);
    }

    // Listagem: retorna ids de comandas de uma pessoa percorrendo a lista invertida
    // via head do hash
    public java.util.List<Integer> listarPorPessoa(int pessoaId) throws Exception {
        java.util.ArrayList<Integer> out = new java.util.ArrayList<>();
        ParChaveHead ph = idxPessoaHead.read(Math.abs(pessoaId));
        long pos = ph == null ? -1L : ph.getHead();
        while (pos != -1L) {
            InvertedList.Node n = listaPessoaComandas.readAt(pos);
            if (n == null) break;
            // garante apenas ativos
            Comanda c = arqComandas.read(n.childId);
            if (c != null) out.add(n.childId);
            pos = n.next;
        }
        return out;
    }
}
