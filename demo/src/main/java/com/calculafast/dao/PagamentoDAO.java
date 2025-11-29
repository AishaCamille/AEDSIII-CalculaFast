package com.calculafast.dao;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import com.calculafast.index.bptree.BPlusTree;
import com.calculafast.index.hash.HashExtensivel;
import com.calculafast.index.hash.ParChaveHead;
import com.calculafast.index.inverted.InvertedList;
import com.calculafast.model.Arquivo;
import com.calculafast.model.Comanda;
import com.calculafast.model.Pagamento;
import com.calculafast.model.Pessoa;

public class PagamentoDAO {

    private Arquivo<Pagamento> arqPagamento;
    private HashExtensivel<ParChaveHead> idxComandaHead; // comandaId -> head lista
    private InvertedList listaComandaPagamentos;
    private BPlusTree bptValor; // indexada por valor


    //para verificação se existirem no banco
   private PessoaDAO pessoaDAO;
    private ComandaDAO comandaDAO;

    public PagamentoDAO() throws Exception {
        arqPagamento = new Arquivo<>("Pagamentos", Pagamento.class.getConstructor());
        initIndices("./dados/Pagamentos/Pagamentos");
        rebuildIndices();

         // Inicializa os DAOs para validação se existem no banco
        pessoaDAO = new PessoaDAO();
        comandaDAO = new ComandaDAO();
    }

    public PagamentoDAO(PessoaDAO pessoaDAO, ComandaDAO comandaDAO) throws Exception {
    arqPagamento = new Arquivo<>("Pagamentos", Pagamento.class.getConstructor());
    initIndices("./dados/Pagamentos/Pagamentos");
    
    this.pessoaDAO = pessoaDAO;
    this.comandaDAO = comandaDAO;
    
    rebuildIndices();
}

    private void initIndices(String base) throws Exception {
        idxComandaHead = new HashExtensivel<>(ParChaveHead.class.getConstructor(), 8,
                base + ".comanda_head_d.db", base + ".comanda_head_b.db");
        listaComandaPagamentos = new InvertedList(base + ".comanda_list.db");
        bptValor = new BPlusTree(base + ".valor_bpt.db"); 
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
         
        if (!validarPessoa(pagamento.getIdPessoa())) {
            System.out.println("ERRO: Pessoa com ID " + pagamento.getIdPessoa() + " não existe no banco!");
            return false;
        }
        
        if (!validarComanda(pagamento.getIdComanda())) {
            System.out.println("ERRO: Comanda com ID " + pagamento.getIdComanda() + " não existe no banco!");
            return false;
        }
        long off = arqPagamento.createWithOffset(pagamento);
        if (off < 0)
            return false;
        
        int comandaId = pagamento.getIdComanda();
        int pagamentoId = pagamento.getId();
        
        //atualiza lista invertida de comandas 
        ParChaveHead ph = idxComandaHead.read(Math.abs(comandaId));
        long head = ph == null ? -1L : ph.getHead();
        long novoHead = listaComandaPagamentos.prepend(pagamentoId, head);
        
        if (ph == null)
            idxComandaHead.create(new ParChaveHead(comandaId, novoHead));
        else {
            ph.setHead(novoHead);
            idxComandaHead.update(ph);
        }
        
        int valorCentavos = (int)(pagamento.getValorPago() * 100);
        bptValor.put(valorCentavos, pagamentoId);
        
        return true;
    }

    public boolean alterarPagamento(Pagamento pagamento) throws Exception {
         
        if (!validarPessoa(pagamento.getIdPessoa())) {
            System.out.println("ERRO: Pessoa com ID " + pagamento.getIdPessoa() + " não existe no banco!");
            return false;
        }
        
        if (!validarComanda(pagamento.getIdComanda())) {
            System.out.println("ERRO: Comanda com ID " + pagamento.getIdComanda() + " não existe no banco!");
            return false;
        }
        
        Pagamento antigo = arqPagamento.read(pagamento.getId());
        boolean ok = arqPagamento.update(pagamento);
        if (!ok)
            return false;
            
        // indexa dnv se mudou a comanda
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

    // Lista pagamentos de uma comanda usando Hash e Lista Invertida
    public List<Integer> listarPorComanda(int comandaId) throws Exception {
        ArrayList<Integer> out = new ArrayList<>();
        HashSet<Long> visitados = new HashSet<>();
        
        ParChaveHead ph = idxComandaHead.read(Math.abs(comandaId));
        long pos = ph == null ? -1L : ph.getHead();
        
        while (pos != -1L) {
            // coreeção do loop circular
            if (visitados.contains(pos)) {
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

    // busca pagamentos por intervalo de valores usando B+ Tree
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
//metodos de verificação se pessoa e comanda existem antes de adc um pagamento
    
// metodo para validar se pessoa existe
    private boolean validarPessoa(int idPessoa) throws Exception {
        Pessoa pessoa = pessoaDAO.buscarPessoa(idPessoa);
        return pessoa != null;
    }
     // metodo para validar se comanda existe
    private boolean validarComanda(int idComanda) throws Exception {
        Comanda comanda = (Comanda) comandaDAO.buscarComanda(idComanda);
        return comanda != null;
    }
}