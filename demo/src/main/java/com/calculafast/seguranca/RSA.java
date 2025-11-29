package com.calculafast.seguranca;

import java.math.BigInteger;
import java.security.SecureRandom;

public class RSA {
    private BigInteger p;
    private BigInteger q;
    private BigInteger n;
    private BigInteger z;
    private BigInteger d;
    private BigInteger e;

    // Construtor que inicializa as chaves
    public RSA() {
        gerarChaves();
    }

    private void gerarChaves() {
        
        // escolher 2 numeros primos grande
        SecureRandom random = new SecureRandom();
        p = BigInteger.probablePrime(512, random);
        q = BigInteger.probablePrime(512, random);

        // Calcular n = p * q
        n = p.multiply(q);

        //calcular z = (p-1) * (q-1)
        z = p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE));

        //escolher um numero relativamente primo a z e chama de d, ou seja primo que não é divisível por z
        //escolher e de forma que (e* d) mod z=1
        e = BigInteger.valueOf(65537);
        
        //verificação se z é relativamente primo
        if (!e.gcd(z).equals(BigInteger.ONE)) {
            e = BigInteger.valueOf(3);
            while (!e.gcd(z).equals(BigInteger.ONE)) {
                e = e.add(BigInteger.TWO); 
            }
        }
        
        // Calcula d usando o inverso modular: d = e^-1 mod z
     
        d = e.modInverse(z);
    }

    private int converterCharParaNumero(char c) {
        //se for letra, converte para posição no alfabeto
        if (Character.isLetter(c)) {
            char minuscula = Character.toLowerCase(c);
            return (minuscula - 'a') + 1;
        }
        
        // Se for dígito, retorna valor + 27 para não conflitar com letras
        if (Character.isDigit(c)) {
            return (c - '0') + 27; 
        }
        
        // Para outros símbolos, usa o código ASCII + 37
        return ((int) c) + 37;
    }

    private char converterNumeroParaChar(int num) {
        // Se for número de 1 a 26, é uma letra
        if (num >= 1 && num <= 26) {
            return (char) ('a' + (num - 1));
        }
        
        // Se for 27-36, é um dígito
        if (num >= 27 && num <= 36) {
            return (char) ('0' + (num - 27));
        }
        
        // não é letra nem numero ent é um símbolo
        return (char) (num - 37);
    }

    // para cifrar, calcula c=texto puro elevado a e mod n
    public BigInteger[] cifrar(String textoPuro) {
        BigInteger[] textoCifrado = new BigInteger[textoPuro.length()];

        for (int i = 0; i < textoPuro.length(); i++) {
            char caractere = textoPuro.charAt(i);
            
            int numeroCaractere = converterCharParaNumero(caractere);
            
            // c = (m ^ e) mod n
            BigInteger m = BigInteger.valueOf(numeroCaractere);
            BigInteger c = m.modPow(e, n);
            
            textoCifrado[i] = c;
        }

        return textoCifrado;
    }

    // para decifrar texto puro elevado a d mod n
    public String decifrar(BigInteger[] textoCifrado) {
        StringBuilder textoDecifrado = new StringBuilder();

        for (BigInteger c : textoCifrado) {
            //descriptar m = (c ^ d) mod n
            BigInteger m = c.modPow(d, n);
            
            int numeroCaractere = m.intValue();
            char caractere = converterNumeroParaChar(numeroCaractere);
            
            textoDecifrado.append(caractere);
        }

        return textoDecifrado.toString();
    }

    public BigInteger getChavePublicaE() {
        return e;
    }

    public BigInteger getChavePublicaN() {
        return n;
    }

    public BigInteger getChavePrivadaD() {
        return d;
    }

    public BigInteger getChavePrivadaN() {
        return n;
    }

   
}