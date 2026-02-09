
package com.barraca.dao;

/**
 * Caso o MySQL não esteja disponível, o app alterna automaticamente para simulação,
 * evitando travar a apresentação.
 */
public final class AppConfigFallback {
    private static volatile boolean warned = false;

    private AppConfigFallback() {}

    public static void enableSimulacao() {
        // Não conseguimos alterar o properties em runtime de forma simples.
        // Então, usamos uma flag interna no service (ver BaseDAO).
        BaseDAO.forceSimulacao = true;
        if (!warned) {
            warned = true;
            System.err.println("[AVISO] Falha ao conectar no MySQL. Alternando para modo SIMULAÇÃO (memória).");
        }
    }
}
