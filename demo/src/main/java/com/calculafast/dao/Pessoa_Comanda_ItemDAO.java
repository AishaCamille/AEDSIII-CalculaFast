package com.calculafast.dao;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.calculafast.index.hash.IndiceChaveComposta;
import com.calculafast.index.hash.HashExtensivel;
import com.calculafast.index.hash.ParIdOffset;
import com.calculafast.model.Arquivo;
import com.calculafast.model.Comanda;
import com.calculafast.model.Item;
import com.calculafast.model.PessoaComanda;
import com.calculafast.model.Pessoa_Comanda_Item;
import com.calculafast.model.Registro;

public class Pessoa_Comanda_ItemDAO {

    private Arquivo<Pessoa_Comanda_Item> arqPessoa_Comanda_Item;
    private IndiceChaveComposta indiceChave;
    
    // Índices hash extensível para as chaves estrangeiras
    private HashExtensivel<ParIdOffset> idxPessoaComanda;
    private HashExtensivel<ParIdOffset> idxComanda;
    private HashExtensivel<ParIdOffset> idxItem;
    
    private PessoaComandaDAO pessoaComandaDAO;
    private ComandaDAO comandaDAO;
    private ItemDAO itemDAO;
    
    public void setComandaDAO(ComandaDAO comandaDAO) {
        this.comandaDAO = comandaDAO;
    }

    public Pessoa_Comanda_ItemDAO() throws Exception {
        File pastaIndices = new File("./indices");
        if (!pastaIndices.exists()) {
            pastaIndices.mkdirs();
        }
        
        // Cria pasta específica para os índices desta entidade
        File pastaPCI = new File("./dados/pessoa_comanda_item");
        if (!pastaPCI.exists()) {
            pastaPCI.mkdirs();
        }
        
        arqPessoa_Comanda_Item = new Arquivo<>("Pessoa_Comanda_Item", Pessoa_Comanda_Item.class.getConstructor());
        indiceChave = new IndiceChaveComposta("dados/indice_chave_composta");
        
        // Inicializa os índices hash extensível para cada chave estrangeira
        this.idxPessoaComanda = new HashExtensivel<>(
            ParIdOffset.class.getConstructor(),
            10,
            "./dados/pessoa_comanda_item/idx_pessoa_comanda_d.db",
            "./dados/pessoa_comanda_item/idx_pessoa_comanda_b.db"
        );
        
        this.idxComanda = new HashExtensivel<>(
            ParIdOffset.class.getConstructor(),
            10,
            "./dados/pessoa_comanda_item/idx_comanda_d.db",
            "./dados/pessoa_comanda_item/idx_comanda_b.db"
        );
        
        this.idxItem = new HashExtensivel<>(
            ParIdOffset.class.getConstructor(),
            10,
            "./dados/pessoa_comanda_item/idx_item_d.db",
            "./dados/pessoa_comanda_item/idx_item_b.db"
        );
        
        // Reconstrói os índices se necessário
        reconstruirIndices();
    }
    
    /**
     * Reconstrói todos os índices varrendo o arquivo
     */
    private void reconstruirIndices() throws Exception {
        System.out.println("Reconstruindo índices de Pessoa_Comanda_Item...");
        
        arqPessoa_Comanda_Item.scanValidRecords((pos, obj) -> {
            try {
                Pessoa_Comanda_Item pci = (Pessoa_Comanda_Item) obj;
                
                // Atualiza índice da chave composta
                indiceChave.inserir(
                    pci.getIdPessoaComanda(), 
                    pci.getIdComanda(), 
                    pci.getIdItem(), 
                    pos
                );
                
                // Atualiza índices das chaves estrangeiras
                try {
                    idxPessoaComanda.create(new ParIdOffset(pci.getIdPessoaComanda(), pos));
                } catch (Exception e) {
                    idxPessoaComanda.update(new ParIdOffset(pci.getIdPessoaComanda(), pos));
                }
                
                try {
                    idxComanda.create(new ParIdOffset(pci.getIdComanda(), pos));
                } catch (Exception e) {
                    idxComanda.update(new ParIdOffset(pci.getIdComanda(), pos));
                }
                
                try {
                    idxItem.create(new ParIdOffset(pci.getIdItem(), pos));
                } catch (Exception e) {
                    idxItem.update(new ParIdOffset(pci.getIdItem(), pos));
                }
                
            } catch (Exception e) {
               // System.err.println("Erro ao reconstruir índice: " + e.getMessage());
            }
        });
        
        System.out.println("Índices reconstruídos com sucesso!");
    }
    
