package com.barraca.dao;

import com.barraca.model.*;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PedidoDAO extends BaseDAO {

    /**
     * Insere pedido e itens dentro de UMA ÚNICA transação.
     * Isso evita salvar o pedido sem os itens caso dê erro no meio.
     */
    public Pedido inserirComItens(Pedido p) {
        if (simulacao()) {
            p.setId(InMemoryDB.nextPedidoId());
            InMemoryDB.pedidos.add(p);
            return p;
        }

        try (Connection c = Conexao.getConnection()) {
            boolean oldAuto = c.getAutoCommit();
            c.setAutoCommit(false);
            try {
                inserir(c, p);
                new ItemPedidoDAO().inserirItens(c, p);
                c.commit();
                return p;
            } catch (Exception ex) {
                try { c.rollback(); } catch (SQLException ignored) {}
                throw ex;
            } finally {
                try { c.setAutoCommit(oldAuto); } catch (SQLException ignored) {}
            }
        } catch (Exception e) {
            // comportamento atual do projeto: fallback para simulação
            AppConfigFallback.enableSimulacao();
            return inserirComItens(p);
        }
    }

    /**
     * Insere SOMENTE o pedido usando uma conexão existente.
     */
    private Pedido inserir(Connection c, Pedido p) throws SQLException {
        String sql = supportsPagamentoRecebido(c)
                ? "INSERT INTO pedido (data_pedido, status, forma_pagamento, pagamento_recebido) VALUES (?,?,?,?)"
                : "INSERT INTO pedido (data_pedido, status, forma_pagamento) VALUES (?,?,?)";
        try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setDate(1, Date.valueOf(p.getDataPedido()));
            ps.setString(2, p.getStatus().name());
            if (p.getFormaPagamento() == null) ps.setNull(3, Types.VARCHAR);
            else ps.setString(3, p.getFormaPagamento().name());
            if (supportsPagamentoRecebido(c)) {
                ps.setBoolean(4, p.isPagamentoRecebido());
            }
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) p.setId(keys.getInt(1));
            }
            return p;
        }
    }

    /**
     * Mantido por compatibilidade: insere pedido (sem itens) abrindo conexão própria.
     */
    public Pedido inserir(Pedido p) {
        if (simulacao()) {
            p.setId(InMemoryDB.nextPedidoId());
            InMemoryDB.pedidos.add(p);
            return p;
        }

        try (Connection c = Conexao.getConnection()) {
            inserir(c, p);
            return p;
        } catch (SQLException e) {
            AppConfigFallback.enableSimulacao();
            return inserir(p);
        }
    }

    public void atualizarStatusPagamento(Pedido p) {
        atualizarStatusPagamentoRecebido(p.getId(), p.getStatus(), p.getFormaPagamento(), p.isPagamentoRecebido());
    }

    public void atualizarStatusPagamentoRecebido(int pedidoId, StatusPedido status, FormaPagamento formaPagamento, boolean recebido) {
        if (simulacao()) {
            for (Pedido p : InMemoryDB.pedidos) {
                if (p.getId() != null && p.getId() == pedidoId) {
                    p.setStatus(status);
                    p.setFormaPagamento(formaPagamento);
                    p.setPagamentoRecebido(recebido);
                    break;
                }
            }
            return;
        }

        String sql = supportsPagamentoRecebido()
                ? "UPDATE pedido SET status=?, forma_pagamento=?, pagamento_recebido=? WHERE id=?"
                : "UPDATE pedido SET status=?, forma_pagamento=? WHERE id=?";
        try (Connection c = Conexao.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, status.name());
            if (formaPagamento == null) ps.setNull(2, Types.VARCHAR);
            else ps.setString(2, formaPagamento.name());
            if (supportsPagamentoRecebido(c)) {
                ps.setBoolean(3, recebido);
                ps.setInt(4, pedidoId);
            } else {
                ps.setInt(3, pedidoId);
            }
            ps.executeUpdate();
        } catch (SQLException e) {
            AppConfigFallback.enableSimulacao();
            atualizarStatusPagamentoRecebido(pedidoId, status, formaPagamento, recebido);
        }
    }

    public void atualizarRecebido(int pedidoId, boolean recebido) {
        if (simulacao()) {
            for (Pedido p : InMemoryDB.pedidos) {
                if (p.getId() != null && p.getId() == pedidoId) {
                    p.setPagamentoRecebido(recebido);
                    break;
                }
            }
            return;
        }
        if (!supportsPagamentoRecebido()) return;
        String sql = "UPDATE pedido SET pagamento_recebido=? WHERE id=?";
        try (Connection c = Conexao.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setBoolean(1, recebido);
            ps.setInt(2, pedidoId);
            ps.executeUpdate();
        } catch (SQLException e) {
            AppConfigFallback.enableSimulacao();
            atualizarRecebido(pedidoId, recebido);
        }
    }

    public List<PedidoResumo> listarResumo(LocalDate de, LocalDate ate) {
        if (simulacao()) {
            List<PedidoResumo> out = new ArrayList<>();
            for (Pedido p : InMemoryDB.pedidos) {
                boolean okDe = (de == null) || !p.getDataPedido().isBefore(de);
                boolean okAte = (ate == null) || !p.getDataPedido().isAfter(ate);
                if (okDe && okAte) {
                    out.add(new PedidoResumo(
                            p.getId(),
                            p.getDataPedido(),
                            p.getStatus(),
                            p.getFormaPagamento(),
                            p.isPagamentoRecebido(),
                            p.getTotal(),
                            p.getItens().size()
                    ));
                }
            }
            out.sort((a, b) -> Integer.compare(b.getId(), a.getId()));
            return out;
        }

        boolean supRecebido = supportsPagamentoRecebido();

        StringBuilder sb = new StringBuilder();
        sb.append("SELECT p.id, p.data_pedido, p.status, p.forma_pagamento");
        if (supRecebido) sb.append(", p.pagamento_recebido");
        sb.append(", ");
        sb.append("COALESCE(SUM(i.quantidade * i.preco_unitario), 0) AS total, ");
        sb.append("COALESCE(SUM(i.quantidade), 0) AS itens ");
        sb.append("FROM pedido p ");
        sb.append("LEFT JOIN item_pedido i ON i.pedido_id = p.id ");
        sb.append("WHERE 1=1 ");
        if (de != null) sb.append("AND p.data_pedido >= ? ");
        if (ate != null) sb.append("AND p.data_pedido <= ? ");
        sb.append("GROUP BY p.id, p.data_pedido, p.status, p.forma_pagamento");
        if (supRecebido) sb.append(", p.pagamento_recebido");
        sb.append(" ");
        sb.append("ORDER BY p.id DESC");

        List<PedidoResumo> list = new ArrayList<>();
        try (Connection c = Conexao.getConnection();
             PreparedStatement ps = c.prepareStatement(sb.toString())) {
            int idx = 1;
            if (de != null) ps.setDate(idx++, Date.valueOf(de));
            if (ate != null) ps.setDate(idx++, Date.valueOf(ate));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    LocalDate data = rs.getDate("data_pedido").toLocalDate();
                    StatusPedido status = StatusPedido.valueOf(rs.getString("status"));
                    String fp = rs.getString("forma_pagamento");
                    FormaPagamento pag = (fp == null) ? null : FormaPagamento.valueOf(fp);
                    boolean recebido = false;
                    if (supRecebido) {
                        try { recebido = rs.getBoolean("pagamento_recebido"); } catch (SQLException ignored) {}
                    }
                    BigDecimal total = rs.getBigDecimal("total");
                    int itens = rs.getInt("itens");
                    list.add(new PedidoResumo(id, data, status, pag, recebido, total, itens));
                }
            }
        } catch (SQLException e) {
            AppConfigFallback.enableSimulacao();
            return listarResumo(de, ate);
        }
        return list;
    }

    private static volatile Boolean CACHED_SUP_RECEBIDO = null;

    private boolean supportsPagamentoRecebido() {
        if (CACHED_SUP_RECEBIDO != null) return CACHED_SUP_RECEBIDO;
        try (Connection c = Conexao.getConnection()) {
            return supportsPagamentoRecebido(c);
        } catch (Exception e) {
            CACHED_SUP_RECEBIDO = false;
            return false;
        }
    }

    private boolean supportsPagamentoRecebido(Connection c) {
        if (CACHED_SUP_RECEBIDO != null) return CACHED_SUP_RECEBIDO;
        try {
            DatabaseMetaData md = c.getMetaData();
            try (ResultSet rs = md.getColumns(c.getCatalog(), null, "pedido", "pagamento_recebido")) {
                boolean ok = rs.next();
                CACHED_SUP_RECEBIDO = ok;
                return ok;
            }
        } catch (SQLException e) {
            CACHED_SUP_RECEBIDO = false;
            return false;
        }
    }

}
