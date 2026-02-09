package com.barraca.dao;

import com.barraca.model.ItemPedido;
import com.barraca.model.Pedido;
import com.barraca.model.Produto;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ItemPedidoDAO extends BaseDAO {

    /**
     * Insere itens usando conexão própria (legado).
     */
    public void inserirItens(Pedido pedido) {
        if (simulacao()) return;

        try (Connection c = Conexao.getConnection()) {
            inserirItens(c, pedido);
        } catch (SQLException e) {
            AppConfigFallback.enableSimulacao();
            inserirItens(pedido);
        }
    }

    /**
     * Insere itens usando uma conexão existente (permite transação junto com o pedido).
     */
    public void inserirItens(Connection c, Pedido pedido) throws SQLException {
        if (simulacao()) return;

        String sql = "INSERT INTO item_pedido (pedido_id, produto_id, quantidade, preco_unitario) VALUES (?,?,?,?)";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            for (ItemPedido it : pedido.getItens()) {
                ps.setInt(1, pedido.getId());
                ps.setInt(2, it.getProduto().getId());
                ps.setInt(3, it.getQuantidade());
                ps.setBigDecimal(4, it.getPrecoUnitario());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    public List<ItemPedido> listarPorPedido(int pedidoId) {
        if (simulacao()) {
            for (Pedido p : InMemoryDB.pedidos) {
                if (p.getId() == pedidoId) return new ArrayList<>(p.getItens());
            }
            return new ArrayList<>();
        }

        String sql = "SELECT i.id, i.quantidade, i.preco_unitario, pr.id AS produto_id, pr.nome AS produto_nome, pr.preco AS produto_preco, pr.ativo " +
                     "FROM item_pedido i " +
                     "JOIN produto pr ON pr.id = i.produto_id " +
                     "WHERE i.pedido_id = ? " +
                     "ORDER BY i.id";
        List<ItemPedido> list = new ArrayList<>();
        try (Connection c = Conexao.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, pedidoId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Produto pr = new Produto();
                    pr.setId(rs.getInt("produto_id"));
                    pr.setNome(rs.getString("produto_nome"));
                    pr.setPreco(rs.getBigDecimal("produto_preco"));
                    pr.setAtivo(rs.getInt("ativo") == 1);

                    ItemPedido it = new ItemPedido();
                    it.setId(rs.getInt("id"));
                    it.setProduto(pr);
                    it.setQuantidade(rs.getInt("quantidade"));
                    it.setPrecoUnitario(rs.getBigDecimal("preco_unitario"));
                    list.add(it);
                }
            }
        } catch (SQLException e) {
            AppConfigFallback.enableSimulacao();
            return listarPorPedido(pedidoId);
        }
        return list;
    }
}
