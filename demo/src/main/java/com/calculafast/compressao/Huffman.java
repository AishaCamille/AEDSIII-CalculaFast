package com.calculafast.compressao;

import java.io.*;
import java.util.*;

/**
 * Implementação do algoritmo de compressão Huffman.
 * 
 * Estruturas de dados utilizadas:
 * - PriorityQueue (Min-Heap): Para construir a árvore de Huffman de forma eficiente.
 * - HashMap: Para armazenar a tabela de códigos (símbolo -> código binário).
 * - Árvore binária (NoHuffman): Estrutura hierárquica que representa a codificação.
 */
public class Huffman {

    private static class NoHuffman implements Comparable<NoHuffman>, Serializable {
        private static final long serialVersionUID = 1L;
        byte dado;
        int frequencia;
        NoHuffman esquerda;
        NoHuffman direita;
        boolean folha;

        NoHuffman(byte dado, int frequencia) {
            this.dado = dado;
            this.frequencia = frequencia;
            this.folha = true;
        }

        NoHuffman(NoHuffman esquerda, NoHuffman direita) {
            this.esquerda = esquerda;
            this.direita = direita;
            this.frequencia = esquerda.frequencia + direita.frequencia;
            this.folha = false;
        }

        @Override
        public int compareTo(NoHuffman outro) {
            return Integer.compare(this.frequencia, outro.frequencia);
        }
    }

    public byte[] comprimir(byte[] dados) throws IOException {
        if (dados == null || dados.length == 0) {
            return new byte[0];
        }

        Map<Byte, Integer> frequencias = calcularFrequencias(dados);

        if (frequencias.size() == 1) {
            return comprimirCasoEspecial(dados, frequencias);
        }

        NoHuffman raiz = construirArvore(frequencias);

        Map<Byte, String> tabelaCodigos = new HashMap<>();
        gerarCodigos(raiz, "", tabelaCodigos);

        StringBuilder bitsComprimidos = new StringBuilder();
        for (byte b : dados) {
            bitsComprimidos.append(tabelaCodigos.get(b));
        }

        byte[] dadosCodificados = bitsParaBytes(bitsComprimidos.toString());

        return serializarResultado(raiz, dadosCodificados, bitsComprimidos.length());
    }

    public byte[] descomprimir(byte[] dadosComprimidos) throws IOException, ClassNotFoundException {
        if (dadosComprimidos == null || dadosComprimidos.length == 0) {
            return new byte[0];
        }

        ByteArrayInputStream bais = new ByteArrayInputStream(dadosComprimidos);
        DataInputStream dis = new DataInputStream(bais);

        boolean casoEspecial = dis.readBoolean();
        
        if (casoEspecial) {
            return descomprimirCasoEspecial(dis);
        }

        int numBitsValidos = dis.readInt();
        NoHuffman raiz = deserializarArvore(dis);

        int tamanhoDados = dis.readInt();
        byte[] dadosCodificados = new byte[tamanhoDados];
        dis.readFully(dadosCodificados);

        String bits = bytesParaBits(dadosCodificados, numBitsValidos);

        return decodificar(bits, raiz);
    }

    private Map<Byte, Integer> calcularFrequencias(byte[] dados) {
        Map<Byte, Integer> frequencias = new HashMap<>();
        for (byte b : dados) {
            frequencias.merge(b, 1, Integer::sum);
        }
        return frequencias;
    }

    private NoHuffman construirArvore(Map<Byte, Integer> frequencias) {
        PriorityQueue<NoHuffman> fila = new PriorityQueue<>();

        for (Map.Entry<Byte, Integer> entrada : frequencias.entrySet()) {
            fila.offer(new NoHuffman(entrada.getKey(), entrada.getValue()));
        }

        while (fila.size() > 1) {
            NoHuffman esquerda = fila.poll();
            NoHuffman direita = fila.poll();
            NoHuffman pai = new NoHuffman(esquerda, direita);
            fila.offer(pai);
        }

        return fila.poll();
    }

