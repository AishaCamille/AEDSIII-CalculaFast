package index.hash;

import java.io.*;

public class NoListaValor implements RegistroHashExtensivel<NoListaValor> {
    private double valorUnitario;
    private int id;
    private long proximoOffset; // Offset do próximo nó na lista
    private static final short TAMANHO = 20; // 8 (double) + 4 (int) + 8 (long)

    public NoListaValor() {
        this(0.0, -1, -1);
    }

    public NoListaValor(double valorUnitario, int id) {
        this(valorUnitario, id, -1);
    }

    public NoListaValor(double valorUnitario, int id, long proximoOffset) {
        this.valorUnitario = valorUnitario;
        this.id = id;
        this.proximoOffset = proximoOffset;
    }

    public double getValorUnitario() {
        return valorUnitario;
    }

    public int getId() {
        return id;
    }

    public long getProximoOffset() {
        return proximoOffset;
    }

    public void setProximoOffset(long proximoOffset) {
        this.proximoOffset = proximoOffset;
    }

    @Override
    public int hashCode() {
        return Math.abs(Double.valueOf(valorUnitario).hashCode());
    }

    @Override
    public short size() {
        return TAMANHO;
    }

    @Override
    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeDouble(valorUnitario);
        dos.writeInt(id);
        dos.writeLong(proximoOffset);
        return baos.toByteArray();
    }

    @Override
    public void fromByteArray(byte[] ba) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(ba);
        DataInputStream dis = new DataInputStream(bais);
        this.valorUnitario = dis.readDouble();
        this.id = dis.readInt();
        this.proximoOffset = dis.readLong();
    }

    @Override
    public String toString() {
        return String.format("NoListaValor{valor=%.2f, id=%d, proxOffset=%d}", 
            valorUnitario, id, proximoOffset);
    }
}