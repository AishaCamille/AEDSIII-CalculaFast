package model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Pessoa implements Registro{
	private int id;
	private String nome;
	private String email;
	private String senha;
	
	//construtores
	public Pessoa() {
		id = -1;
		nome = "";
		email= "";
		senha= "";
	}
	

	public Pessoa(int id, String nome, String email, String senha) {
		this.id=id;
		this.nome=nome;
		this.email=email;
		this.senha=senha;
		
	}		
		public Pessoa( String nome, String email, String senha) {
		
		this.nome=nome;
		this.email=email;
		this.senha=senha;
		
	}	
	//getters e setters
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

	
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

	
  // Implementação do método toByteArray()

    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeInt(this.id);
        dos.writeUTF(this.nome);
		dos.writeUTF(this.email);
		dos.writeUTF(this.senha);
        return baos.toByteArray();
    }
	
	  //metodo de from byte to array

    public void fromByteArray(byte[] b) throws IOException{
        ByteArrayInputStream bais= new ByteArrayInputStream(b);
        DataInputStream dis = new DataInputStream(bais);
        this.id = dis.readInt();
        this.nome = dis.readUTF();
		this.email= dis.readUTF();
		this.senha= dis.readUTF();
        
    }

	/**
	 * Método sobreposto da classe Object. É executado quando um objeto precisa
	 * ser exibido na forma de String.
	 */
	@Override
	public String toString() {
		return "\nID: "+ id +
				"\nNome: " + nome +
				"\nEmail:" + email;
	}
	
	@Override
	public boolean equals(Object obj) {
		return (this.getId() == ((Pessoa) obj).getId());
	}	

}
