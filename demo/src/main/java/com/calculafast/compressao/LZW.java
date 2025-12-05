package com.calculafast.compressao;

import java.io.*;
import java.util.*;

/**
 * Implementação do algoritmo de compressão LZW (Lempel-Ziv-Welch).
 * 
 * Estruturas de dados utilizadas:
 * - HashMap (dicionário de compressão): Mapeia sequências de bytes para códigos.
 * - ArrayList (dicionário de descompressão): Array de strings indexado pelo código.
 * - StringBuilder: Para construir sequências de forma eficiente durante o processamento.
 */
public class LZW {

    private static final int TAMANHO_MAX_DICIONARIO = 65536;
    private static final int PRIMEIRO_CODIGO_NOVO = 256;

    public byte[] comprimir(byte[] dados) throws IOException {
        if (dados == null || dados.length == 0) {
            return new byte[0];
        }

        Map<String, Integer> dicionario = new HashMap<>();
        for (int i = 0; i < 256; i++) {
            dicionario.put(String.valueOf((char) i), i);
        }

        int proximoCodigo = PRIMEIRO_CODIGO_NOVO;
        List<Integer> codigosSaida = new ArrayList<>();
        String sequenciaAtual = "";

        for (byte b : dados) {
            char caractere = (char) (b & 0xFF);
            String novaSequencia = sequenciaAtual + caractere;

            if (dicionario.containsKey(novaSequencia)) {
                sequenciaAtual = novaSequencia;
            } else {
                codigosSaida.add(dicionario.get(sequenciaAtual));

                if (proximoCodigo < TAMANHO_MAX_DICIONARIO) {
                    dicionario.put(novaSequencia, proximoCodigo++);
                }

                sequenciaAtual = String.valueOf(caractere);
            }
        }

        if (!sequenciaAtual.isEmpty()) {
            codigosSaida.add(dicionario.get(sequenciaAtual));
        }

        return codificosParaBytes(codigosSaida);
    }

    public byte[] descomprimir(byte[] dadosComprimidos) throws IOException {
        if (dadosComprimidos == null || dadosComprimidos.length == 0) {
            return new byte[0];
        }

        List<Integer> codigos = bytesParaCodigos(dadosComprimidos);
        
        if (codigos.isEmpty()) {
            return new byte[0];
        }

        List<String> dicionario = new ArrayList<>();
        for (int i = 0; i < 256; i++) {
            dicionario.add(String.valueOf((char) i));
        }

        ByteArrayOutputStream saida = new ByteArrayOutputStream();
        
        int codigoAnterior = codigos.get(0);
        String sequenciaAnterior = dicionario.get(codigoAnterior);
        escreverString(saida, sequenciaAnterior);

        for (int i = 1; i < codigos.size(); i++) {
            int codigoAtual = codigos.get(i);
            String sequenciaAtual;

            if (codigoAtual < dicionario.size()) {
                sequenciaAtual = dicionario.get(codigoAtual);
            } else {
                sequenciaAtual = sequenciaAnterior + sequenciaAnterior.charAt(0);
            }

            escreverString(saida, sequenciaAtual);

            if (dicionario.size() < TAMANHO_MAX_DICIONARIO) {
                dicionario.add(sequenciaAnterior + sequenciaAtual.charAt(0));
            }

            sequenciaAnterior = sequenciaAtual;
        }

        return saida.toByteArray();
    }

    private byte[] codificosParaBytes(List<Integer> codigos) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        dos.writeInt(codigos.size());

        for (int codigo : codigos) {
            dos.writeShort(codigo);
        }

        return baos.toByteArray();
    }

    private List<Integer> bytesParaCodigos(byte[] bytes) throws IOException {
        List<Integer> codigos = new ArrayList<>();
        
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        DataInputStream dis = new DataInputStream(bais);

        int numCodigos = dis.readInt();

        for (int i = 0; i < numCodigos; i++) {
            int codigo = dis.readUnsignedShort();
            codigos.add(codigo);
        }

        return codigos;
    }

    private void escreverString(ByteArrayOutputStream baos, String str) {
        for (int i = 0; i < str.length(); i++) {
            baos.write((byte) str.charAt(i));
        }
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
