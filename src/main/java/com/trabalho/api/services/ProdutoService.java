package com.trabalho.api.services;

import com.trabalho.api.entities.Produto;
import com.trabalho.api.exceptions.RecursoNaoEncontradoException;
import com.trabalho.api.logging.AppLogger;
import com.trabalho.api.repositories.IJpaProdutoRepo;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProdutoService {

    private static final String ORIGEM = "ProdutoService";

    // Versão final: Singleton clássico (ver README, seção "Versão final escolhida")
    private final AppLogger log = AppLogger.getInstance();

    private final IJpaProdutoRepo produtoRepo;

    public ProdutoService(IJpaProdutoRepo produtoRepo) {
        this.produtoRepo = produtoRepo;
    }

    public Produto criar(Produto produto) {
        log.debug(ORIGEM, "Criando produto: nome=" + produto.getNome() + ", preco=" + produto.getPreco());
        produto.setId(null);
        Produto salvo = produtoRepo.save(produto);
        log.info(ORIGEM, "Produto criado com id=" + salvo.getId());
        return salvo;
    }

    public List<Produto> listar() {
        List<Produto> produtos = produtoRepo.findAll();
        log.debug(ORIGEM, "Consulta findAll retornou " + produtos.size() + " registro(s)");
        log.info(ORIGEM, "Listagem de produtos concluída");
        return produtos;
    }

    public Produto buscarPorId(Long id) {
        log.debug(ORIGEM, "Buscando produto id=" + id);
        Produto produto = produtoRepo.findById(id).orElseThrow(() -> naoEncontrado(id));
        log.info(ORIGEM, "Produto id=" + id + " encontrado");
        return produto;
    }

    public Produto atualizar(Long id, Produto dados) {
        log.debug(ORIGEM, "Atualizando produto id=" + id + " -> nome=" + dados.getNome()
                + ", preco=" + dados.getPreco() + ", estoque=" + dados.getEstoque());
        Produto existente = produtoRepo.findById(id).orElseThrow(() -> naoEncontrado(id));
        existente.setNome(dados.getNome());
        existente.setDescricao(dados.getDescricao());
        existente.setPreco(dados.getPreco());
        existente.setEstoque(dados.getEstoque());
        Produto salvo = produtoRepo.save(existente);
        log.info(ORIGEM, "Produto id=" + id + " atualizado");
        return salvo;
    }

    public void deletar(Long id) {
        log.debug(ORIGEM, "Removendo produto id=" + id);
        if (!produtoRepo.existsById(id)) {
            throw naoEncontrado(id);
        }
        produtoRepo.deleteById(id);
        log.info(ORIGEM, "Produto id=" + id + " removido");
    }

    private RecursoNaoEncontradoException naoEncontrado(Long id) {
        log.erro(ORIGEM, "Produto id=" + id + " não encontrado");
        return new RecursoNaoEncontradoException("Produto id=" + id + " não encontrado");
    }
}
