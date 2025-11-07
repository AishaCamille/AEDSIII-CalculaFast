package model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Pessoa_Comanda_Item implements Registro {
    private int id;
    private int idPessoa;
    private int idItem;
    private int idComanda;
    private int quantidade;
    private double valorUnitario;
    private static final int TAMANHO_REGISTRO = 36; // 6 inteiros (4*6=24) + 1 double (8) = 32 bytes

    public Pessoa_Comanda_Item(int id, int idPessoa, int idItem, int idComanda, int quantidade, double valorUnitario) {
        this.id = id;
        this.idPessoa = idPessoa;
        this.idItem = idItem;
        this.idComanda = idComanda;
        this.quantidade = quantidade;
        this.valorUnitario = valorUnitario;
    }

    public Pessoa_Comanda_Item(int idPessoa, int idItem, int idComanda, int quantidade, double valorUnitario) {
        this.id = -1; // Será definido quando inserido no arquivo
        this.idPessoa = idPessoa;
        this.idItem = idItem;
        this.idComanda = idComanda;
        this.quantidade = quantidade;
        this.valorUnitario = valorUnitario;
    }

    public Pessoa_Comanda_Item() {
        this(-1, -1, -1, -1, 0, 0.0);
    }

    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getIdPessoa() { return idPessoa; }
    public void setIdPessoa(int idPessoa) { this.idPessoa = idPessoa; }

    public int getIdItem() { return idItem; }
    public void setIdItem(int idItem) { this.idItem = idItem; }

    public int getIdComanda() { return idComanda; }
    public void setIdComanda(int idComanda) { this.idComanda = idComanda; }

    public int getQuantidade() { return quantidade; }
    public void setQuantidade(int quantidade) { this.quantidade = quantidade; }

    public double getValorUnitario() { return valorUnitario; }
    public void setValorUnitario(double valorUnitario) { this.valorUnitario = valorUnitario; }

    // Método para obter o tamanho fixo do registro
    public int size() {
        return TAMANHO_REGISTRO;
    }

    // Implementação do método toByteArray()
    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        
        dos.writeInt(this.id);
        dos.writeInt(this.idPessoa);
        dos.writeInt(this.idItem);
        dos.writeInt(this.idComanda);
        dos.writeInt(this.quantidade);
        dos.writeDouble(this.valorUnitario);
        
        // Garante que o tamanho seja sempre fixo
        if (baos.size() < TAMANHO_REGISTRO) {
            byte[] padding = new byte[TAMANHO_REGISTRO - baos.size()];
            dos.write(padding);
        }
        
        return baos.toByteArray();
    }

    // Implementação do método fromByteArray
    public void fromByteArray(byte[] b) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(b);
        DataInputStream dis = new DataInputStream(bais);
        
        this.id = dis.readInt();
        this.idPessoa = dis.readInt();
        this.idItem = dis.readInt();
        this.idComanda = dis.readInt();
        this.quantidade = dis.readInt();
        this.valorUnitario = dis.readDouble();
        
        // Os bytes restantes são ignorados (padding)
    }

    // Método para criar uma chave única baseada no valorUnitario
    // Útil para o índice hash
    public int getChaveValorUnitario() {
        return Math.abs(Double.valueOf(valorUnitario).hashCode());
    }

    // Método para verificar se é um registro válido
    public boolean isValid() {
        return idPessoa > 0 && idItem > 0 && idComanda > 0 && quantidade >= 0;
    }

    // Método para calcular o valor total
    public double getValorTotal() {
        return quantidade * valorUnitario;
    }

    @Override
    public String toString() {
        return "\nID................: " + this.id +
               "\nID Pessoa.........: " + this.idPessoa +
               "\nID Item...........: " + this.idItem +
               "\nID Comanda........: " + this.idComanda +
               "\nQuantidade........: " + this.quantidade +
               "\nValor Unitário....: R$ " + String.format("%.2f", this.valorUnitario) +
               "\nValor Total.......: R$ " + String.format("%.2f", getValorTotal());
    }

    // Método equals para comparação
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Pessoa_Comanda_Item that = (Pessoa_Comanda_Item) obj;
        return id == that.id && 
               idPessoa == that.idPessoa && 
               idItem == that.idItem && 
               idComanda == that.idComanda;
    }

    // Método hashCode para uso em coleções
    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + idPessoa;
        result = 31 * result + idItem;
        result = 31 * result + idComanda;
        return result;
    }
}