package index.bptree;

import java.io.*;
import java.util.*;

// B+ Tree muito simples (ordem pequena) persistida em arquivo
// Chave int -> valor long (offset na lista invertida)
// Implementação mínima para buscas ordenadas por chave e range scan
public class BPlusTree {

    private static final int ORDEM = 4; // pequeno para a fase atual
    private final RandomAccessFile raf;

    private static class Node implements Serializable {
        boolean folha;
        int n;
        int[] chaves = new int[ORDEM * 2 - 1];
        long[] valores = new long[ORDEM * 2 - 1]; // somente se folha
        long[] filhos = new long[ORDEM * 2];
        long nextLeaf = -1; // encadeamento de folhas
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
        try (ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(raf.getFD())))) {
            try {
                return (Node) ois.readObject();
            } catch (ClassNotFoundException e) {
                throw new IOException(e);
            }
        }
    }

    private void writeNode(long pos, Node node) throws IOException {
        raf.seek(pos);
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new BufferedOutputStream(new FileOutputStream(raf.getFD())))) {
            oos.writeObject(node);
            oos.flush();
        }
    }

    // Para simplificar: usamos arquivo com um único nó raiz (folha) nesta fase
    public synchronized void put(int key, long value) throws IOException {
        Node root = readNode(0);
        // busca inserção ordenada na folha raiz
        int i = root.n - 1;
        while (i >= 0 && key < root.chaves[i]) {
            root.chaves[i + 1] = root.chaves[i];
            root.valores[i + 1] = root.valores[i];
            i--;
        }
        if (i >= 0 && root.chaves[i] == key) {
            root.valores[i] = value;
            writeNode(0, root);
            return;
        }
        root.chaves[i + 1] = key;
        root.valores[i + 1] = value;
        root.n++;
        writeNode(0, root);
    }

    public synchronized Long get(int key) throws IOException {
        Node root = readNode(0);
        for (int i = 0; i < root.n; i++)
            if (root.chaves[i] == key)
                return root.valores[i];
        return null;
    }

    public synchronized List<int[]> range(int keyMin, int keyMax) throws IOException {
        Node root = readNode(0);
        List<int[]> res = new ArrayList<>();
        for (int i = 0; i < root.n; i++)
            if (root.chaves[i] >= keyMin && root.chaves[i] <= keyMax)
                res.add(new int[] { root.chaves[i], (int) root.valores[i] });
        res.sort(Comparator.comparingInt(a -> a[0]));
        return res;
    }

    public void close() throws IOException {
        raf.close();
    }
}
