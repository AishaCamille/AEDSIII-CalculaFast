package index.hash;

import java.io.RandomAccessFile;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

public class ListaEncadeadaValor {
    private RandomAccessFile arquivoLista;
    private Constructor<NoListaValor> construtor;
    private String nomeArquivo;

    public ListaEncadeadaValor(String nomeBase) throws Exception {
        this.nomeArquivo = nomeBase + "_lista.db";
        this.construtor = NoListaValor.class.getConstructor();
        this.arquivoLista = new RandomAccessFile(nomeArquivo, "rw");
    }

    // Adiciona um novo nó ao final da lista
    public long adicionarNo(double valorUnitario, int id) throws Exception {
        arquivoLista.seek(arquivoLista.length());
        long offsetAtual = arquivoLista.getFilePointer();
        
        NoListaValor novoNo = new NoListaValor(valorUnitario, id);
        byte[] dados = novoNo.toByteArray();
        arquivoLista.write(dados);
        
        return offsetAtual;
    }

    // Adiciona um novo nó e atualiza o anterior para apontar para ele
    public long adicionarNo(double valorUnitario, int id, long offsetAnterior) throws Exception {
        long offsetNovo = adicionarNo(valorUnitario, id);
        
        // Atualiza o nó anterior para apontar para o novo
        if (offsetAnterior != -1) {
            NoListaValor anterior = lerNo(offsetAnterior);
            anterior.setProximoOffset(offsetNovo);
            atualizarNo(anterior, offsetAnterior);
        }
        
        return offsetNovo;
    }

    // Lê um nó em um offset específico
    public NoListaValor lerNo(long offset) throws Exception {
        if (offset < 0 || offset >= arquivoLista.length()) {
            return null;
        }
        
        arquivoLista.seek(offset);
        byte[] dados = new byte[new NoListaValor().size()];
        arquivoLista.read(dados);
        
        NoListaValor no = construtor.newInstance();
        no.fromByteArray(dados);
        return no;
    }

    // Atualiza um nó em um offset específico
    public void atualizarNo(NoListaValor no, long offset) throws Exception {
        arquivoLista.seek(offset);
        byte[] dados = no.toByteArray();
        arquivoLista.write(dados);
    }

    // Remove um nó da lista (ajustando os ponteiros)
    public boolean removerNo(long offsetRemover, long offsetAnterior) throws Exception {
        NoListaValor noRemover = lerNo(offsetRemover);
        if (noRemover == null) return false;

        if (offsetAnterior == -1) {
            // É o primeiro nó - marca como removido (não podemos realmente remover fisicamente)
            // Em uma implementação real, você teria um sistema de lapide aqui
            return true;
        } else {
            // É um nó do meio/fim - ajusta o ponteiro do anterior
            NoListaValor anterior = lerNo(offsetAnterior);
            anterior.setProximoOffset(noRemover.getProximoOffset());
            atualizarNo(anterior, offsetAnterior);
            return true;
        }
    }

    // Percorre a lista a partir de um offset e retorna todos os IDs
    public List<Integer> percorrerLista(long offsetInicial) throws Exception {
        List<Integer> ids = new ArrayList<>();
        long offsetAtual = offsetInicial;
        
        while (offsetAtual != -1) {
            NoListaValor no = lerNo(offsetAtual);
            if (no != null) {
                ids.add(no.getId());
                offsetAtual = no.getProximoOffset();
            } else {
                break;
            }
        }
        
        return ids;
    }

    // Percorre a lista e retorna todos os nós
    public List<NoListaValor> percorrerListaCompleta(long offsetInicial) throws Exception {
        List<NoListaValor> nos = new ArrayList<>();
        long offsetAtual = offsetInicial;
        
        while (offsetAtual != -1) {
            NoListaValor no = lerNo(offsetAtual);
            if (no != null) {
                nos.add(no);
                offsetAtual = no.getProximoOffset();
            } else {
                break;
            }
        }
        
        return nos;
    }

    public void fechar() throws Exception {
        if (arquivoLista != null) {
            arquivoLista.close();
        }
    }
}