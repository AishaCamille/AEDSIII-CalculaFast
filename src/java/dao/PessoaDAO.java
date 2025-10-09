package dao;
import model.Arquivo;
import model.Pessoa;


public class PessoaDAO  {

       private Arquivo<Pessoa> arqPessoas;

    public PessoaDAO() throws Exception {
        arqPessoas = new Arquivo<>("pessoas", Pessoa.class.getConstructor());
    }

    public PessoaDAO(Arquivo<Pessoa> arqPessoas) {
        this.arqPessoas = arqPessoas;
    }

    
     public Pessoa buscarPessoa(int id) throws Exception {
        return arqPessoas.read(id);
    }

    public boolean incluirPessoa(Pessoa pessoa) throws Exception {
        return arqPessoas.create(pessoa) > 0;
    }

    public boolean alterarPessoa(Pessoa pessoa) throws Exception {
        return arqPessoas.update(pessoa);
    }

    public boolean excluirPessoa(int id) throws Exception {
        return arqPessoas.delete(id);
    }
    
}
