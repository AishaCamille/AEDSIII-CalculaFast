package com.calculafast.index.hash;

import java.io.File;
import java.lang.reflect.Constructor;

public class IndiceChaveComposta {
    private HashExtensivel<ChaveCompostaPCI> hash;
    private String nomeArquivoDiretorio;
    private String nomeArquivoBuckets;

    public IndiceChaveComposta(String nomeBase) throws Exception {
        this.nomeArquivoDiretorio = nomeBase + "_chave_dir.db";
        this.nomeArquivoBuckets = nomeBase + "_chave_buckets.db";
        
        // Cria o diretório se necessário
        File arquivoDir = new File(nomeArquivoDiretorio);
        File diretorio = arquivoDir.getParentFile();
        if (diretorio != null && !diretorio.exists()) {
            diretorio.mkdirs();
        }
        
        Constructor<ChaveCompostaPCI> construtor = ChaveCompostaPCI.class.getConstructor();
        this.hash = new HashExtensivel<>(construtor, 4, nomeArquivoDiretorio, nomeArquivoBuckets);
    }

    // Insere uma chave composta no índice
    public boolean inserir(int idPessoa, int idComanda, int idItem, long offset) throws Exception {
        // Verifica se já existe antes de inserir (para evitar exceção em caso de colisão de hash)
        ChaveCompostaPCI existente = buscar(idPessoa, idComanda, idItem);
        if (existente != null && 
            existente.getIdPessoa() == idPessoa && 
            existente.getIdComanda() == idComanda && 
            existente.getIdItem() == idItem) {
            throw new Exception("Chave composta já existe no índice");
        }
        
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