    private void gerarCodigos(NoHuffman no, String codigo, Map<Byte, String> tabela) {
        if (no == null) return;

        if (no.folha) {
            tabela.put(no.dado, codigo.isEmpty() ? "0" : codigo);
            return;
        }

        gerarCodigos(no.esquerda, codigo + "0", tabela);
        gerarCodigos(no.direita, codigo + "1", tabela);
    }

    private byte[] bitsParaBytes(String bits) {
        int numBytes = (bits.length() + 7) / 8;
        byte[] bytes = new byte[numBytes];

        for (int i = 0; i < bits.length(); i++) {
            if (bits.charAt(i) == '1') {
                bytes[i / 8] |= (1 << (7 - (i % 8)));
            }
        }

        return bytes;
    }

    private String bytesParaBits(byte[] bytes, int numBitsValidos) {
        StringBuilder bits = new StringBuilder();

        for (int i = 0; i < bytes.length; i++) {
            for (int j = 7; j >= 0; j--) {
                if (bits.length() < numBitsValidos) {
                    bits.append((bytes[i] >> j) & 1);
                }
            }
        }

        return bits.toString();
    }

    private byte[] serializarResultado(NoHuffman raiz, byte[] dadosCodificados, int numBits) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        dos.writeBoolean(false);
        dos.writeInt(numBits);
        serializarArvore(raiz, dos);
        dos.writeInt(dadosCodificados.length);
        dos.write(dadosCodificados);

        return baos.toByteArray();
    }

    private void serializarArvore(NoHuffman no, DataOutputStream dos) throws IOException {
        if (no.folha) {
            dos.writeBoolean(true);
            dos.writeByte(no.dado);
        } else {
            dos.writeBoolean(false);
            serializarArvore(no.esquerda, dos);
            serializarArvore(no.direita, dos);
        }
    }

    private NoHuffman deserializarArvore(DataInputStream dis) throws IOException {
        boolean folha = dis.readBoolean();
        
        if (folha) {
            byte dado = dis.readByte();
            return new NoHuffman(dado, 0);
        } else {
            NoHuffman esquerda = deserializarArvore(dis);
            NoHuffman direita = deserializarArvore(dis);
            return new NoHuffman(esquerda, direita);
        }
    }

    private byte[] decodificar(String bits, NoHuffman raiz) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        NoHuffman atual = raiz;

        for (int i = 0; i < bits.length(); i++) {
            if (bits.charAt(i) == '0') {
                atual = atual.esquerda;
            } else {
                atual = atual.direita;
            }

            if (atual.folha) {
                baos.write(atual.dado);
                atual = raiz;
            }
        }

        return baos.toByteArray();
    }

    private byte[] comprimirCasoEspecial(byte[] dados, Map<Byte, Integer> frequencias) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        dos.writeBoolean(true);
        byte unicoByte = frequencias.keySet().iterator().next();
        dos.writeByte(unicoByte);
        dos.writeInt(dados.length);

        return baos.toByteArray();
    }

    private byte[] descomprimirCasoEspecial(DataInputStream dis) throws IOException {
        byte unicoByte = dis.readByte();
        int quantidade = dis.readInt();
        
        byte[] resultado = new byte[quantidade];
        Arrays.fill(resultado, unicoByte);
        
        return resultado;
    }

    public Map<String, Object> getEstatisticas(byte[] original, byte[] comprimido) {
        Map<String, Object> stats = new HashMap<>();
        
        long tamanhoOriginal = original.length;
        long tamanhoComprimido = comprimido.length;
        double taxaCompressao = (1.0 - ((double) tamanhoComprimido / tamanhoOriginal)) * 100;
        double razaoCompressao = (double) tamanhoOriginal / tamanhoComprimido;
        
        stats.put("tamanhoOriginal", tamanhoOriginal);
        stats.put("tamanhoComprimido", tamanhoComprimido);
        stats.put("taxaCompressao", taxaCompressao);
        stats.put("razaoCompressao", razaoCompressao);
        stats.put("economia", tamanhoOriginal - tamanhoComprimido);
        
        return stats;
    }
}
