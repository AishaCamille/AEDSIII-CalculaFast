
package com.calculafast.model;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Comanda implements Registro {
    private int id;
    private int idPessoa;
    private String status;
    private double consumoPessoa;

    public Comanda() {
    }

    // construtor
    public Comanda(int id, int idPessoa, String status, double consumoPessoa) {
        this.id = id;
        this.idPessoa = idPessoa;
        this.status = status;
        this.consumoPessoa = consumoPessoa;
    }

      public Comanda( int idPessoa, String status, double consumoPessoa) {
        
        this.idPessoa = idPessoa;
        this.status = status;
        this.consumoPessoa = consumoPessoa;
    }
    // getters e setters
    public int getId() {
        return id;
    }

    public int getIdPessoa() {
        return idPessoa;
    }

    public String getStatus() {
        return status;
    }

    public double getConsumoPessoa() {
        return consumoPessoa;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setIdPessoa(int idPessoa) {
        this.idPessoa = idPessoa;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setConsumoPessoa(double consumoPessoa) {
        this.consumoPessoa = consumoPessoa;
    }

    // Implementação do método toByteArray()
    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeInt(this.id);
        dos.writeInt(this.idPessoa);
        dos.writeUTF(this.status);
        dos.writeDouble(this.consumoPessoa);
        return baos.toByteArray();
    }
    // metodo de from byte to array

    public void fromByteArray(byte[] b) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(b);
        DataInputStream dis = new DataInputStream(bais);
        this.id = dis.readInt();
        this.idPessoa = dis.readInt();
        this.status = dis.readUTF();
        this.consumoPessoa = dis.readDouble();
    }

    @Override
    public String toString() {
        return "\nID........: " + this.id +
                "\nStatus......: " + this.status +
                "\nConsumo Pessoa.......: " + this.consumoPessoa;
    }
}
