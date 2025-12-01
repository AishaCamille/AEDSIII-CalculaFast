package com.calculafast.app;
import com.calculafast.dao.PessoaDAO;
import com.calculafast.model.Pessoa;

import com.calculafast.dao.ComandaDAO;
import com.calculafast.model.Comanda;

import com.calculafast.dao.ItemDAO;
import com.calculafast.model.Item;

import com.calculafast.dao.PagamentoDAO;
import com.calculafast.model.Pagamento;

import com.calculafast.dao.Pessoa_Comanda_ItemDAO;
import com.calculafast.model.Pessoa_Comanda_Item;

import com.calculafast.dao.PessoaComandaDAO;
import com.calculafast.model.PessoaComanda;

import com.google.gson.Gson;

import static spark.Spark.*;

import java.util.List;
import java.util.Map;

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
            Comanda c = gson.fromJson(req.body(), Comanda.class);
            comandaDAO.incluirComanda(c);
            return "Comanda criada!";
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
        
        ///////////////item
        //criar
        post("/itens", (req, res) -> {
            Item i = gson.fromJson(req.body(), Item.class);
            itemDAO.incluirItem(i);
            return "Item criada!";
        });
         //atualizar
        put("/itens/:id", (req, res) -> {
            int id = Integer.parseInt(req.params("id"));
            Item i = gson.fromJson(req.body(), Item.class);
            i.setId(id);

            boolean ok = itemDAO.alterarItem(i);

            if(ok) return "Item atualizada!";
            return "Erro ao atualizar!";
        });
         //excluir
        delete("/itens/:id", (req, res) -> {
            int id = Integer.parseInt(req.params("id"));
            boolean ok = itemDAO.excluirItem(id);

            if(ok) return "Item excluída!";
            return "Erro ao excluir!";
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
        // Criar (incluir)
post("/pessoa-comanda-item", (req, res) -> {
    Pessoa_Comanda_Item pci = gson.fromJson(req.body(), Pessoa_Comanda_Item.class);
    pciDAO.incluirPessoa_Comanda_Item(pci);
    return "Pessoa_Comanda_Item criado!";
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

    }
}
