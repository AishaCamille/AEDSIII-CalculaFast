package com.calculafast.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Pagamento implements Registro {
    private int id;
    private double valorPago;
    private double valorComDesconto;
    private int idPessoa;
    private int idComanda;

    public Pagamento() {}

    public Pagamento(int id, double valorPago, double valorComDesconto, int idPessoa, int idComanda) {
        this.id = id;
        this.valorPago = valorPago;
        this.valorComDesconto = valorComDesconto;
        this.idPessoa = idPessoa;
        this.idComanda = idComanda;
    }
     public Pagamento( double valorPago, double valorComDesconto, int idPessoa, int idComanda) {
        
        this.valorPago = valorPago;
        this.valorComDesconto = valorComDesconto;
        this.idPessoa = idPessoa;
        this.idComanda = idComanda;
    }

    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public double getValorPago() { return valorPago; }
    public void setValorPago(double valorPago) { this.valorPago = valorPago; }

    public double getValorComDesconto() { return valorComDesconto; }
    public void setValorComDesconto(double valorComDesconto) { this.valorComDesconto = valorComDesconto; }

    public int getIdPessoa() { return idPessoa; }
    public void setIdPessoa(int idPessoa) { this.idPessoa = idPessoa; }

    public int getIdComanda() { return idComanda; }
    public void setIdComanda(int idComanda) { this.idComanda = idComanda; }

     // Implementação do método toByteArray()

    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeInt(this.id);
        dos.writeDouble(this.valorPago);
        dos.writeDouble(this.valorComDesconto);
        dos.writeInt(this.idPessoa);
        dos.writeInt(this.idComanda);
        return baos.toByteArray();
    }
    
    //metodo de from byte to array

    public void fromByteArray(byte[] b) throws IOException{
        ByteArrayInputStream bais= new ByteArrayInputStream(b);
        DataInputStream dis = new DataInputStream(bais);
        this.id = dis.readInt();
        this.valorPago = dis.readDouble();
        this.valorComDesconto = dis.readDouble();
         this.idPessoa = dis.readInt();
        this.idComanda = dis.readInt();
       
    }
      @Override
    public String toString() {
        return "\nID........: " + this.id +
               "\nValor Pago......: " + this.valorPago +
               "\nValor com Desconto.......: " + this.valorComDesconto +
               "\nid Pessoa..........." +this.idPessoa +
               "\nid Comanda.........." + this.idComanda;
    }
}
