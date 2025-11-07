package index.hash;

import java.io.*;

public class ParValorOffset implements RegistroHashExtensivel<ParValorOffset> {
    private double valorUnitario;
    private int id;
    private int proximoId; // ← NOVO: ponteiro para próximo registro com mesmo valor
    private static final short TAMANHO = 16; // 8 (double) + 4 (int) + 4 (int)

    public ParValorOffset() {
        this(0.0, -1, -1);
    }

    public ParValorOffset(double valorUnitario, int id) {
        this(valorUnitario, id, -1);
    }

    public ParValorOffset(double valorUnitario, int id, int proximoId) {
        this.valorUnitario = valorUnitario;
        this.id = id;
        this.proximoId = proximoId;
    }

    public double getValorUnitario() {
        return valorUnitario;
    }

    public int getId() {
        return id;
    }

    public int getProximoId() {
        return proximoId;
    }

    public void setProximoId(int proximoId) {
        this.proximoId = proximoId;
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
        dos.writeInt(proximoId);
        return baos.toByteArray();
    }

    @Override
    public void fromByteArray(byte[] ba) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(ba);
        DataInputStream dis = new DataInputStream(bais);
        this.valorUnitario = dis.readDouble();
        this.id = dis.readInt();
        this.proximoId = dis.readInt();
    }
}