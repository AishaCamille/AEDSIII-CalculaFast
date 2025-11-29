package com.calculafast.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class PessoaComanda implements Registro {

    private int id;
    private String nome;
    private int idComanda;   
    private double consumoPessoa;

    public PessoaComanda() {
        this.id = -1;
        this.nome = "";
        this.idComanda = -1;
        this.consumoPessoa = 0.0;
    }

    public PessoaComanda(int id, String nome, int idComanda,  double consumoPessoa) {
        this.id = id;
        this.nome = nome;
        this.idComanda = idComanda;
        this.consumoPessoa = consumoPessoa;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public int getIdComanda() {
        return idComanda;
    }

    public void setIdComanda(int idComanda) {
        this.idComanda = idComanda;
    }

    

    public double getConsumoPessoa() {
        return consumoPessoa;
    }

    public void setConsumoPessoa(double consumoPessoa) {
        this.consumoPessoa = consumoPessoa;
    }

    @Override
    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(out);

        dos.writeInt(id);
        dos.writeUTF(nome);
        dos.writeInt(idComanda);

        dos.writeDouble(consumoPessoa);

        return out.toByteArray();
    }

    @Override
    public void fromByteArray(byte[] ba) throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream(ba);
        DataInputStream dis = new DataInputStream(in);

        id = dis.readInt();
        nome = dis.readUTF();
        idComanda = dis.readInt();


        consumoPessoa = dis.readDouble();
    }
    @Override
    public PessoaComanda clone() {
        return new PessoaComanda(
            this.id,
            this.nome,
            this.idComanda,
            this.consumoPessoa
        );
    }

    @Override
    public String toString() {
        return "\nID: " + id +
                "\nNome: " + nome +
                "\nID Comanda: " + idComanda +
                "\nConsumo: " + consumoPessoa;
    }

    @Override
    public boolean equals(Object obj) {
        return (this.id == ((PessoaComanda) obj).getId());
    }
}
