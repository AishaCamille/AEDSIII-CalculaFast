
import java.util.*;



public class PagamentoDAO {

    private Arquivo<Pagamento> arqPagamento;

    public PagamentoDAO() throws Exception {
        arqPagamento = new Arquivo<>("Pagamentos", Pagamento.class.getConstructor());
    }

    public PagamentoDAO(Arquivo<Pagamento> arqPagamento) {
        this.arqPagamento = arqPagamento;
    }

    public Pagamento buscarPagamento(int id) throws Exception {
        return arqPagamento.read(id);
    }

    public boolean incluirPagamento(Pagamento pagamento) throws Exception {
        return arqPagamento.create(pagamento) > 0;
    }

    public boolean alterarPagamento(Pagamento Pagamento) throws Exception {
        return arqPagamento.update(Pagamento);
    }

    public boolean excluirPagamento(int id) throws Exception {
        return arqPagamento.delete(id);
    }

}
