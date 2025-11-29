package com.calculafast.model;

import java.io.File;
import java.io.RandomAccessFile;
import java.lang.reflect.Constructor;

public class Arquivo<T extends Registro> {
    private static final int TAM_CABECALHO = 12; // 4 bytes (último ID) + 8 bytes (ponteiro lista de excluídos)
    private RandomAccessFile arquivo;
    private String nomeArquivo;
    private Constructor<T> construtor;

    public Arquivo(String nomeArquivo, Constructor<T> construtor) throws Exception {
        File diretorio = new File("./dados");
        if (!diretorio.exists())
            diretorio.mkdir();

        diretorio = new File("./dados/" + nomeArquivo);
        if (!diretorio.exists())
            diretorio.mkdir();

        this.nomeArquivo = "./dados/" + nomeArquivo + "/" + nomeArquivo + ".db";
        this.construtor = construtor;
        this.arquivo = new RandomAccessFile(this.nomeArquivo, "rw");

        if (arquivo.length() < TAM_CABECALHO) {
            arquivo.writeInt(0); // Último ID usado
            arquivo.writeLong(-1); // Lista de registros excluídos
        }
    }

    public int create(T obj) throws Exception {
        // Descobre último ID registrado no cabeçalho
        arquivo.seek(0);
        int ultimoId = arquivo.readInt();

        int idSolicitado = obj.getId();
        int novoID = (idSolicitado > 0) ? idSolicitado : (ultimoId + 1);

        if (idSolicitado > 0) {
            T existente = read(idSolicitado);
            if (existente != null) {
                return -1; // ID já existe
            }
        }

        // Atualiza o cabeçalho com o maior ID conhecido
        int novoUltimoId = Math.max(ultimoId, novoID);
        arquivo.seek(0);
        arquivo.writeInt(novoUltimoId);

        // Persiste o registro
        obj.setId(novoID);
        byte[] dados = obj.toByteArray();

        long endereco = getDeleted(dados.length);
        if (endereco == -1) {
            arquivo.seek(arquivo.length());
            endereco = arquivo.getFilePointer();
            arquivo.writeByte(' '); // Lápide
            arquivo.writeShort(dados.length);
            arquivo.write(dados);
        } else {
            arquivo.seek(endereco);
            arquivo.writeByte(' '); // Remove a lápide
            arquivo.skipBytes(2);
            arquivo.write(dados);
        }
        return obj.getId();
    }

    public long createWithOffset(T obj) throws Exception {
        arquivo.seek(0);
        int ultimoId = arquivo.readInt();

        int idSolicitado = obj.getId();
        int novoID = (idSolicitado > 0) ? idSolicitado : (ultimoId + 1);
        if (idSolicitado > 0) {
            T existente = read(idSolicitado);
            if (existente != null)
                return -1L;
        }

        int novoUltimoId = Math.max(ultimoId, novoID);
        arquivo.seek(0);
        arquivo.writeInt(novoUltimoId);

        // Persiste o registro
        obj.setId(novoID);
        byte[] dados = obj.toByteArray();

        long endereco = getDeleted(dados.length);
        if (endereco == -1) {
            arquivo.seek(arquivo.length());
            endereco = arquivo.getFilePointer();
            arquivo.writeByte(' ');
            arquivo.writeShort(dados.length);
            arquivo.write(dados);
        } else {
            arquivo.seek(endereco);
            arquivo.writeByte(' ');
            arquivo.skipBytes(2);
            arquivo.write(dados);
        }
        return endereco;
    }

    public T read(int id) throws Exception {
        arquivo.seek(TAM_CABECALHO);
        int tentativas = 0;
        while (arquivo.getFilePointer() < arquivo.length()) {
            long posicao = arquivo.getFilePointer();
            byte lapide = arquivo.readByte();
            short tamanho = arquivo.readShort();
            byte[] dados = new byte[tamanho];
            arquivo.read(dados);
         
            if (lapide == ' ') {
                T obj = construtor.newInstance();
                obj.fromByteArray(dados);
                if (obj.getId() == id) {
                    return obj;
                }
            }
        }
        return null;
    }

