package com.calculafast.index.hash;

/*
 Tabela Hash Extensível adaptada para o projeto
 Base: implementação do Prof. Marcos Kutova (v1.1 - 2021), ajustada para pacotes
 e nomenclatura ("bucket" em vez de "cesto").
*/

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.lang.reflect.Constructor;

public class HashExtensivel<T extends RegistroHashExtensivel<T>> {

    // Caminhos e arquivos do índice em disco
    String nomeArquivoDiretorio;
    String nomeArquivoBuckets;
    RandomAccessFile arqDiretorio;
    RandomAccessFile arqBuckets;
    int quantidadeDadosPorBucket;
    Diretorio diretorio;
    Constructor<T> construtor;

    public class Bucket {
        Constructor<T> construtor;
        short quantidadeMaxima;
        short bytesPorElemento;
        short bytesPorBucket;
        byte profundidadeLocal;
        short quantidade;
        ArrayList<T> elementos;

        public Bucket(Constructor<T> ct, int qtdmax) throws Exception {
            this(ct, qtdmax, 0);
        }

        public Bucket(Constructor<T> ct, int qtdmax, int pl) throws Exception {
            construtor = ct;
            if (qtdmax > 32767)
                throw new Exception("Quantidade máxima de 32.767 elementos");
            if (pl > 127)
                throw new Exception("Profundidade local máxima de 127 bits");
            profundidadeLocal = (byte) pl;
            quantidade = 0;
            quantidadeMaxima = (short) qtdmax;
            elementos = new ArrayList<>(quantidadeMaxima);
            bytesPorElemento = ct.newInstance().size();
            bytesPorBucket = (short) (bytesPorElemento * quantidadeMaxima + 3);
        }

        public byte[] toByteArray() throws Exception {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            dos.writeByte(profundidadeLocal);
            dos.writeShort(quantidade);
            int i = 0;
            while (i < quantidade) {
                dos.write(elementos.get(i).toByteArray());
                i++;
            }
            byte[] vazio = new byte[bytesPorElemento];
            while (i < quantidadeMaxima) {
                dos.write(vazio);
                i++;
            }
            return baos.toByteArray();
        }

        public void fromByteArray(byte[] ba) throws Exception {
            ByteArrayInputStream bais = new ByteArrayInputStream(ba);
            DataInputStream dis = new DataInputStream(bais);
            profundidadeLocal = dis.readByte();
            quantidade = dis.readShort();
            int i = 0;
            elementos = new ArrayList<>(quantidadeMaxima);
            byte[] dados = new byte[bytesPorElemento];
            T elem;
            while (i < quantidadeMaxima) {
                dis.read(dados);
                elem = construtor.newInstance();
                elem.fromByteArray(dados);
                elementos.add(elem);
                i++;
            }
        }

        public boolean create(T elem) {
            if (full())
                return false;
            int i = quantidade - 1;
            while (i >= 0 && elem.hashCode() < elementos.get(i).hashCode())
                i--;
            elementos.add(i + 1, elem);
            quantidade++;
            return true;
        }

        public T read(int chave) {
            if (empty())
                return null;
            int i = 0;
            while (i < quantidade && chave > elementos.get(i).hashCode())
                i++;
            if (i < quantidade && chave == elementos.get(i).hashCode())
                return elementos.get(i);
            return null;
        }

        public boolean update(T elem) {
            if (empty())
                return false;
            int i = 0;
            while (i < quantidade && elem.hashCode() > elementos.get(i).hashCode())
                i++;
            if (i < quantidade && elem.hashCode() == elementos.get(i).hashCode()) {
                elementos.set(i, elem);
                return true;
            }
            return false;
        }

        public boolean delete(int chave) {
            if (empty())
                return false;
            int i = 0;
            while (i < quantidade && chave > elementos.get(i).hashCode())
                i++;
            if (i < quantidade && chave == elementos.get(i).hashCode()) {
                elementos.remove(i);
                quantidade--;
                return true;
            }
            return false;
        }

        public boolean empty() {
            return quantidade == 0;
        }

        public boolean full() {
            return quantidade == quantidadeMaxima;
        }

        public int size() { return bytesPorBucket; }
    }

    protected class Diretorio {
        byte profundidadeGlobal;
        long[] enderecos;

        public Diretorio() {
            profundidadeGlobal = 0;
            enderecos = new long[1];
            enderecos[0] = 0;
        }

        public boolean atualizaEndereco(int p, long e) {
            if (p > Math.pow(2, profundidadeGlobal))
                return false;
            enderecos[p] = e;
            return true;
        }

        public byte[] toByteArray() throws IOException {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            dos.writeByte(profundidadeGlobal);
            int quantidade = (int) Math.pow(2, profundidadeGlobal);
            for (int i = 0; i < quantidade; i++)
                dos.writeLong(enderecos[i]);
            return baos.toByteArray();
        }

        public void fromByteArray(byte[] ba) throws IOException {
            ByteArrayInputStream bais = new ByteArrayInputStream(ba);
            DataInputStream dis = new DataInputStream(bais);
            profundidadeGlobal = dis.readByte();
            int quantidade = (int) Math.pow(2, profundidadeGlobal);
            enderecos = new long[quantidade];
            for (int i = 0; i < quantidade; i++)
                enderecos[i] = dis.readLong();
        }

        protected long endereço(int p) {
            if (p > Math.pow(2, profundidadeGlobal))
                return -1;
            return enderecos[p];
        }

