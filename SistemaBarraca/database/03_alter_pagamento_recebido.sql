USE barraca_db;
ALTER TABLE pedido ADD COLUMN pagamento_recebido TINYINT(1) NOT NULL DEFAULT 0;