    private PessoaComandaDAO getPessoaComandaDAO() throws Exception {
        if (pessoaComandaDAO == null) {
            pessoaComandaDAO = new PessoaComandaDAO();
        }
        return pessoaComandaDAO;
    }
    
    private ComandaDAO getComandaDAO() throws Exception {
        if (comandaDAO == null) {
            comandaDAO = new ComandaDAO();
        }
        return comandaDAO;
    }
    
    private ItemDAO getItemDAO() throws Exception {
        if (itemDAO == null) {
            itemDAO = new ItemDAO();
        }
        return itemDAO;
    }

    public boolean incluirPessoa_Comanda_Item(Pessoa_Comanda_Item pci) throws Exception {
        boolean sucesso = false;
        getPessoaComandaDAO();
        getComandaDAO();
        getItemDAO();
        
        if (pessoaComandaDAO == null) {
            throw new Exception("PessoaComandaDAO não foi inicializado.");
        }
        if (comandaDAO == null) {
            throw new Exception("ComandaDAO não foi inicializado.");
        }
        if (itemDAO == null) {
            throw new Exception("ItemDAO não foi inicializado.");
        }
        
        // Verificação de integridade
        if (getPessoaComandaDAO().buscar(pci.getIdPessoaComanda()) == null) {
            throw new Exception("Erro de Integridade: PessoaComanda com ID " + pci.getIdPessoaComanda() + " não existe.");
        }
        
        if (getComandaDAO().buscarComanda(pci.getIdComanda()) == null) {
            throw new Exception("Erro de Integridade: Comanda com ID " + pci.getIdComanda() + " não existe.");
        }
        
        if (getItemDAO().buscarItem(pci.getIdItem()) == null) {
            throw new Exception("Erro de Integridade: Item com ID " + pci.getIdItem() + " não existe.");
        }
        
        // Verifica se já existe
        Pessoa_Comanda_Item existente = buscarPorChaveComposta(
            pci.getIdPessoaComanda(), 
            pci.getIdComanda(), 
            pci.getIdItem()
        );
        
        if (existente != null) {
            throw new Exception("Já existe registro com esta chave composta!");
        }

        long offset = arqPessoa_Comanda_Item.createWithOffset(pci);
        
        if (offset < 0) {
            int idHash = pci.getChaveComposta();
            Pessoa_Comanda_Item registroArquivo = arqPessoa_Comanda_Item.read(idHash);
            
            if (registroArquivo != null && 
                registroArquivo.getIdPessoaComanda() == pci.getIdPessoaComanda() && 
                registroArquivo.getIdComanda() == pci.getIdComanda() && 
                registroArquivo.getIdItem() == pci.getIdItem()) {
                
                offset = encontrarOffsetNoArquivo(registroArquivo);
                
                if (offset >= 0) {
                    indiceChave.inserir(
                        registroArquivo.getIdPessoaComanda(), 
                        registroArquivo.getIdComanda(), 
                        registroArquivo.getIdItem(), 
                        offset
                    );
                    return true;
                } else {
                    throw new Exception("Registro existe no arquivo mas não foi possível encontrar o offset.");
                }
            } else {
                throw new Exception("Erro ao criar registro. ID (hash) já existe para outro registro ou registro não encontrado.");
            }
        }
        
        // Insere na chave composta
        indiceChave.inserir(pci.getIdPessoaComanda(), pci.getIdComanda(), pci.getIdItem(), offset);
        
        // Insere nos índices de chaves estrangeiras
        try {
            idxPessoaComanda.create(new ParIdOffset(pci.getIdPessoaComanda(), offset));
        } catch (Exception e) {
            // Se já existe, apenas atualiza
            idxPessoaComanda.update(new ParIdOffset(pci.getIdPessoaComanda(), offset));
        }
        
        try {
            idxComanda.create(new ParIdOffset(pci.getIdComanda(), offset));
        } catch (Exception e) {
            idxComanda.update(new ParIdOffset(pci.getIdComanda(), offset));
        }
        
        try {
            idxItem.create(new ParIdOffset(pci.getIdItem(), offset));
        } catch (Exception e) {
            idxItem.update(new ParIdOffset(pci.getIdItem(), offset));
        }
        
        System.out.println("Inserção concluída com sucesso.");
        sucesso = true;
        
        if (sucesso) {
            try {
                // Chama diretamente o método do ComandaDAO
                getComandaDAO().adicionarPessoaComandaAComanda(
                    pci.getIdComanda(), 
                    pci.getIdPessoaComanda()
                );
            } catch (Exception e) {
                System.out.println("Aviso leve: Erro ao sincronizar com ComandaDAO (Pode ser ignorado se a pessoa já estiver na lista): " + e.getMessage());
            }
        }
        
        return true;
    }

