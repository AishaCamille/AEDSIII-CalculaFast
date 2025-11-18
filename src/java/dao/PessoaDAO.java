package dao;
import index.hash.HashExtensivel;
import index.hash.ParIdOffset;
import java.util.List;
import model.Arquivo;
import model.Pessoa;
import model.Pessoa_Comanda_Item;

public class PessoaDAO  {

       private Arquivo<Pessoa> arqPessoas;
       private HashExtensivel<ParIdOffset> idxPk;
       private Pessoa_Comanda_ItemDAO pciDAO;

    public PessoaDAO() throws Exception {
        arqPessoas = new Arquivo<>("pessoas", Pessoa.class.getConstructor());
        // inicializa índice de PK (id -> offset)
        String base = "./dados/pessoas/pessoas";
        idxPk = new HashExtensivel<>(ParIdOffset.class.getConstructor(), 8,
                base + ".pkhash_d.db", base + ".pkhash_b.db");
        rebuildIndexIfNeeded();
        pciDAO = new Pessoa_Comanda_ItemDAO();
    }

    public PessoaDAO(Arquivo<Pessoa> arqPessoas) throws Exception {
        this.arqPessoas = arqPessoas;
        String base = arqPessoas.getNomeArquivo().replace(".db", "");
        idxPk = new HashExtensivel<>(ParIdOffset.class.getConstructor(), 8,
                base + ".pkhash_d.db", base + ".pkhash_b.db");
        rebuildIndexIfNeeded();
    }

    private void rebuildIndexIfNeeded() throws Exception {
        // Revarre arquivo e atualiza/insere pares (id, offset)
        arqPessoas.scanValidRecords((pos, obj) -> {
            try {
                Pessoa p = (Pessoa) obj;
                if (!idxPk.update(new ParIdOffset(p.getId(), pos))) {
                    try { idxPk.create(new ParIdOffset(p.getId(), pos)); } catch (Exception ignore) {}
                }
            } catch (Exception e) { }
        });
    }

     public Pessoa buscarPessoa(int id) throws Exception {
        ParIdOffset ref = idxPk.read(Math.abs(id));
        if (ref != null) {
            // acesso direto por offset
            Pessoa p = (Pessoa) arqPessoas.readAt(ref.getOffset());
            if (p != null && p.getId() == id) return p;
        }
        // fallback
        return arqPessoas.read(id);
    }

    public boolean incluirPessoa(Pessoa pessoa) throws Exception {
        long off = arqPessoas.createWithOffset(pessoa);
        if (off < 0) return false;
        idxPk.create(new ParIdOffset(pessoa.getId(), off));
        return true;
    }

    public boolean alterarPessoa(Pessoa pessoa) throws Exception {
        long off = arqPessoas.updateWithOffset(pessoa);
        if (off < 0) return false;
        ParIdOffset par = new ParIdOffset(pessoa.getId(), off);
        if (!idxPk.update(par)) idxPk.create(par);
        return true;
    }

    public boolean excluirPessoa(int id) throws Exception {
        boolean ok = arqPessoas.delete(id);
        if (ok) idxPk.delete(Math.abs(id));
        return ok;
    }

    //p tabelas intermediarias
    //busca todos os itens que uma pessoa ja comprou
      public List<Integer> getItensCompradosPorPessoa(int idPessoa) throws Exception {
        return pciDAO.buscarItensUnicosPorPessoa(idPessoa);
    }
    
    //mostra todas as relações de uma pessoa
     public List<Pessoa_Comanda_Item> getRelacoesDaPessoa(int idPessoa) throws Exception {
        return pciDAO.buscarPorPessoa(idPessoa);
    }
}
