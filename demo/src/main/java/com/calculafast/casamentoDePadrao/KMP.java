package com.calculafast.casamentoDePadrao;

public class KMP {
    public boolean kmp(String texto, String padrao){
        if (texto == null || padrao == null || padrao.isEmpty()) return false;

        texto = texto.toLowerCase();
        padrao = padrao.toLowerCase();

        int[] lps= new int[padrao.length()];
        lps= construirLPS(padrao, padrao.length());
        int i=0;
        int j=0;
        while(i<texto.length()){
            if(texto.charAt(i)==padrao.charAt(j)){
                i++;
                j++;
            }
            if(j==padrao.length()){
                return true;
              //  j=lps[j-1]; procurando p saber se o item existe
            }else if(i<texto.length() && texto.charAt(i)!=padrao.charAt(j)){
                if(j!=0){
                    j=lps[j-1];
                }else{
                    i++;
                }
            }
        }
        return false;
    }
    

    int[] construirLPS(String padrao, int m){
        int lps[]= new int[m];
        int len=0; //tam do pefixo anterior mais longo
        int i=1;// começa do segundo char
        lps[0]=0; //lps do primeiro char é sempre 0
        while(i<m){//caso onde os char coincidem
            if(padrao.charAt(i)==padrao.charAt(len)){
                len++;
                lps[i]=len;
                i++;
            }else{//não coincide
                if(len!=0){
                    len=lps[len-1];//recua o len para o valor do prefixo anterior q combinava
                }else{//caso que len é 0 ent n teve match
                    lps[i]=len;
                    i++;
                }
            }
        }
        return lps;
    }
}