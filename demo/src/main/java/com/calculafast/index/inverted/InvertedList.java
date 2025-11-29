package com.calculafast.index.inverted;

import java.io.*;

// Lista invertida 1:N encadeada em arquivo: cada nó guarda (filhoId, proxOffset)
public class InvertedList {

  private final RandomAccessFile raf;
  public static final int NODE_SIZE = 16; // 4 (id) + 8 (prox) + 4 padding

  public static class Node {
    public int childId;
    public long next;
    public Node(int childId, long next) { this.childId = childId; this.next = next; }
  }

  public InvertedList(String path) throws IOException {
    File f = new File(path);
    if (f.getParentFile() != null && !f.getParentFile().exists()) f.getParentFile().mkdirs();
    this.raf = new RandomAccessFile(f, "rw");
  }

  public void clear() throws IOException { raf.setLength(0); }

  // Insere no início: novo nó aponta para o head atual e retorna o offset do novo head
  public long prepend(int childId, long head) throws IOException {
    long pos = raf.length();
    raf.seek(pos);
    raf.writeInt(childId);
    raf.writeLong(head);
    raf.writeInt(0); // padding
    return pos;
  }

  public Node readAt(long pos) throws IOException {
    if (pos < 0) return null;
    raf.seek(pos);
    int id = raf.readInt();
    long next = raf.readLong();
    return new Node(id, next);
  }

  // Atualiza ponteiro next de um nó existente na lista
  public void updateNext(long pos, long next) throws IOException {
    raf.seek(pos + 4); // 4 bytes do childId
    raf.writeLong(next);
  }

  public void close() throws IOException { raf.close(); }
}


