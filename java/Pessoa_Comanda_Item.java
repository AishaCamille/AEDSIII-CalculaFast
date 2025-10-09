

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

    public Pessoa_Comanda_Item(int id, int idPessoa, int idItem, int idComanda, int quantidade, double valorUnitario) {
        this.id = id;
        this.idPessoa = idPessoa;
        this.idItem = idItem;
        this.idComanda = idComanda;
        this.quantidade = quantidade;
        this.valorUnitario = valorUnitario;
    }
     public Pessoa_Comanda_Item( int idPessoa, int idItem, int idComanda, int quantidade, double valorUnitario) {
      
        this.idPessoa = idPessoa;
        this.idItem = idItem;
        this.idComanda = idComanda;
        this.quantidade = quantidade;
        this.valorUnitario = valorUnitario;
    }

    public Pessoa_Comanda_Item() {}

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
        return baos.toByteArray();
    }
// frombytearray
     public void fromByteArray(byte[] b) throws IOException{
        ByteArrayInputStream bais= new ByteArrayInputStream(b);
        DataInputStream dis = new DataInputStream(bais);
        this.id = dis.readInt();
        this.idPessoa = dis.readInt();
        this.idItem = dis.readInt();
        this.idComanda = dis.readInt();
        this.quantidade = dis.readInt();
        this.valorUnitario = dis.readDouble();
    }
       @Override
    public String toString() {
        return "\nID........: " + this.id +
               "\nId pessoa......: " + this.idPessoa +
               "\nid Item.......: " + this.idItem+
               "\nid Comanda.......: " + this.idComanda+
               "\nquantidade.......: " + this.quantidade+
               "\nvalor unitário.......: " + this.valorUnitario;
    }
}