    public void setPessoaComandaDAO(PessoaComandaDAO pessoaComandaDAO) {
        this.pessoaComandaDAO = pessoaComandaDAO;
    }
    
    public void setItemDAO(ItemDAO itemDAO) {
        this.itemDAO = itemDAO;
    }

    public Pessoa_Comanda_Item buscarPorChaveComposta(int idPessoaComanda, int idComanda, int idItem) throws Exception {
        var chave = indiceChave.buscar(idPessoaComanda, idComanda, idItem);
        
        if (chave != null) {
            Pessoa_Comanda_Item resultado = buscarPorOffset(chave.getOffset());
            return resultado;
        }
        return null;
    }

    public boolean alterarPessoa_Comanda_Item(Pessoa_Comanda_Item novo) throws Exception {
        Pessoa_Comanda_Item antigo = buscarPorChaveComposta(
            novo.getIdPessoaComanda(), 
            novo.getIdComanda(), 
            novo.getIdItem()
        );
        if (antigo == null) return false;

        return arqPessoa_Comanda_Item.update(novo);
    }

    public boolean excluirPessoa_Comanda_Item(int idPessoaComanda, int idComanda, int idItem) throws Exception {
        Pessoa_Comanda_Item registro = buscarPorChaveComposta(idPessoaComanda, idComanda, idItem);
        if (registro == null) return false;

        boolean sucesso = arqPessoa_Comanda_Item.delete(calcularId(registro));
        if (sucesso) {
            indiceChave.remover(idPessoaComanda, idComanda, idItem);
            
            // Remove dos índices de chaves estrangeiras
            idxPessoaComanda.delete(idPessoaComanda);
            idxComanda.delete(idComanda);
            idxItem.delete(idItem);
        }
        return sucesso;
    }

    /**
     * Busca usando o índice hash de PessoaComanda
     */
    public List<Pessoa_Comanda_Item> buscarPorPessoaComanda(int idPessoaComanda) throws Exception {
        List<Pessoa_Comanda_Item> resultados = new ArrayList<>();
        
        // Tenta usar o índice primeiro (mais eficiente)
        try {
            ParIdOffset ref = idxPessoaComanda.read(idPessoaComanda);
            if (ref != null) {
                Pessoa_Comanda_Item pci = buscarPorOffset(ref.getOffset());
                if (pci != null && pci.getIdPessoaComanda() == idPessoaComanda) {
                    resultados.add(pci);
                }
            }
        } catch (Exception e) {
            System.out.println("Índice não encontrou, fazendo busca sequencial...");
        }
        
        // Busca sequencial como fallback ou complemento
        arqPessoa_Comanda_Item.scanValidRecords((Long pos, Pessoa_Comanda_Item registro) -> {
            if (registro.getIdPessoaComanda() == idPessoaComanda) {
                boolean jaExiste = false;
                for (Pessoa_Comanda_Item r : resultados) {
                    if (r.getIdComanda() == registro.getIdComanda() && 
                        r.getIdItem() == registro.getIdItem()) {
                        jaExiste = true;
                        break;
                    }
                }
                if (!jaExiste) {
                    resultados.add(registro);
                }
            }
        });
        
        return resultados;
    }

