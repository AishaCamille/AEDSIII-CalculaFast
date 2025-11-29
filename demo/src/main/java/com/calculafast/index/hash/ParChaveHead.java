package com.calculafast.index.hash;

import java.io.*;

// Par (chave pai -> head da lista invertida)
public class ParChaveHead implements RegistroHashExtensivel<ParChaveHead> {

  private int chave;
  private long head;
  private static final short TAMANHO = 16; // 4 + 8 + padding

  public ParChaveHead() { this(0, -1L); }

  public ParChaveHead(int chave, long head) { this.chave = chave; this.head = head; }

  public int getChave() { return chave; }
  public long getHead() { return head; }
  public void setHead(long h) { this.head = h; }

  @Override
  public int hashCode() { return Math.abs(chave); }

  @Override
  public short size() { return TAMANHO; }

  @Override
  public byte[] toByteArray() throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    DataOutputStream dos = new DataOutputStream(baos);
    dos.writeInt(chave);
    dos.writeLong(head);
    while (baos.size() < TAMANHO) dos.writeByte(0);
    return baos.toByteArray();
  }

  @Override
  public void fromByteArray(byte[] ba) throws IOException {
    ByteArrayInputStream bais = new ByteArrayInputStream(ba);
    DataInputStream dis = new DataInputStream(bais);
    this.chave = dis.readInt();
    this.head = dis.readLong();
  }
}


