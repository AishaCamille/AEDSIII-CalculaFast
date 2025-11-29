package com.calculafast.index.hash;

import java.io.*;

// Par (id -> offset) de tamanho fixo para uso no Hash Extensível
public class ParIdOffset implements RegistroHashExtensivel<ParIdOffset> {

    private int id;
    private long offset;
    private static final short TAMANHO = 16; // 4 (id) + 8 (offset) + margem p/ alinhamento UTF ausente

    public ParIdOffset() {
        this(-1, -1L);
    }

    public ParIdOffset(int id, long offset) {
        this.id = id;
        this.offset = offset;
    }

    public int getId() {
        return id;
    }

    public long getOffset() {
        return offset;
    }

    public void setOffset(long o) {
        this.offset = o;
    }

    @Override
    public int hashCode() {
        return Math.abs(id);
    }

    @Override
    public short size() {
        return TAMANHO;
    }

    @Override
    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeInt(id);
        dos.writeLong(offset);
        // padding para completar TAMANHO, se necessário
        while (baos.size() < TAMANHO)
            dos.writeByte(0);
        return baos.toByteArray();
    }

    @Override
    public void fromByteArray(byte[] ba) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(ba);
        DataInputStream dis = new DataInputStream(bais);
        this.id = dis.readInt();
        this.offset = dis.readLong();
    }
}
