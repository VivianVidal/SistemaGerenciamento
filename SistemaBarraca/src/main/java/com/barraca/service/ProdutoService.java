
package com.barraca.service;

import com.barraca.dao.ProdutoDAO;
import com.barraca.model.Produto;

import java.math.BigDecimal;
import java.util.List;

public class ProdutoService {
    private final ProdutoDAO dao = new ProdutoDAO();

    public List<Produto> listar(boolean incluirInativos) {
        return dao.listar(incluirInativos);
    }

    public Produto salvar(Produto p) {
        validar(p);
        if (p.getId() == null) return dao.inserir(p);
        dao.atualizar(p);
        return p;
    }

    public void setAtivo(int id, boolean ativo) {
        dao.setAtivo(id, ativo);
    }

    private void validar(Produto p) {
        if (p.getNome() == null || p.getNome().trim().isEmpty())
            throw new IllegalArgumentException("Informe o nome do produto.");
        if (p.getPreco() == null || p.getPreco().compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Informe um preço válido.");
    }
}
