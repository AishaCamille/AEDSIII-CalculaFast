package com.calculafast.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigInteger;

import com.calculafast.seguranca.RSA;

public class Pessoa implements Registro{
	private int id;
	private String nome;
	private String email;
	private String senha;
	private BigInteger[] senhaCriptografada; 
private static RSA rsa = new RSA();
	//construtores
	public Pessoa() {
		id = -1;
		nome = "";
		email= "";
		senha= "";
	}
	

	public Pessoa( String nome, String email, String senha) {
		this.id=-1;
		this.nome=nome;
		this.email=email;
		setSenha(senha); //set para criptografar
		
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
	public static boolean emailValido(String email) {
		if (email == null || email.isEmpty()) return false;

		// Regex pra validar padrões comuns de e-mail
		String regex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";

		return email.matches(regex);
	}

    public void setEmail(String email) {
    if (!emailValido(email)) {
        throw new IllegalArgumentException("E-mail inválido!");
    }
    this.email = email;
}

   public String getSenha() {
    if (senhaCriptografada != null && senhaCriptografada.length > 0) {
        return rsa.decifrar(senhaCriptografada); 
    }
    return "";
}

   public void setSenha(String senha) {
    if (senha != null && !senha.isEmpty()) {
        this.senhaCriptografada = rsa.cifrar(senha); 
		
    }
}

	// Método para obter a senha criptografada para debug
    public String getSenhaCriptografada() {
        return this.senha;
    }


	
  // Implementação do método toByteArray()

    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeInt(this.id);
        dos.writeUTF(this.nome);
		dos.writeUTF(this.email);
		 dos.writeUTF(this.senha != null ? this.senha : "");
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
				"\nEmail:" + email// +
				 /*"\nSenha: [CRIPTOGRAFADA]"+ rsa.decifrar(senhaCriptografada)*/;
	}
	
	@Override
	public boolean equals(Object obj) {
		return (this.getId() == ((Pessoa) obj).getId());
	}	

}
