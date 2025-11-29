package com.calculafast.model;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Item implements Registro{
    private int id;
    private String descricao;
    private double valor;
    private int quantidade;

    // Construtor 
    public Item() {
        this.id = -1;
        this.descricao = null; 
        this.valor = 0.0;
        this.quantidade=0;
        
    }

    // construtor com parametros para inicializar os valores
    public Item(String descricao, double valor, int quantidade) {
        this.id = -1;
        this.descricao = descricao;
        this.valor = valor;
        this.quantidade=quantidade;
    }
    

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public double getValor() {
        return valor;
    }

    public void setValor(double valor) {
        this.valor = valor;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(int quantidade) {
        this.quantidade = quantidade;
    }
 // Implementação do método toByteArray()

    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeInt(this.id);
        dos.writeUTF(this.descricao);
        dos.writeDouble(this.valor);
        dos.writeInt(this.quantidade);
        return baos.toByteArray();
    }
    
    //metodo de from byte to array

    public void fromByteArray(byte[] b) throws IOException{
        ByteArrayInputStream bais= new ByteArrayInputStream(b);
        DataInputStream dis = new DataInputStream(bais);
        this.id = dis.readInt();
        this.descricao = dis.readUTF();
         this.valor = dis.readDouble();
        this.quantidade = dis.readInt();
       
    }
   
    // toString para exibir o item como String
    @Override
    public String toString() {
        return "\nID: " + id +
               "\nDescrição: " + descricao +
               String.format("\nValor: %.2f", valor) +
               "\nQuantidade: " + quantidade;
    }

}
