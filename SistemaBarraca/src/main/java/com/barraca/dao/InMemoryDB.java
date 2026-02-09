
package com.barraca.dao;

import com.barraca.model.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public final class InMemoryDB {
    private static final AtomicInteger produtoSeq = new AtomicInteger(1);
    private static final AtomicInteger pedidoSeq = new AtomicInteger(1000);
    private static final AtomicInteger itemSeq = new AtomicInteger(1);

    public static final List<Produto> produtos = new ArrayList<>();
    public static final List<Pedido> pedidos = new ArrayList<>();

    static {
        // Dados iniciais para facilitar a demonstração (modo simulação)
        produtos.add(new Produto(produtoSeq.getAndIncrement(), "Cachorro-Quente", "Pão + salsicha + molhos", new BigDecimal("12.00"), true));
        produtos.add(new Produto(produtoSeq.getAndIncrement(), "Refrigerante Lata", "350ml", new BigDecimal("6.00"), true));
        produtos.add(new Produto(produtoSeq.getAndIncrement(), "Batata Frita", "Porção média", new BigDecimal("10.00"), true));

        Pedido p = new Pedido();
        p.setId(pedidoSeq.getAndIncrement());
        p.setDataPedido(LocalDate.now());
        p.getItens().add(new ItemPedido(produtos.get(0), 2, produtos.get(0).getPreco()));
        p.getItens().add(new ItemPedido(produtos.get(1), 1, produtos.get(1).getPreco()));
        p.setFormaPagamento(FormaPagamento.PIX);
        p.setStatus(StatusPedido.FECHADO);
        pedidos.add(p);
    }

    private InMemoryDB() {}

    public static int nextProdutoId() { return produtoSeq.getAndIncrement(); }
    public static int nextPedidoId() { return pedidoSeq.getAndIncrement(); }
    public static int nextItemId() { return itemSeq.getAndIncrement(); }
}
