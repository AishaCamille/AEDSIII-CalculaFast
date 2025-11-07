package index.hash;

import java.lang.reflect.Constructor;

public class IndiceChaveComposta {
    private HashExtensivel<ChaveCompostaPCI> hash;
    private String nomeArquivoDiretorio;
    private String nomeArquivoBuckets;

    public IndiceChaveComposta(String nomeBase) throws Exception {
        this.nomeArquivoDiretorio = nomeBase + "_chave_dir.db";
        this.nomeArquivoBuckets = nomeBase + "_chave_buckets.db";
        Constructor<ChaveCompostaPCI> construtor = ChaveCompostaPCI.class.getConstructor();
        this.hash = new HashExtensivel<>(construtor, 4, nomeArquivoDiretorio, nomeArquivoBuckets);
    }

    // Insere uma chave composta no índice
    public boolean inserir(int idPessoa, int idComanda, int idItem, long offset) throws Exception {
        ChaveCompostaPCI chave = new ChaveCompostaPCI(idPessoa, idComanda, idItem, offset);
        return hash.create(chave);
    }

    // Busca por chave composta
    public ChaveCompostaPCI buscar(int idPessoa, int idComanda, int idItem) throws Exception {
        int chaveHash = Math.abs((idPessoa * 31) + (idComanda * 17) + (idItem * 7));
        return hash.read(chaveHash);
    }

    // Atualiza o offset para uma chave composta
    public boolean atualizar(int idPessoa, int idComanda, int idItem, long novoOffset) throws Exception {
        ChaveCompostaPCI chave = new ChaveCompostaPCI(idPessoa, idComanda, idItem, novoOffset);
        return hash.update(chave);
    }

    // Remove uma chave composta do índice
    public boolean remover(int idPessoa, int idComanda, int idItem) throws Exception {
        int chaveHash = Math.abs((idPessoa * 31) + (idComanda * 17) + (idItem * 7));
        return hash.delete(chaveHash);
    }

    public void fechar() throws Exception {
        // Fechamento automático
    }
}