package index.hash;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

public class IndiceValorUnitario {
    private HashExtensivel<NoListaValor> hash;
    private ListaEncadeadaValor listaEncadeada;
    private String nomeArquivoDiretorio;
    private String nomeArquivoBuckets;

    public IndiceValorUnitario(String nomeBase) throws Exception {
        this.nomeArquivoDiretorio = nomeBase + "_dir.db";
        this.nomeArquivoBuckets = nomeBase + "_buckets.db";
        
        Constructor<NoListaValor> construtor = NoListaValor.class.getConstructor();
        this.hash = new HashExtensivel<>(construtor, 4, nomeArquivoDiretorio, nomeArquivoBuckets);
        this.listaEncadeada = new ListaEncadeadaValor(nomeBase);
    }

    // Insere um novo registro no índice
    public boolean inserir(double valorUnitario, int id) throws Exception {
        int chave = Math.abs(Double.valueOf(valorUnitario).hashCode());
        NoListaValor existente = hash.read(chave);
        
        if (existente == null) {
            // Primeiro registro com este valor - cria novo nó na lista
            long offsetNovo = listaEncadeada.adicionarNo(valorUnitario, id);
            NoListaValor novoNo = new NoListaValor(valorUnitario, id);
            return hash.create(novoNo);
        } else {
            // Já existe registro - adiciona à lista encadeada
            // Encontra o último nó da lista
            List<NoListaValor> nos = listaEncadeada.percorrerListaCompleta(
                calcularOffset(existente));
            
            if (!nos.isEmpty()) {
                NoListaValor ultimoNo = nos.get(nos.size() - 1);
                long offsetUltimo = calcularOffset(ultimoNo);
                listaEncadeada.adicionarNo(valorUnitario, id, offsetUltimo);
            }
            return true;
        }
    }

    // Busca TODOS os registros com um valor unitário específico
    public List<Integer> buscarTodosIds(double valorUnitario) throws Exception {
        int chave = Math.abs(Double.valueOf(valorUnitario).hashCode());
        NoListaValor primeiroNo = hash.read(chave);
        
        if (primeiroNo != null && primeiroNo.getValorUnitario() == valorUnitario) {
            return listaEncadeada.percorrerLista(calcularOffset(primeiroNo));
        }
        
        return new ArrayList<>();
    }

    // Busca o PRIMEIRO registro com um valor unitário específico
    public NoListaValor buscar(double valorUnitario) throws Exception {
        int chave = Math.abs(Double.valueOf(valorUnitario).hashCode());
        NoListaValor no = hash.read(chave);
        
        if (no != null && no.getValorUnitario() == valorUnitario) {
            return no;
        }
        
        return null;
    }

    // Remove um registro específico do índice
    public boolean remover(double valorUnitario, int idRemover) throws Exception {
        int chave = Math.abs(Double.valueOf(valorUnitario).hashCode());
        NoListaValor primeiroNo = hash.read(chave);
        
        if (primeiroNo == null) return false;
        
        List<NoListaValor> nos = listaEncadeada.percorrerListaCompleta(calcularOffset(primeiroNo));
        if (nos.isEmpty()) return false;
        
        // Caso especial: remoção do primeiro nó
        if (nos.get(0).getId() == idRemover) {
            if (nos.size() == 1) {
                // Era o único nó - remove do hash
                return hash.delete(chave);
            } else {
                // Tem próximos - atualiza o hash para apontar para o segundo
                NoListaValor segundoNo = nos.get(1);
                return hash.update(segundoNo);
            }
        }
        
        // Procura o nó a ser removido e seu anterior
        for (int i = 1; i < nos.size(); i++) {
            if (nos.get(i).getId() == idRemover) {
                NoListaValor anterior = nos.get(i - 1);
                NoListaValor atual = nos.get(i);
                
                // Ajusta o ponteiro do anterior
                anterior.setProximoOffset(atual.getProximoOffset());
                listaEncadeada.atualizarNo(anterior, calcularOffset(anterior));
                return true;
            }
        }
        
        return false;
    }

    // Método auxiliar para calcular offset (simplificado)
    private long calcularOffset(NoListaValor no) throws Exception {
        // Em uma implementação real, você teria um mapeamento melhor
        // Esta é uma versão simplificada
        int chave = no.hashCode();
        return chave * no.size();
    }

    public void fechar() throws Exception {
        if (listaEncadeada != null) {
            listaEncadeada.fechar();
        }
    }
}