        protected boolean duplica() {
            if (profundidadeGlobal == 127)
                return false;
            profundidadeGlobal++;
            int q1 = (int) Math.pow(2, profundidadeGlobal - 1);
            int q2 = (int) Math.pow(2, profundidadeGlobal);
            long[] novos = new long[q2];
            int i = 0;
            while (i < q1) {
                novos[i] = enderecos[i];
                i++;
            }
            while (i < q2) {
                novos[i] = enderecos[i - q1];
                i++;
            }
            enderecos = novos;
            return true;
        }

        protected int hash(int chave) {
            return Math.abs(chave) % (int) Math.pow(2, profundidadeGlobal);
        }

        protected int hash2(int chave, int pl) {
            return Math.abs(chave) % (int) Math.pow(2, pl);
        }
    }

    public HashExtensivel(Constructor<T> ct, int n, String nd, String nb) throws Exception {
        construtor = ct;
        quantidadeDadosPorBucket = n;
        nomeArquivoDiretorio = nd;
        nomeArquivoBuckets = nb;
        arqDiretorio = new RandomAccessFile(nomeArquivoDiretorio, "rw");
        arqBuckets = new RandomAccessFile(nomeArquivoBuckets, "rw");
        if (arqDiretorio.length() == 0 || arqBuckets.length() == 0) {
            diretorio = new Diretorio();
            byte[] bd = diretorio.toByteArray();
            arqDiretorio.write(bd);
            Bucket c = new Bucket(construtor, quantidadeDadosPorBucket);
            bd = c.toByteArray();
            arqBuckets.seek(0);
            arqBuckets.write(bd);
        }
    }

    public boolean create(T elem) throws Exception {
        byte[] bd = new byte[(int) arqDiretorio.length()];
        arqDiretorio.seek(0);
        arqDiretorio.read(bd);
        diretorio = new Diretorio();
        diretorio.fromByteArray(bd);
        int i = diretorio.hash(elem.hashCode());
        long enderecoBucket = diretorio.endereço(i);
        Bucket c = new Bucket(construtor, quantidadeDadosPorBucket);
        byte[] ba = new byte[c.size()];
        arqBuckets.seek(enderecoBucket);
        arqBuckets.read(ba);
        c.fromByteArray(ba);
        if (c.read(elem.hashCode()) != null)
            throw new Exception("Elemento já existe");
        if (!c.full()) {
            c.create(elem);
            arqBuckets.seek(enderecoBucket);
            arqBuckets.write(c.toByteArray());
            return true;
        }
        byte pl = c.profundidadeLocal;
        if (pl >= diretorio.profundidadeGlobal)
            diretorio.duplica();
        byte pg = diretorio.profundidadeGlobal;
        Bucket c1 = new Bucket(construtor, quantidadeDadosPorBucket, pl + 1);
        arqBuckets.seek(enderecoBucket);
        arqBuckets.write(c1.toByteArray());
        Bucket c2 = new Bucket(construtor, quantidadeDadosPorBucket, pl + 1);
        long novoEndereco = arqBuckets.length();
        arqBuckets.seek(novoEndereco);
        arqBuckets.write(c2.toByteArray());
        int inicio = diretorio.hash2(elem.hashCode(), c.profundidadeLocal);
        int deslocamento = (int) Math.pow(2, pl);
        int max = (int) Math.pow(2, pg);
        boolean troca = false;
        for (int j = inicio; j < max; j += deslocamento) {
            if (troca)
                diretorio.atualizaEndereco(j, novoEndereco);
            troca = !troca;
        }
        bd = diretorio.toByteArray();
        arqDiretorio.seek(0);
        arqDiretorio.write(bd);
        for (int j = 0; j < c.quantidade; j++) {
            create(c.elementos.get(j));
        }
        create(elem);
        return true;
    }

    public T read(int chave) throws Exception {
        byte[] bd = new byte[(int) arqDiretorio.length()];
        arqDiretorio.seek(0);
        arqDiretorio.read(bd);
        diretorio = new Diretorio();
        diretorio.fromByteArray(bd);
        int i = diretorio.hash(chave);
        long enderecoBucket = diretorio.endereço(i);
        Bucket c = new Bucket(construtor, quantidadeDadosPorBucket);
        byte[] ba = new byte[c.size()];
        arqBuckets.seek(enderecoBucket);
        arqBuckets.read(ba);
        c.fromByteArray(ba);
        return c.read(chave);
    }

    public boolean update(T elem) throws Exception {
        byte[] bd = new byte[(int) arqDiretorio.length()];
        arqDiretorio.seek(0);
        arqDiretorio.read(bd);
        diretorio = new Diretorio();
        diretorio.fromByteArray(bd);
        int i = diretorio.hash(elem.hashCode());
        long enderecoBucket = diretorio.endereço(i);
        Bucket c = new Bucket(construtor, quantidadeDadosPorBucket);
        byte[] ba = new byte[c.size()];
        arqBuckets.seek(enderecoBucket);
        arqBuckets.read(ba);
        c.fromByteArray(ba);
        if (!c.update(elem))
            return false;
        arqBuckets.seek(enderecoBucket);
        arqBuckets.write(c.toByteArray());
        return true;
    }

    public boolean delete(int chave) throws Exception {
        byte[] bd = new byte[(int) arqDiretorio.length()];
        arqDiretorio.seek(0);
        arqDiretorio.read(bd);
        diretorio = new Diretorio();
        diretorio.fromByteArray(bd);
        int i = diretorio.hash(chave);
        long enderecoBucket = diretorio.endereço(i);
        Bucket c = new Bucket(construtor, quantidadeDadosPorBucket);
        byte[] ba = new byte[c.size()];
        arqBuckets.seek(enderecoBucket);
        arqBuckets.read(ba);
        c.fromByteArray(ba);
        if (!c.delete(chave))
            return false;
        arqBuckets.seek(enderecoBucket);
        arqBuckets.write(c.toByteArray());
        return true;
    }
}