    public boolean delete(int id) throws Exception {
        arquivo.seek(TAM_CABECALHO);
        while (arquivo.getFilePointer() < arquivo.length()) {
            long posicao = arquivo.getFilePointer();
            byte lapide = arquivo.readByte();
            short tamanho = arquivo.readShort();
            byte[] dados = new byte[tamanho];
            arquivo.read(dados);

            if (lapide == ' ') {
                T obj = construtor.newInstance();
                obj.fromByteArray(dados);
                if (obj.getId() == id) {
                    arquivo.seek(posicao);
                    arquivo.writeByte('*');
                    addDeleted(tamanho, posicao);
                    return true;
                }
            }
        }
        return false;
    }

    public boolean update(T novoObj) throws Exception {
        arquivo.seek(TAM_CABECALHO);
        while (arquivo.getFilePointer() < arquivo.length()) {
            long posicao = arquivo.getFilePointer();
            byte lapide = arquivo.readByte();
            short tamanho = arquivo.readShort();
            byte[] dados = new byte[tamanho];
            arquivo.read(dados);

            if (lapide == ' ') {
                T obj = construtor.newInstance();
                obj.fromByteArray(dados);
                if (obj.getId() == novoObj.getId()) {
                    byte[] novosDados = novoObj.toByteArray();
                    short novoTam = (short) novosDados.length;

                    if (novoTam <= tamanho) {
                        arquivo.seek(posicao + 3);
                        arquivo.write(novosDados);
                    } else {
                        arquivo.seek(posicao);
                        arquivo.writeByte('*');
                        addDeleted(tamanho, posicao);

                        long novoEndereco = getDeleted(novosDados.length);
                        if (novoEndereco == -1) {
                            arquivo.seek(arquivo.length());
                            novoEndereco = arquivo.getFilePointer();
                            arquivo.writeByte(' ');
                            arquivo.writeShort(novoTam);
                            arquivo.write(novosDados);
                        } else {
                            arquivo.seek(novoEndereco);
                            arquivo.writeByte(' ');
                            arquivo.skipBytes(2);
                            arquivo.write(novosDados);
                        }
                    }
                    return true;
                }
            }
        }
        return false;
    }

    // Atualiza e retorna o offset onde o registro ficou ao final
    public long updateWithOffset(T novoObj) throws Exception {
        arquivo.seek(TAM_CABECALHO);
        while (arquivo.getFilePointer() < arquivo.length()) {
            long posicao = arquivo.getFilePointer();
            byte lapide = arquivo.readByte();
            short tamanho = arquivo.readShort();
            byte[] dados = new byte[tamanho];
            arquivo.read(dados);

            if (lapide == ' ') {
                T obj = construtor.newInstance();
                obj.fromByteArray(dados);
                if (obj.getId() == novoObj.getId()) {
                    byte[] novosDados = novoObj.toByteArray();
                    short novoTam = (short) novosDados.length;
                    if (novoTam <= tamanho) {
                        arquivo.seek(posicao + 3);
                        arquivo.write(novosDados);
                        return posicao;
                    } else {
                        arquivo.seek(posicao);
                        arquivo.writeByte('*');
                        addDeleted(tamanho, posicao);

                        long novoEndereco = getDeleted(novosDados.length);
                        if (novoEndereco == -1) {
                            arquivo.seek(arquivo.length());
                            novoEndereco = arquivo.getFilePointer();
                            arquivo.writeByte(' ');
                            arquivo.writeShort(novoTam);
                            arquivo.write(novosDados);
                        } else {
                            arquivo.seek(novoEndereco);
                            arquivo.writeByte(' ');
                            arquivo.skipBytes(2);
                            arquivo.write(novosDados);
                        }
                        return novoEndereco;
                    }
                }
            }
        }
        return -1L;
    }

