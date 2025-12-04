package com.calculafast.casamentoDePadrao;

public class BoyerMoore {
    static int NUMERO_DE_CARACTERES = 256; 

    // Função que preenche a tabela de deslocamentos baseada no char ruim
    void preencherTabelaCaractereRuim(char[] padrao, int tamanho, int[] tabelaCaractereRuim) {
        for(int i = 0; i < NUMERO_DE_CARACTERES; i++){
            tabelaCaractereRuim[i] = -1;
        }

        // Preenche com a última posição de ocorrência de cada caractere no padrão
        for(int i = 0; i < tamanho; i++){
            tabelaCaractereRuim[(int) padrao[i]] = i;
        }
    }

    public boolean buscar(String texto, String padrao){
        if (texto == null || padrao == null || padrao.isEmpty()) return false;

        texto = texto.toLowerCase();
        padrao = padrao.toLowerCase();

        char[] arrayTexto = texto.toCharArray();
        char[] arrayPadrao = padrao.toCharArray();
        
        int tamPadrao = arrayPadrao.length;
        int tamTexto = arrayTexto.length;

        int tabelaCaractereRuim[] = new int[NUMERO_DE_CARACTERES];
        
        // pre processamento
        preencherTabelaCaractereRuim(arrayPadrao, tamPadrao, tabelaCaractereRuim);

        int deslocamento = 0;
        
        while(deslocamento <= (tamTexto - tamPadrao)){
            int j = tamPadrao - 1;

            //direita para a esquerda 
            while(j >= 0 && arrayPadrao[j] == arrayTexto[deslocamento + j]){
                j--;
            }

            if(j < 0){ //para quando achar
                return true;

               /*
                if (deslocamento + tamPadrao < tamTexto) {
                    
                    deslocamento += tamPadrao - tabelaCaractereRuim[arrayTexto[deslocamento + tamPadrao]];
                } else {
                    deslocamento += 1;
                } */ 
            } else {
                // Caso de incompatibilidade (Mismatch)
                deslocamento += Math.max(1, j - tabelaCaractereRuim[arrayTexto[deslocamento + j]]);
            }
        }
        return false;
    }
}