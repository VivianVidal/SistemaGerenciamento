package com.barraca.service;

import com.barraca.dao.ItemPedidoDAO;
import com.barraca.dao.PedidoDAO;
import com.barraca.model.*;

import java.time.LocalDate;
import java.util.List;

public class PedidoService {
    private final PedidoDAO pedidoDAO = new PedidoDAO();
    private final ItemPedidoDAO itemDAO = new ItemPedidoDAO();

    public Pedido criarNovo() {
        Pedido p = new Pedido();
        p.setStatus(StatusPedido.ABERTO);
        p.setFormaPagamento(null);
        return p;
    }

    public Pedido salvarPedido(Pedido p) {
        if (p.getId() == null) {
            // melhoria: salva pedido e itens na mesma transação
            pedidoDAO.inserirComItens(p);
        }
        return p;
    }

    public void fecharPedido(Pedido p, FormaPagamento fp) {
        if (p.getItens().isEmpty())
            throw new IllegalStateException("Adicione ao menos 1 item antes de fechar o pedido.");
        if (fp == null)
            throw new IllegalArgumentException("Selecione a forma de pagamento.");

        p.setFormaPagamento(fp);
        p.setStatus(StatusPedido.FECHADO);

        if (p.getId() == null) {
            salvarPedido(p);
        } else {
            pedidoDAO.atualizarStatusPagamento(p);
        }
    }

    public List<PedidoResumo> listarResumo(LocalDate de, LocalDate ate) {
        return pedidoDAO.listarResumo(de, ate);
    }

    public List<ItemPedido> itensDoPedido(int pedidoId) {
        return itemDAO.listarPorPedido(pedidoId);
    }

    public void cancelarPedido(int pedidoId) {
        pedidoDAO.atualizarStatusPagamentoRecebido(pedidoId, StatusPedido.CANCELADO, null, false);
    }

    public void reabrirPedido(int pedidoId) {
        pedidoDAO.atualizarStatusPagamentoRecebido(pedidoId, StatusPedido.ABERTO, null, false);
    }

    public void atualizarPagamentoRecebido(int pedidoId, boolean recebido) {
        pedidoDAO.atualizarRecebido(pedidoId, recebido);
    }

}