    private void addDeleted(int tamanhoEspaco, long enderecoEspaco) throws Exception {
        long posicao = 4;
        arquivo.seek(posicao);
        long endereco = arquivo.readLong();
        long proximo;

        if (endereco == -1) {
            arquivo.seek(4);
            arquivo.writeLong(enderecoEspaco);
            arquivo.seek(enderecoEspaco + 3);
            arquivo.writeLong(-1);
        } else {
            do {
                arquivo.seek(endereco + 1);
                int tamanho = arquivo.readShort();
                proximo = arquivo.readLong();

                if (tamanho > tamanhoEspaco) {
                    if (posicao == 4)
                        arquivo.seek(posicao);
                    else
                        arquivo.seek(posicao + 3);
                    arquivo.writeLong(enderecoEspaco);
                    arquivo.seek(enderecoEspaco + 3);
                    arquivo.writeLong(endereco);
                    break;
                }

                if (proximo == -1) {
                    arquivo.seek(endereco + 3);
                    arquivo.writeLong(enderecoEspaco);
                    arquivo.seek(enderecoEspaco + 3);
                    arquivo.writeLong(-1);
                    break;
                }

                posicao = endereco;
                endereco = proximo;
            } while (endereco != -1);
        }
    }

    private long getDeleted(int tamanhoNecessario) throws Exception {
        long posicao = 4;
        arquivo.seek(posicao);
        long endereco = arquivo.readLong();
        long proximo;
        int tamanho;

        while (endereco != -1) {
            arquivo.seek(endereco + 1);
            tamanho = arquivo.readShort();
            proximo = arquivo.readLong();

            if (tamanho > tamanhoNecessario) {
                if (posicao == 4)
                    arquivo.seek(posicao);
                else
                    arquivo.seek(posicao + 3);
                arquivo.writeLong(proximo);
                return endereco;
            }
            posicao = endereco;
            endereco = proximo;
        }
        return -1;
    }

    public void close() throws Exception {
        arquivo.close();
    }

    // ===== Helpers for indexing and iteration =====
    public String getNomeArquivo() {
        return this.nomeArquivo;
    }

    public static final int CABECALHO_BYTES = TAM_CABECALHO;

    public long length() throws Exception {
        return arquivo.length();
    }

    public Registro readAt(long posicao) throws Exception {
        arquivo.seek(posicao);
        byte lapide = arquivo.readByte();
        short tamanho = arquivo.readShort();
        byte[] dados = new byte[tamanho];
        arquivo.read(dados);
        if (lapide != ' ')
            return null;
        T obj = construtor.newInstance();
        obj.fromByteArray(dados);
        return obj;
    }


    public long firstRecordPosition() {
        return TAM_CABECALHO;
    }

    // Percorre todos os registros válidos, chamando o visitor com (offset, objeto)
    public void scanValidRecords(java.util.function.BiConsumer<Long, T> visitor) throws Exception {
        arquivo.seek(TAM_CABECALHO);
        while (arquivo.getFilePointer() < arquivo.length()) {
            long pos = arquivo.getFilePointer();
            byte lapide = arquivo.readByte();
            short tamanho = arquivo.readShort();
            byte[] dados = new byte[tamanho];
            arquivo.read(dados);
            if (lapide == ' ') {
                T obj = construtor.newInstance();
                obj.fromByteArray(dados);
                visitor.accept(pos, obj);
            }
        }
    }

    /// debug
    public void debugArquivo() throws Exception {
        System.out.println("\n=== DEBUG DO ARQUIVO ===");
        System.out.println("Tamanho do arquivo: " + arquivo.length() + " bytes");
        System.out.println("TAM_CABECALHO: " + TAM_CABECALHO);

        arquivo.seek(0);
        int ultimoID = arquivo.readInt();
        System.out.println("Último ID registrado: " + ultimoID);

        arquivo.seek(TAM_CABECALHO);
        int registroNum = 0;

        while (arquivo.getFilePointer() < arquivo.length()) {
            registroNum++;
            long posicao = arquivo.getFilePointer();

            byte lapide = arquivo.readByte();
            short tamanho = arquivo.readShort();
            byte[] dados = new byte[tamanho];
            arquivo.read(dados);

            System.out.println("\n--- Registro " + registroNum + " ---");
            System.out.println("Posição: " + posicao);
            System.out.println("Lápide (byte): " + lapide);
            System.out.println("Lápide (char): '" + (char) lapide + "'");
            System.out.println("Tamanho: " + tamanho);

            // Tenta ler o objeto
            try {
                T obj = construtor.newInstance();
                obj.fromByteArray(dados);
                System.out.println("ID do objeto: " + obj.getId());
                System.out.println("Objeto: " + obj);
            } catch (Exception e) {
                System.out.println("Erro ao ler objeto: " + e.getMessage());
            }
        }
        System.out.println("\n======================\n");
    }

}
