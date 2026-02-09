
package com.barraca.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class PedidoResumo {
    private final int id;
    private final LocalDate data;
    private final StatusPedido status;
    private final FormaPagamento pagamento;
    private final boolean recebido;
    private final BigDecimal total;
    private final int itens;

    public PedidoResumo(int id, LocalDate data, StatusPedido status, FormaPagamento pagamento, boolean recebido, BigDecimal total, int itens) {
        this.id = id;
        this.data = data;
        this.status = status;
        this.pagamento = pagamento;
        this.recebido = recebido;
        this.total = total;
        this.itens = itens;
    }

    public int getId() { return id; }
    public LocalDate getData() { return data; }
    public StatusPedido getStatus() { return status; }
    public FormaPagamento getPagamento() { return pagamento; }
    public boolean isRecebido() { return recebido; }
    public BigDecimal getTotal() { return total; }
    public int getItens() { return itens; }
}
