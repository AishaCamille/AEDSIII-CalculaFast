package com.calculafast.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Pessoa_Comanda_Item implements Registro {
    private int idPessoaComanda;
    private int idComanda;
    private int idItem;
    private int quantidade;
    private double valorUnitario;
    private static final int TAMANHO_REGISTRO = 24;

    public Pessoa_Comanda_Item(int idPessoaComanda, int idComanda, int idItem, int quantidade, double valorUnitario) {
        this.idPessoaComanda = idPessoaComanda;
        this.idComanda = idComanda;
        this.idItem = idItem;
        this.quantidade = quantidade;
        this.valorUnitario = valorUnitario;
    }

    public Pessoa_Comanda_Item() {
        this(-1, -1, -1, 0, 0.0);
    }

    // Getters e Setters
    public int getIdPessoaComanda() { return idPessoaComanda; }
    public void setIdPessoaComanda(int idPessoaComanda) { this.idPessoaComanda = idPessoaComanda; }

    public int getIdComanda() { return idComanda; }
    public void setIdComanda(int idComanda) { this.idComanda = idComanda; }

    public int getIdItem() { return idItem; }
    public void setIdItem(int idItem) { this.idItem = idItem; }

    public int getQuantidade() { return quantidade; }
    public void setQuantidade(int quantidade) { this.quantidade = quantidade; }

    public double getValorUnitario() { return valorUnitario; }
    public void setValorUnitario(double valorUnitario) { this.valorUnitario = valorUnitario; }

     @Override
    public int getId() {
        return getChaveComposta();
    }

    @Override
    public void setId(int id) {
        // Este método é necessário pela interface 
    }

    // Gera hash da chave composta
    public int getChaveComposta() {
        return Math.abs((idPessoaComanda * 31) + (idComanda * 17) + (idItem * 7));
    }

    public int size() {
        return TAMANHO_REGISTRO;
    }

    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        
        dos.writeInt(this.idPessoaComanda);
        dos.writeInt(this.idComanda);
        dos.writeInt(this.idItem);
        dos.writeInt(this.quantidade);
        dos.writeDouble(this.valorUnitario);
        
        return baos.toByteArray();
    }

    public void fromByteArray(byte[] b) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(b);
        DataInputStream dis = new DataInputStream(bais);
        
        this.idPessoaComanda = dis.readInt();
        this.idComanda = dis.readInt();
        this.idItem = dis.readInt();
        this.quantidade = dis.readInt();
        this.valorUnitario = dis.readDouble();
    }

    @Override
    public String toString() {
           return "\nID PessoaComanda.........: " + this.idPessoaComanda +
               "\nID Comanda........: " + this.idComanda +
               "\nID Item...........: " + this.idItem +
               "\nQuantidade........: " + this.quantidade +
               "\nValor Unitário....: R$ " + String.format("%.2f", this.valorUnitario) +
               "\nValor Total.......: R$ " + String.format("%.2f", getValorTotal()) +
               "\nChave Composta....: " + getChaveComposta();
    }

    public double getValorTotal() {
        return quantidade * valorUnitario;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Pessoa_Comanda_Item that = (Pessoa_Comanda_Item) obj;
        return idPessoaComanda == that.idPessoaComanda && 
               idComanda == that.idComanda && 
               idItem == that.idItem;
    }

    @Override
    public int hashCode() {
        return getChaveComposta();
    }
}