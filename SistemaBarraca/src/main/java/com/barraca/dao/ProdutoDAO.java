
package com.barraca.dao;

import com.barraca.model.Produto;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProdutoDAO extends BaseDAO {

    public List<Produto> listar(boolean incluirInativos) {
        if (simulacao()) {
            List<Produto> out = new ArrayList<>();
            for (Produto p : InMemoryDB.produtos) {
                if (incluirInativos || p.isAtivo()) out.add(p);
            }
            return out;
        }

        String sql = "SELECT id, nome, preco, ativo FROM produto " +
                     (incluirInativos ? "" : "WHERE ativo=1 ") +
                     "ORDER BY nome";
        List<Produto> list = new ArrayList<>();
        try (Connection c = Conexao.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Produto p = new Produto();
                p.setId(rs.getInt("id"));
                p.setNome(rs.getString("nome"));
                p.setPreco(rs.getBigDecimal("preco"));
                p.setAtivo(rs.getInt("ativo") == 1);
                // descrição não está no SQL original; mantemos como null no DB
                list.add(p);
            }
        } catch (SQLException e) {
            // Se falhar, cai para simulação
            AppConfigFallback.enableSimulacao();
            return listar(incluirInativos);
        }
        return list;
    }

    public Produto inserir(Produto p) {
        if (simulacao()) {
            p.setId(InMemoryDB.nextProdutoId());
            InMemoryDB.produtos.add(p);
            return p;
        }

        String sql = "INSERT INTO produto (nome, preco, ativo) VALUES (?,?,?)";
        try (Connection c = Conexao.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getNome());
            ps.setBigDecimal(2, p.getPreco());
            ps.setInt(3, p.isAtivo() ? 1 : 0);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) p.setId(keys.getInt(1));
            }
            return p;
        } catch (SQLException e) {
            AppConfigFallback.enableSimulacao();
            return inserir(p);
        }
    }

    public void atualizar(Produto p) {
        if (simulacao()) {
            for (int i=0;i<InMemoryDB.produtos.size();i++) {
                if (InMemoryDB.produtos.get(i).getId().equals(p.getId())) {
                    InMemoryDB.produtos.set(i, p);
                    return;
                }
            }
            return;
        }

        String sql = "UPDATE produto SET nome=?, preco=?, ativo=? WHERE id=?";
        try (Connection c = Conexao.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, p.getNome());
            ps.setBigDecimal(2, p.getPreco());
            ps.setInt(3, p.isAtivo() ? 1 : 0);
            ps.setInt(4, p.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            AppConfigFallback.enableSimulacao();
            atualizar(p);
        }
    }

    public void setAtivo(int id, boolean ativo) {
        if (simulacao()) {
            for (Produto p : InMemoryDB.produtos) {
                if (p.getId() == id) { p.setAtivo(ativo); return; }
            }
            return;
        }

        String sql = "UPDATE produto SET ativo=? WHERE id=?";
        try (Connection c = Conexao.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, ativo ? 1 : 0);
            ps.setInt(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            AppConfigFallback.enableSimulacao();
            setAtivo(id, ativo);
        }
    }
}
