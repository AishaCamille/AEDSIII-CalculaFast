package com.calculafast.index.bptree;

import java.io.*;
import java.util.*;

// B+ Tree muito simples (ordem pequena) persistida em arquivo
// Chave int -> valor long (offset na lista invertida ou ID)
// Implementação mínima para buscas ordenadas por chave e range scan
public class BPlusTree {

    private static final int ORDEM = 4;
    private final RandomAccessFile raf;

    private static class Node implements Serializable {
        boolean folha;
        int n;
        int[] chaves = new int[ORDEM * 2 - 1];
        long[] valores = new long[ORDEM * 2 - 1];
        long[] filhos = new long[ORDEM * 2];
        long nextLeaf = -1;
    }

    public BPlusTree(String path) throws IOException {
        File f = new File(path);
        if (f.getParentFile() != null && !f.getParentFile().exists())
            f.getParentFile().mkdirs();
        this.raf = new RandomAccessFile(f, "rw");
        if (raf.length() == 0) {
            Node root = new Node();
            root.folha = true;
            root.n = 0;
            writeNode(0, root);
        }
    }

    private Node readNode(long pos) throws IOException {
        raf.seek(pos);
        
        // Cria streams SEM fechar o RandomAccessFile
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int bytesRead;
        long initialPos = raf.getFilePointer();
        
        // Lê dados do nó (limitado ao tamanho estimado)
        try {
            // Marca posição e tenta ler objeto serializado
            raf.seek(pos);
            DataInputStream dis = new DataInputStream(new FileInputStream(raf.getFD()));
            ObjectInputStream ois = new ObjectInputStream(dis);
            Node node = (Node) ois.readObject();
            return node;
        } catch (ClassNotFoundException e) {
            throw new IOException("Erro ao ler nó", e);
        } catch (IOException e) {
            // Se falhar, tenta método alternativo
            raf.seek(pos);
            return readNodeAlternative();
        }
    }
    
    // Método alternativo de leitura usando ByteArrayInputStream
    private Node readNodeAlternative() throws IOException {
        // Lê tamanho do objeto serializado
        int size = raf.readInt();
        byte[] data = new byte[size];
        raf.readFully(data);
        
        try (ByteArrayInputStream bais = new ByteArrayInputStream(data);
             ObjectInputStream ois = new ObjectInputStream(bais)) {
            return (Node) ois.readObject();
        } catch (ClassNotFoundException e) {
            throw new IOException("Erro ao deserializar nó", e);
        }
    }

    private void writeNode(long pos, Node node) throws IOException {
        // Serializa para byte array primeiro
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(node);
            oos.flush();
        }
        
        byte[] data = baos.toByteArray();
        
        // Escreve no arquivo
        raf.seek(pos);
        raf.writeInt(data.length); // Escreve tamanho primeiro
        raf.write(data);
    }

    public synchronized void put(int key, long value) throws IOException {
        Node root = readNode(0);
        
        // Busca posição para inserção ordenada
        int i = root.n - 1;
        while (i >= 0 && key < root.chaves[i]) {
            root.chaves[i + 1] = root.chaves[i];
            root.valores[i + 1] = root.valores[i];
            i--;
        }
        
        // Se a chave já existe, atualiza o valor
        if (i >= 0 && root.chaves[i] == key) {
            root.valores[i] = value;
            writeNode(0, root);
            return;
        }
        
        // Insere nova chave
        root.chaves[i + 1] = key;
        root.valores[i + 1] = value;
        root.n++;
        writeNode(0, root);
    }

    public synchronized Long get(int key) throws IOException {
        Node root = readNode(0);
        for (int i = 0; i < root.n; i++) {
            if (root.chaves[i] == key)
                return root.valores[i];
        }
        return null;
    }

    public synchronized List<int[]> range(int keyMin, int keyMax) throws IOException {
        Node root = readNode(0);
        List<int[]> res = new ArrayList<>();
        
        for (int i = 0; i < root.n; i++) {
            if (root.chaves[i] >= keyMin && root.chaves[i] <= keyMax) {
                res.add(new int[] { root.chaves[i], (int) root.valores[i] });
            }
        }
        
        // Ordena por chave
        res.sort(Comparator.comparingInt(a -> a[0]));
        return res;
    }

    public void close() throws IOException {
        if (raf != null) {
            raf.close();
        }
    }
}