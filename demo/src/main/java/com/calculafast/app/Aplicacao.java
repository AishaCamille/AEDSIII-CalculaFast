package com.calculafast.app;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.calculafast.dao.ComandaDAO;
import com.calculafast.dao.ItemDAO;
import com.calculafast.dao.PagamentoDAO;
import com.calculafast.dao.PessoaComandaDAO;
import com.calculafast.dao.PessoaDAO;
import com.calculafast.dao.Pessoa_Comanda_ItemDAO;
import com.calculafast.model.Comanda;
import com.calculafast.model.Item;
import com.calculafast.model.Pagamento;
import com.calculafast.model.Pessoa;
import com.calculafast.model.PessoaComanda;
import com.calculafast.model.Pessoa_Comanda_Item;
import com.google.gson.Gson;

import static spark.Spark.before;
import static spark.Spark.delete;
import static spark.Spark.get;
import static spark.Spark.options;
import static spark.Spark.port;
import static spark.Spark.post;
import static spark.Spark.put;

public class Aplicacao {
    public static void main(String[] args) throws Exception {

        port(4567);


  options("/*", (request, response) -> {
            String accessControlRequestHeaders = request.headers("Access-Control-Request-Headers");
            if (accessControlRequestHeaders != null) {
                response.header("Access-Control-Allow-Headers", accessControlRequestHeaders);
            }

            String accessControlRequestMethod = request.headers("Access-Control-Request-Method");
            if (accessControlRequestMethod != null) {
                response.header("Access-Control-Allow-Methods", accessControlRequestMethod);
            }

            return "OK";
        });

        before((request, response) -> {
            response.header("Access-Control-Allow-Origin", "*");
            response.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            response.header("Access-Control-Allow-Headers", "Content-Type");
        });

        Gson gson = new Gson();

        PessoaDAO pessoaDAO = new PessoaDAO();
        ComandaDAO comandaDAO = new ComandaDAO();
        ItemDAO itemDAO = new ItemDAO();
        PagamentoDAO pagamentoDAO = new PagamentoDAO();
        Pessoa_Comanda_ItemDAO pciDAO = new Pessoa_Comanda_ItemDAO();
        PessoaComandaDAO pessoaComandaDAO = new PessoaComandaDAO();

        pciDAO.setComandaDAO(comandaDAO);
    pciDAO.setPessoaComandaDAO(pessoaComandaDAO);
    pciDAO.setItemDAO(itemDAO);

    pciDAO.setComandaDAO(comandaDAO);
    pciDAO.setPessoaComandaDAO(pessoaComandaDAO);
    pciDAO.setItemDAO(itemDAO);

/////////////pessoa
        //listar
        get("/pessoas", (req, res) -> {
            res.type("application/json");
            return gson.toJson(pessoaDAO.listarPessoas());
        });
        //criar
        post("/pessoas", (req, res) -> {
    res.type("application/json");
    try {
        Pessoa p = gson.fromJson(req.body(), Pessoa.class);
        
        String senhaTemp = p.getSenha(); 
        if (senhaTemp != null && !senhaTemp.isEmpty()) {
            p.setSenha(senhaTemp); 
        }
        
        boolean criado = pessoaDAO.incluirPessoa(p);
        
        if (criado) {
            res.status(201);
            return gson.toJson(Map.of("mensagem", "Pessoa criada com sucesso!"));
        } else {
            res.status(400);
            return gson.toJson(Map.of("erro", "Erro ao criar pessoa"));
        }
    } catch (Exception e) {
        res.status(500);
        return gson.toJson(Map.of("erro", "Erro: " + e.getMessage()));
    }
});
        //atualizar
        put("/pessoas/:id", (req, res) -> {
            int id = Integer.parseInt(req.params("id"));
            Pessoa p = gson.fromJson(req.body(), Pessoa.class);
            p.setId(id);

            boolean ok = pessoaDAO.alterarPessoa(p);

            if(ok) return "Pessoa atualizada!";
            return "Erro ao atualizar!";
        });
        //excluir
        delete("/pessoas/:id", (req, res) -> {
            int id = Integer.parseInt(req.params("id"));
            boolean ok = pessoaDAO.excluirPessoa(id);

            if(ok) return "Pessoa excluída!";
            return "Erro ao excluir!";
        });

        ///////////////comanda
        //criar
       post("/comandas", (req, res) -> {
    res.type("application/json"); // 
    Comanda c = gson.fromJson(req.body(), Comanda.class);
    comandaDAO.incluirComanda(c);
    
    return gson.toJson(c); 
});
         //atualizar
        put("/comandas/:id", (req, res) -> {
            int id = Integer.parseInt(req.params("id"));
            Comanda c = gson.fromJson(req.body(), Comanda.class);
            c.setId(id);

            boolean ok = comandaDAO.alterarComanda(c);

            if(ok) return "Comanda atualizada!";
            return "Erro ao atualizar!";
        });
         //excluir
        delete("/comandas/:id", (req, res) -> {
            int id = Integer.parseInt(req.params("id"));
            boolean ok = comandaDAO.excluirComanda(id);

            if(ok) return "Comanda excluída!";
            return "Erro ao excluir!";
        });

        //objetos pessoacomanda
       get("/comandas/:id/pessoas-detalhadas", (req, res) -> {
    res.type("application/json");
    try {
        int idComanda = Integer.parseInt(req.params("id"));
        
        // Verificação de segurança
        if (comandaDAO == null) throw new Exception("comandaDAO está nulo!");
        
        // Pega os IDs
        List<Integer> idsPessoas = comandaDAO.getPessoasComandasDaComanda(idComanda);
        
        // Converte IDs em Objetos
        List<PessoaComanda> pessoasDetalhadas = new ArrayList<>();
        for (Integer idPessoa : idsPessoas) {
            PessoaComanda p = pessoaComandaDAO.buscar(idPessoa);
            if (p != null) {
                pessoasDetalhadas.add(p);
            }
        }
        
        return gson.toJson(pessoasDetalhadas);

    } catch (Exception e) {
        e.printStackTrace(); 
        
        res.status(500);
        return gson.toJson(Map.of("erro", "Erro no Java: " + e.getMessage()));
    }
});
        
        ///////////////item
        //criar
       post("/itens", (req, res) -> {
            res.type("application/json");
            try {
                Item i = gson.fromJson(req.body(), Item.class);
                itemDAO.incluirItem(i); 
                res.status(201);
                return gson.toJson(i);
            } catch (Exception e) {
                res.status(400);
                return gson.toJson(Map.of("erro", "Erro ao criar: " + e.getMessage()));
            }
        });
         //atualizar
        put("/itens/:id", (req, res) -> {
            res.type("application/json");
            try {
                int id = Integer.parseInt(req.params("id"));
                Item i = gson.fromJson(req.body(), Item.class);
                i.setId(id);
                boolean ok = itemDAO.alterarItem(i);
                if (ok) return gson.toJson(Map.of("mensagem", "Item atualizado!"));
                
                res.status(404);
                return gson.toJson(Map.of("erro", "Item não encontrado para atualizar"));
            } catch (Exception e) {
                res.status(400);
                return gson.toJson(Map.of("erro", "Erro ao atualizar: " + e.getMessage()));
            }
        });
         //excluir
        delete("/itens/:id", (req, res) -> {
            res.type("application/json");
            try {
                int id = Integer.parseInt(req.params("id"));
                boolean ok = itemDAO.excluirItem(id);
                if (ok) return gson.toJson(Map.of("mensagem", "Item excluído!"));
                
                res.status(404); // Ou 400 dependendo da regra de negócio (se falhou por ter dependências)
                return gson.toJson(Map.of("erro", "Não foi possível excluir (Verifique dependências ou ID)"));
            } catch (Exception e) {
                res.status(400);
                return gson.toJson(Map.of("erro", "Erro: " + e.getMessage()));
            }
        });
//lista todos os itens
        get("/itens", (req, res) -> {
    res.type("application/json");
    return gson.toJson(itemDAO.listarItens()); 
});
// Buscar Item Único por ID (Método buscarItem)
        get("/itens/:id", (req, res) -> {
            res.type("application/json");
            try {
                int id = Integer.parseInt(req.params("id"));
                Item item = itemDAO.buscarItem(id);
                
                if (item != null) {
                    return gson.toJson(item);
                } else {
                    res.status(404);
                    return gson.toJson(Map.of("erro", "Item não encontrado"));
                }
            } catch (Exception e) {
                res.status(500);
                return gson.toJson(Map.of("erro", "Erro ao buscar: " + e.getMessage()));
            }
        });
        // Listar IDs das PessoasComanda que compraram este item (Método getPessoasQueCompraramItem)
        get("/itens/:id/consumidores", (req, res) -> {
            res.type("application/json");
            try {
                int id = Integer.parseInt(req.params("id"));
                List<Integer> ids = itemDAO.getPessoasQueCompraramItem(id);
                return gson.toJson(ids);
            } catch (Exception e) {
                res.status(500);
                return gson.toJson(Map.of("erro", "Erro ao buscar consumidores: " + e.getMessage()));
            }
        });
//  Listar IDs das Comandas onde este item aparece (Método getComandasDoItem)
        get("/itens/:id/comandas", (req, res) -> {
            res.type("application/json");
            try {
                int id = Integer.parseInt(req.params("id"));
                List<Integer> ids = itemDAO.getComandasDoItem(id);
                return gson.toJson(ids);
            } catch (Exception e) {
                res.status(500);
                return gson.toJson(Map.of("erro", "Erro ao buscar comandas: " + e.getMessage()));
            }
        });

         ///////////////pagamento
        //criar
        post("/pagamentos", (req, res) -> {
            Pagamento p = gson.fromJson(req.body(), Pagamento.class);
            pagamentoDAO.incluirPagamento(p);
            return "Pagamento criada!";
        });
         //atualizar
        put("/pagamentos/:id", (req, res) -> {
            int id = Integer.parseInt(req.params("id"));
            Pagamento p = gson.fromJson(req.body(), Pagamento.class);
            p.setId(id);

            boolean ok = pagamentoDAO.alterarPagamento(p);

            if(ok) return "Pagamento atualizada!";
            return "Erro ao atualizar!";
        });
         //excluir
        delete("/pagamentos/:id", (req, res) -> {
            int id = Integer.parseInt(req.params("id"));
            boolean ok = pagamentoDAO.excluirPagamento(id);

            if(ok) return "Pagamento excluída!";
            return "Erro ao excluir!";
        });

        // Criar (incluir) PESSOA COMANDA ITEM
post("/pessoa-comanda-item", (req, res) -> {
    res.type("application/json"); 
    try {
        Map<String, Object> body = gson.fromJson(req.body(), Map.class);
        
        // Converte os IDs
        int idPessoaComanda = ((Double) body.get("idPessoaComanda")).intValue();
        int idComanda = ((Double) body.get("idComanda")).intValue();
        int idItem = ((Double) body.get("idItem")).intValue();
        
        int qtd = body.containsKey("quantidade") ? ((Double) body.get("quantidade")).intValue() : 1;

        // 2. Verifica se JÁ EXISTE no banco
        Pessoa_Comanda_Item existente = pciDAO.buscarPorChaveComposta(idPessoaComanda, idComanda, idItem);

        if (existente != null) {
             res.status(200); 
            return gson.toJson(Map.of(
                "mensagem", "Item já estava na comanda desta pessoa. (Quantidade mantida)",
                "status", "existente"
            ));
        }

        //Cria novo
        Pessoa_Comanda_Item novo = new Pessoa_Comanda_Item();
        novo.setIdPessoaComanda(idPessoaComanda);
        novo.setIdComanda(idComanda);
        novo.setIdItem(idItem);
        novo.setQuantidade(qtd); 

        pciDAO.incluirPessoa_Comanda_Item(novo);
        
        res.status(201);
        return gson.toJson(Map.of(
            "mensagem", "Item adicionado com sucesso!",
            "status", "criado"
        ));

    } catch (Exception e) {
        e.printStackTrace();
        res.status(400);
        return gson.toJson(Map.of("erro", "Erro ao processar: " + e.getMessage()));
    }
});


// Buscar por chave composta
get("/pessoa-comanda-item/:idPessoa/:idComanda/:idItem", (req, res) -> {
    int idPessoa = Integer.parseInt(req.params("idPessoa"));
    int idComanda = Integer.parseInt(req.params("idComanda"));
    int idItem = Integer.parseInt(req.params("idItem"));
    
    Pessoa_Comanda_Item pci = pciDAO.buscarPorChaveComposta(idPessoa, idComanda, idItem);
    
    if (pci != null) {
        return gson.toJson(pci);
    }
    res.status(404);
    return "Registro não encontrado!";
});

// Atualizar
put("/pessoa-comanda-item/:idPessoa/:idComanda/:idItem", (req, res) -> {
    int idPessoa = Integer.parseInt(req.params("idPessoa"));
    int idComanda = Integer.parseInt(req.params("idComanda"));
    int idItem = Integer.parseInt(req.params("idItem"));
    
    Pessoa_Comanda_Item pci = gson.fromJson(req.body(), Pessoa_Comanda_Item.class);
    pci.setIdPessoaComanda(idPessoa);
    pci.setIdComanda(idComanda);
    pci.setIdItem(idItem);

    boolean ok = pciDAO.alterarPessoa_Comanda_Item(pci);

    if(ok) return "Pessoa_Comanda_Item atualizado!";
    return "Erro ao atualizar!";
});

// Excluir
delete("/pessoa-comanda-item/:idPessoa/:idComanda/:idItem", (req, res) -> {
    int idPessoa = Integer.parseInt(req.params("idPessoa"));
    int idComanda = Integer.parseInt(req.params("idComanda"));
    int idItem = Integer.parseInt(req.params("idItem"));
    
    boolean ok = pciDAO.excluirPessoa_Comanda_Item(idPessoa, idComanda, idItem);

    if(ok) return "Pessoa_Comanda_Item excluído!";
    return "Erro ao excluir!";
});

// Listar todos
get("/pessoa-comanda-item", (req, res) -> {
    List<Pessoa_Comanda_Item> todos = pciDAO.buscarTodos();
    return gson.toJson(todos);
});

// Buscar por pessoa
get("/pessoa-comanda-item/pessoa/:idPessoa", (req, res) -> {
    int idPessoa = Integer.parseInt(req.params("idPessoa"));
    List<Pessoa_Comanda_Item> resultados = pciDAO.buscarPorPessoaComanda(idPessoa);
    return gson.toJson(resultados);
});

// Buscar por comanda
get("/pessoa-comanda-item/comanda/:idComanda", (req, res) -> {
    int idComanda = Integer.parseInt(req.params("idComanda"));
    List<Pessoa_Comanda_Item> resultados = pciDAO.buscarPorComanda(idComanda);
    return gson.toJson(resultados);
});

// Buscar por item
get("/pessoa-comanda-item/item/:idItem", (req, res) -> {
    int idItem = Integer.parseInt(req.params("idItem"));
    List<Pessoa_Comanda_Item> resultados = pciDAO.buscarPorItem(idItem);
    return gson.toJson(resultados);
});


// Listar todas as PessoasComanda PESSOACO
get("/pessoas-comanda", (req, res) -> {
    res.type("application/json");
    try {
        List<PessoaComanda> lista = pessoaComandaDAO.listarPessoasComanda();
        return gson.toJson(lista);
    } catch (Exception e) {
        res.status(500);
        return gson.toJson(Map.of("erro", "Erro ao listar: " + e.getMessage()));
    }
});

// Buscar PessoaComanda por ID
get("/pessoas-comanda/:id", (req, res) -> {
    res.type("application/json");
    try {
        int id = Integer.parseInt(req.params("id"));
        PessoaComanda pc = pessoaComandaDAO.buscar(id);
        if (pc != null) {
            return gson.toJson(pc);
        } else {
            res.status(404);
            return gson.toJson(Map.of("erro", "PessoaComanda não encontrada"));
        }
    } catch (Exception e) {
        res.status(500);
        return gson.toJson(Map.of("erro", "Erro ao buscar: " + e.getMessage()));
    }
});

// Criar nova PessoaComanda
post("/pessoas-comanda", (req, res) -> {
    res.type("application/json");
    try {
        PessoaComanda pc = gson.fromJson(req.body(), PessoaComanda.class);
        int id = pessoaComandaDAO.incluir(pc);
        res.status(201);
        return gson.toJson(Map.of(
            "mensagem", "PessoaComanda criada com sucesso!",
            "id", id
        ));
    } catch (Exception e) {
        res.status(400);
        return gson.toJson(Map.of("erro", "Erro ao criar: " + e.getMessage()));
    }
});

// Atualizar PessoaComanda
put("/pessoas-comanda/:id", (req, res) -> {
    res.type("application/json");
    try {
        int id = Integer.parseInt(req.params("id"));
        PessoaComanda pc = gson.fromJson(req.body(), PessoaComanda.class);
        pc.setId(id);
        
        boolean ok = pessoaComandaDAO.atualizar(pc);
        if (ok) {
            return gson.toJson(Map.of("mensagem", "PessoaComanda atualizada com sucesso!"));
        } else {
            res.status(400);
            return gson.toJson(Map.of("erro", "Erro ao atualizar PessoaComanda"));
        }
    } catch (Exception e) {
        res.status(400);
        return gson.toJson(Map.of("erro", "Erro: " + e.getMessage()));
    }
});

// Excluir PessoaComanda
delete("/pessoas-comanda/:id", (req, res) -> {
    res.type("application/json");
    try {
        int id = Integer.parseInt(req.params("id"));
        boolean ok = pessoaComandaDAO.excluir(id);
        if (ok) {
            return gson.toJson(Map.of("mensagem", "PessoaComanda excluída com sucesso!"));
        } else {
            res.status(400);
            return gson.toJson(Map.of("erro", "Erro ao excluir PessoaComanda"));
        }
    } catch (Exception e) {
        res.status(400);
        return gson.toJson(Map.of("erro", "Erro: " + e.getMessage()));
    }
});

// Buscar PessoasComanda por Comanda
get("/pessoas-comanda/comanda/:idComanda", (req, res) -> {
    res.type("application/json");
    try {
        int idComanda = Integer.parseInt(req.params("idComanda"));
        List<PessoaComanda> resultados = pessoaComandaDAO.buscarPorComanda(idComanda);
        return gson.toJson(resultados);
    } catch (Exception e) {
        res.status(500);
        return gson.toJson(Map.of("erro", "Erro ao buscar: " + e.getMessage()));
    }
});

// Adicionar item à PessoaComanda
post("/pessoas-comanda/:idPessoaComanda/itens", (req, res) -> {
    res.type("application/json");
    try {
        int idPessoaComanda = Integer.parseInt(req.params("idPessoaComanda"));
        Map<String, Integer> dados = gson.fromJson(req.body(), Map.class);
        
        int idComanda = dados.get("idComanda");
        int idItem = dados.get("idItem");
        
        boolean ok = pessoaComandaDAO.adicionarItemAPessoaComanda(idPessoaComanda, idComanda, idItem);
        
        if (ok) {
            return gson.toJson(Map.of("mensagem", "Item adicionado à PessoaComanda com sucesso!"));
        } else {
            res.status(400);
            return gson.toJson(Map.of("erro", "Erro ao adicionar item"));
        }
    } catch (Exception e) {
        res.status(400);
        return gson.toJson(Map.of("erro", "Erro: " + e.getMessage()));
    }
});

// Remover item da PessoaComanda
delete("/pessoas-comanda/:idPessoaComanda/itens/:idItem/comanda/:idComanda", (req, res) -> {
    res.type("application/json");
    try {
        int idPessoaComanda = Integer.parseInt(req.params("idPessoaComanda"));
        int idItem = Integer.parseInt(req.params("idItem"));
        int idComanda = Integer.parseInt(req.params("idComanda"));
        
        boolean ok = pessoaComandaDAO.removerItemDaPessoaComanda(idPessoaComanda, idComanda, idItem);
        
        if (ok) {
            return gson.toJson(Map.of("mensagem", "Item removido da PessoaComanda com sucesso!"));
        } else {
            res.status(400);
            return gson.toJson(Map.of("erro", "Erro ao remover item"));
        }
    } catch (Exception e) {
        res.status(400);
        return gson.toJson(Map.of("erro", "Erro: " + e.getMessage()));
    }
});

// Listar itens de uma PessoaComanda
get("/pessoas-comanda/:idPessoaComanda/itens", (req, res) -> {
    res.type("application/json");
    try {
        int idPessoaComanda = Integer.parseInt(req.params("idPessoaComanda"));
        List<Integer> itens = pessoaComandaDAO.getItensCompradosPorPessoaComanda(idPessoaComanda);
        return gson.toJson(itens);
    } catch (Exception e) {
        res.status(500);
        return gson.toJson(Map.of("erro", "Erro ao listar itens: " + e.getMessage()));
    }
});

// Listar relações completas de uma PessoaComanda
get("/pessoas-comanda/:idPessoaComanda/relacoes", (req, res) -> {
    res.type("application/json");
    try {
        int idPessoaComanda = Integer.parseInt(req.params("idPessoaComanda"));
        List<Pessoa_Comanda_Item> relacoes = pessoaComandaDAO.getRelacoesDaPessoaComanda(idPessoaComanda);
        return gson.toJson(relacoes);
    } catch (Exception e) {
        res.status(500);
        return gson.toJson(Map.of("erro", "Erro ao listar relações: " + e.getMessage()));
    }
});

// Calcular consumo total de uma PessoaComanda
get("/pessoas-comanda/:idPessoaComanda/consumo-total", (req, res) -> {
    res.type("application/json");
    try {
        int idPessoaComanda = Integer.parseInt(req.params("idPessoaComanda"));
        double total = pessoaComandaDAO.calcularConsumoTotal(idPessoaComanda);
        return gson.toJson(Map.of("consumoTotal", total));
    } catch (Exception e) {
        res.status(500);
        return gson.toJson(Map.of("erro", "Erro ao calcular consumo: " + e.getMessage()));
    }
});

    }
}
