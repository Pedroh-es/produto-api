package com.trabalho.api.controllers;

import com.trabalho.api.entities.Produto;
import com.trabalho.api.logging.AppLogger;
import com.trabalho.api.services.ProdutoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller só recebe a requisição, chama o Service e devolve a resposta
 * com o status HTTP correto. Os casos 404 são tratados no GlobalExceptionHandler.
 */
@RestController
@RequestMapping("/produtos")
public class ProdutoController {

    private static final String ORIGEM = "ProdutoController";
    private final AppLogger log = AppLogger.getInstance();

    private final ProdutoService produtoService;

    public ProdutoController(ProdutoService produtoService) {
        this.produtoService = produtoService;
    }

    @PostMapping
    public ResponseEntity<Produto> criar(@RequestBody Produto produto) {
        log.debug(ORIGEM, "POST /produtos recebido");
        Produto criado = produtoService.criar(produto);
        log.info(ORIGEM, "POST /produtos -> 201 Created (id=" + criado.getId() + ")");
        return ResponseEntity.status(HttpStatus.CREATED).body(criado);
    }

    @GetMapping
    public ResponseEntity<List<Produto>> listar() {
        log.debug(ORIGEM, "GET /produtos recebido");
        List<Produto> produtos = produtoService.listar();
        log.info(ORIGEM, "GET /produtos -> 200 OK (" + produtos.size() + " itens)");
        return ResponseEntity.ok(produtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Produto> buscarPorId(@PathVariable Long id) {
        log.debug(ORIGEM, "GET /produtos/" + id + " recebido");
        Produto produto = produtoService.buscarPorId(id);
        log.info(ORIGEM, "GET /produtos/" + id + " -> 200 OK");
        return ResponseEntity.ok(produto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Produto> atualizar(@PathVariable Long id, @RequestBody Produto produto) {
        log.debug(ORIGEM, "PUT /produtos/" + id + " recebido");
        Produto atualizado = produtoService.atualizar(id, produto);
        log.info(ORIGEM, "PUT /produtos/" + id + " -> 200 OK");
        return ResponseEntity.ok(atualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        log.debug(ORIGEM, "DELETE /produtos/" + id + " recebido");
        produtoService.deletar(id);
        log.info(ORIGEM, "DELETE /produtos/" + id + " -> 204 No Content");
        return ResponseEntity.noContent().build();
    }
}
