
package com.barraca.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Pedido {
    private Integer id;
    private LocalDate dataPedido = LocalDate.now();
    private StatusPedido status = StatusPedido.ABERTO;
    private FormaPagamento formaPagamento; // null enquanto ABERTO
    private boolean pagamentoRecebido = false; // somente faz sentido quando FECHADO
    private final List<ItemPedido> itens = new ArrayList<>();

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public LocalDate getDataPedido() { return dataPedido; }
    public void setDataPedido(LocalDate dataPedido) { this.dataPedido = dataPedido; }

    public StatusPedido getStatus() { return status; }
    public void setStatus(StatusPedido status) { this.status = status; }

    public FormaPagamento getFormaPagamento() { return formaPagamento; }
    public void setFormaPagamento(FormaPagamento formaPagamento) { this.formaPagamento = formaPagamento; }

    public boolean isPagamentoRecebido() { return pagamentoRecebido; }
    public void setPagamentoRecebido(boolean pagamentoRecebido) { this.pagamentoRecebido = pagamentoRecebido; }

    public List<ItemPedido> getItens() { return itens; }

    public BigDecimal getTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (ItemPedido it : itens) total = total.add(it.getSubtotal());
        return total;
    }
}