    /**
     * Busca usando o índice hash de Item
     */
    public List<Pessoa_Comanda_Item> buscarPorItem(int idItem) throws Exception {
        List<Pessoa_Comanda_Item> resultados = new ArrayList<>();
        
        arqPessoa_Comanda_Item.scanValidRecords((Long pos, Pessoa_Comanda_Item registro) -> {
            if (registro.getIdItem() == idItem) {
                resultados.add(registro);
            }
        });
        return resultados;
    }

    /**
     * Busca usando o índice hash de Comanda
     */
    public List<Pessoa_Comanda_Item> buscarPorComanda(int idComanda) throws Exception {
        List<Pessoa_Comanda_Item> resultados = new ArrayList<>();
        
        arqPessoa_Comanda_Item.scanValidRecords((Long pos, Pessoa_Comanda_Item registro) -> {
            if (registro.getIdComanda() == idComanda) {
                resultados.add(registro);
            }
        });
        return resultados;
    }

    public List<Integer> buscarItensUnicosPorPessoaComanda(int idPessoaComanda) throws Exception {
        List<Integer> itensUnicos = new ArrayList<>();
        List<Pessoa_Comanda_Item> relacoes = buscarPorPessoaComanda(idPessoaComanda);
        
        for (Pessoa_Comanda_Item relacao : relacoes) {
            if (!itensUnicos.contains(relacao.getIdItem())) {
                itensUnicos.add(relacao.getIdItem());
            }
        }
        return itensUnicos;
    }

    public List<Pessoa_Comanda_Item> buscarTodos() throws Exception {
        List<Pessoa_Comanda_Item> todos = new ArrayList<>();
        arqPessoa_Comanda_Item.scanValidRecords((Long pos, Pessoa_Comanda_Item registro) -> {
            todos.add(registro);
        });
        return todos;
    }

    private long calcularOffset(int id) {
        int tamanhoRegistro = new Pessoa_Comanda_Item().size();
        int tamanhoCabecalho = 12; 
        return tamanhoCabecalho + (id - 1) * (1 + 2 + tamanhoRegistro);
    }
    
    private int calcularId(Pessoa_Comanda_Item pci) {
        return pci.getChaveComposta();
    }
    
    private long encontrarOffsetNoArquivo(Pessoa_Comanda_Item pci) throws Exception {
        final long[] offsetEncontrado = {-1};
        arqPessoa_Comanda_Item.scanValidRecords((Long pos, Pessoa_Comanda_Item registro) -> {
            if (registro.getIdPessoaComanda() == pci.getIdPessoaComanda() && 
                registro.getIdComanda() == pci.getIdComanda() && 
                registro.getIdItem() == pci.getIdItem()) {
                offsetEncontrado[0] = pos;
            }
        });
        return offsetEncontrado[0];
    }

    private Pessoa_Comanda_Item buscarPorOffset(long offset) throws Exception {
        try {
            Registro registro = arqPessoa_Comanda_Item.readAt(offset);
            if (registro instanceof Pessoa_Comanda_Item) {
                return (Pessoa_Comanda_Item) registro;
            }
            return null;
        } catch (Exception e) {
            System.out.println("Erro no buscarPorOffset: " + e.getMessage());
            return buscarPorOffsetSequencial(offset);
        }
    }

    private Pessoa_Comanda_Item buscarPorOffsetSequencial(long offsetAlvo) throws Exception {
        int tentativas = 0;
        while (tentativas < 10000) { 
            try {
                tentativas++;
                Pessoa_Comanda_Item registro = arqPessoa_Comanda_Item.read(tentativas);
                if (registro != null) {
                    long offsetCalculado = calcularOffset(tentativas);
                    if (offsetCalculado == offsetAlvo) {
                        return registro;
                    }
                }
            } catch (Exception e) {
                break; 
            }
        }
        return null;
    }

    public void fechar() throws Exception {
        indiceChave.fechar();
        arqPessoa_Comanda_Item.close();
        
        // Fecha os DAOs se eles foram criados por este objeto
        if (pessoaComandaDAO != null) {
            pessoaComandaDAO.fechar();
        }
        if (comandaDAO != null) {
            comandaDAO.fechar();
        }
        if (itemDAO != null) {
            itemDAO.fechar();
        }
    }
}