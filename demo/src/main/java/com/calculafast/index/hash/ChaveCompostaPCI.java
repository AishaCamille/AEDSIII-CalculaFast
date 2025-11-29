package com.calculafast.index.hash;

import java.io.*;

public class ChaveCompostaPCI implements RegistroHashExtensivel<ChaveCompostaPCI> {
    private int idPessoa;
    private int idComanda;
    private int idItem;
    private long offset;
    private static final short TAMANHO = 20; // 3 int (12) + 1 long (8) = 20 bytes

    public ChaveCompostaPCI() {
        this(-1, -1, -1, -1);
    }

    public ChaveCompostaPCI(int idPessoa, int idComanda, int idItem, long offset) {
        this.idPessoa = idPessoa;
        this.idComanda = idComanda;
        this.idItem = idItem;
        this.offset = offset;
    }

    public int getIdPessoa() { return idPessoa; }
    public int getIdComanda() { return idComanda; }
    public int getIdItem() { return idItem; }
    public long getOffset() { return offset; }
    public void setOffset(long offset) { this.offset = offset; }

    @Override
    public int hashCode() {
        return Math.abs((idPessoa * 31) + (idComanda * 17) + (idItem * 7));
    }

    @Override
    public short size() {
        return TAMANHO;
    }

    @Override
    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeInt(idPessoa);
        dos.writeInt(idComanda);
        dos.writeInt(idItem);
        dos.writeLong(offset);
        return baos.toByteArray();
    }

    @Override
    public void fromByteArray(byte[] ba) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(ba);
        DataInputStream dis = new DataInputStream(bais);
        this.idPessoa = dis.readInt();
        this.idComanda = dis.readInt();
        this.idItem = dis.readInt();
        this.offset = dis.readLong();
    }
}