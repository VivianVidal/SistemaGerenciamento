# Sistema Barraca — Gerenciamento de Pedidos

## Status do Projeto
🚧 Em desenvolvimento

## Tecnologias Aplicadas
- **Java** (JDK 8+)
- **NetBeans IDE**
- **Java Swing** (interfaces desktop)
- **MySQL** (banco de dados)
- **JDBC** (conexão e operações no banco)
- **Git/GitHub** (versionamento)

## Time de Desenvolvedores
- **Aluno:** Vivian Vidal

## Objetivo do Software
Desenvolver um sistema desktop para auxiliar uma barraca/lanchonete no **controle do cardápio (produtos)** e no **gerenciamento de pedidos**, permitindo registrar itens, calcular automaticamente o total, controlar o status do pedido e realizar consultas por período.

## Funcionalidades do Sistema (Requisitos)

### Requisitos Funcionais
- **RF01 — Gerenciar Produtos (CRUD):**  
  Permitir cadastrar, editar, listar e inativar/ativar produtos do cardápio.

- **RF02 — Criar e manter Pedidos:**  
  Permitir abrir pedidos, adicionar/remover itens e calcular automaticamente o valor total.

- **RF03 — Fechar Pedido com Pagamento:**  
  Permitir selecionar a forma de pagamento (Dinheiro/Cartão/PIX) e fechar o pedido.

- **RF04 — Consultar Pedidos por Data:**  
  Permitir filtrar e consultar pedidos por período (data inicial e data final).

- **RF05 — Cancelar Pedido:**  
  Permitir cancelar um pedido (alterando o status para **CANCELADO**) mediante confirmação.

- **RF06 — Reabrir Pedido:**  
  Permitir reabrir um pedido (alterando o status para **ABERTO**) mediante confirmação, quando aplicável.

- **RF07 — Marcar Pagamento Recebido:**  
  Permitir marcar se o pagamento do pedido foi **recebido (Sim/Não)**.

### Requisitos Não Funcionais
- **RNF01 — Linguagem/IDE:** O sistema deve ser desenvolvido em **Java**, utilizando a **IDE NetBeans**.
- **RNF02 — Banco de dados:** O sistema deve usar **MySQL** para armazenamento das informações.
- **RNF03 — Usabilidade:** A interface deve ser simples, intuitiva e de fácil aprendizado.
- **RNF04 — Integridade dos dados:** Garantir consistência dos dados no banco (uso de chaves estrangeiras e validações).
- **RNF05 — Compatibilidade:** O sistema deve ser compatível com **Windows**.

---

## Como Executar (Resumo)
1. Criar o banco `barraca_db` e as tabelas no MySQL.
2. Configurar as credenciais no arquivo `db.properties`.
3. Abrir o projeto no NetBeans e executar.

> Observação: o projeto utiliza JDBC para a conexão com o MySQL.
