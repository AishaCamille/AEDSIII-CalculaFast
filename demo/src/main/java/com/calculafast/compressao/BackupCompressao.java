package com.calculafast.compressao;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.text.SimpleDateFormat;

/**
 * Sistema de Backup com Compressão para os arquivos de dados do sistema.
 * 
 * Responsável por agrupar todos os arquivos .db em um único arquivo,
 * comprimir usando Huffman ou LZW e permitir restauração completa dos dados.
 */
public class BackupCompressao {

    public static final int COMPRESSAO_HUFFMAN = 1;
    public static final int COMPRESSAO_LZW = 2;

    private static final byte[] MAGIC_NUMBER = {'C', 'F', 'B', 'K'};
    private static final byte VERSAO = 1;
    private static final String ARQUIVO_BACKUP = "./backup.cfbk";

    private String diretorioDados;
    private Huffman huffman;
    private LZW lzw;
    private Map<String, Object> ultimasEstatisticas;

    public BackupCompressao() {
        this("./dados");
    }

    public BackupCompressao(String diretorioDados) {
        this.diretorioDados = diretorioDados;
        this.huffman = new Huffman();
        this.lzw = new LZW();
        this.ultimasEstatisticas = new HashMap<>();
    }

    public boolean criarBackup(int tipoCompressao) throws IOException {
        ultimasEstatisticas.clear();
        ultimasEstatisticas.put("tipoCompressao", getNomeCompressao(tipoCompressao));
        
        List<Path> arquivosDb = listarArquivosDb();
        
        if (arquivosDb.isEmpty()) {
            return false;
        }

        ByteArrayOutputStream dadosAgrupados = new ByteArrayOutputStream();
        DataOutputStream dosAgrupado = new DataOutputStream(dadosAgrupados);

        dosAgrupado.writeInt(arquivosDb.size());

        long tamanhoTotalOriginal = 0;
        List<Map<String, Object>> infoArquivos = new ArrayList<>();

        for (Path arquivo : arquivosDb) {
            String caminhoRelativo = Paths.get(diretorioDados).relativize(arquivo).toString();
            byte[] conteudo = Files.readAllBytes(arquivo);
            
            tamanhoTotalOriginal += conteudo.length;
            
            dosAgrupado.writeUTF(caminhoRelativo);
            dosAgrupado.writeLong(conteudo.length);
            dosAgrupado.write(conteudo);

            Map<String, Object> info = new HashMap<>();
            info.put("caminho", caminhoRelativo);
            info.put("tamanhoOriginal", conteudo.length);
            infoArquivos.add(info);
        }

        dosAgrupado.close();
        byte[] dadosOriginais = dadosAgrupados.toByteArray();

        byte[] dadosComprimidos;
        long tempoInicio = System.currentTimeMillis();
        
        if (tipoCompressao == COMPRESSAO_HUFFMAN) {
            dadosComprimidos = huffman.comprimir(dadosOriginais);
        } else {
            dadosComprimidos = lzw.comprimir(dadosOriginais);
        }
        
        long tempoCompressao = System.currentTimeMillis() - tempoInicio;

        try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(ARQUIVO_BACKUP))) {
            dos.write(MAGIC_NUMBER);
            dos.writeByte(VERSAO);
            dos.writeByte(tipoCompressao);
            dos.writeLong(System.currentTimeMillis());
            dos.writeInt(arquivosDb.size());
            dos.writeLong(dadosOriginais.length);
            dos.writeLong(dadosComprimidos.length);
            dos.write(dadosComprimidos);
        }

        long tamanhoArquivoFinal = new File(ARQUIVO_BACKUP).length();
        double taxaCompressao = (1.0 - ((double) dadosComprimidos.length / dadosOriginais.length)) * 100;
        double razaoCompressao = (double) dadosOriginais.length / dadosComprimidos.length;

        ultimasEstatisticas.put("numeroArquivos", arquivosDb.size());
        ultimasEstatisticas.put("tamanhoTotalOriginal", tamanhoTotalOriginal);
        ultimasEstatisticas.put("tamanhoDadosAgrupados", (long) dadosOriginais.length);
        ultimasEstatisticas.put("tamanhoComprimido", (long) dadosComprimidos.length);
        ultimasEstatisticas.put("tamanhoArquivoFinal", tamanhoArquivoFinal);
        ultimasEstatisticas.put("taxaCompressao", taxaCompressao);
        ultimasEstatisticas.put("razaoCompressao", razaoCompressao);
        ultimasEstatisticas.put("tempoCompressaoMs", tempoCompressao);
        ultimasEstatisticas.put("arquivos", infoArquivos);

        return true;
    }

    public boolean restaurarBackup() throws IOException, ClassNotFoundException {
        File arquivoBackup = new File(ARQUIVO_BACKUP);
        if (!arquivoBackup.exists()) {
            return false;
        }

        try (DataInputStream dis = new DataInputStream(new FileInputStream(ARQUIVO_BACKUP))) {
            byte[] magic = new byte[4];
            dis.readFully(magic);
            if (!Arrays.equals(magic, MAGIC_NUMBER)) {
                return false;
            }

            byte versao = dis.readByte();
            int tipoCompressao = dis.readByte();
            long timestamp = dis.readLong();
            int numArquivos = dis.readInt();
            long tamanhoOriginal = dis.readLong();
            long tamanhoComprimido = dis.readLong();

            byte[] dadosComprimidos = new byte[(int) tamanhoComprimido];
            dis.readFully(dadosComprimidos);

            byte[] dadosOriginais;
            
            if (tipoCompressao == COMPRESSAO_HUFFMAN) {
                dadosOriginais = huffman.descomprimir(dadosComprimidos);
            } else {
                dadosOriginais = lzw.descomprimir(dadosComprimidos);
            }

            ByteArrayInputStream bais = new ByteArrayInputStream(dadosOriginais);
            DataInputStream disArquivos = new DataInputStream(bais);

            int numArquivosLido = disArquivos.readInt();

            for (int i = 0; i < numArquivosLido; i++) {
                String caminhoRelativo = disArquivos.readUTF();
                long tamanhoArquivo = disArquivos.readLong();
                byte[] conteudoArquivo = new byte[(int) tamanhoArquivo];
                disArquivos.readFully(conteudoArquivo);

                Path caminhoCompleto = Paths.get(diretorioDados, caminhoRelativo);
                Files.createDirectories(caminhoCompleto.getParent());
                Files.write(caminhoCompleto, conteudoArquivo);
            }

            return true;
        }
    }

    public boolean existeBackup() {
        return new File(ARQUIVO_BACKUP).exists();
    }

    public Map<String, Object> getInfoBackup() throws IOException {
        Map<String, Object> info = new HashMap<>();
        File arquivo = new File(ARQUIVO_BACKUP);
        
        if (!arquivo.exists()) {
            info.put("existe", false);
            return info;
        }

        try (DataInputStream dis = new DataInputStream(new FileInputStream(ARQUIVO_BACKUP))) {
            byte[] magic = new byte[4];
            dis.readFully(magic);
            if (!Arrays.equals(magic, MAGIC_NUMBER)) {
                info.put("existe", false);
                return info;
            }

            byte versao = dis.readByte();
            int tipoCompressao = dis.readByte();
            long timestamp = dis.readLong();
            int numArquivos = dis.readInt();
            long tamanhoOriginal = dis.readLong();
            long tamanhoComprimido = dis.readLong();

            info.put("existe", true);
            info.put("versao", (int) versao);
            info.put("tipoCompressao", getNomeCompressao(tipoCompressao));
            info.put("dataBackup", timestamp);
            info.put("dataFormatada", new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date(timestamp)));
            info.put("numeroArquivos", numArquivos);
            info.put("tamanhoOriginal", tamanhoOriginal);
            info.put("tamanhoComprimido", tamanhoComprimido);
            info.put("tamanhoArquivo", arquivo.length());
            info.put("taxaCompressao", (1.0 - ((double) tamanhoComprimido / tamanhoOriginal)) * 100);
        }

        return info;
    }

    private List<Path> listarArquivosDb() throws IOException {
        Path base = Paths.get(diretorioDados);
        
        if (!Files.exists(base)) {
            return new ArrayList<>();
        }

        List<Path> arquivos = new ArrayList<>();
        Files.walk(base)
            .filter(Files::isRegularFile)
            .filter(p -> p.toString().endsWith(".db"))
            .forEach(arquivos::add);
        
        return arquivos;
    }

    private String getNomeCompressao(int tipo) {
        if (tipo == COMPRESSAO_HUFFMAN) return "Huffman";
        if (tipo == COMPRESSAO_LZW) return "LZW";
        return "Nenhuma";
    }

    public Map<String, Object> getUltimasEstatisticas() {
        return new HashMap<>(ultimasEstatisticas);
    }

    public void setDiretorioDados(String diretorioDados) {
        this.diretorioDados = diretorioDados;
    }
}
