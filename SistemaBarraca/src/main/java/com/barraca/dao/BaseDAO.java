
package com.barraca.dao;

public abstract class BaseDAO {
    static volatile boolean forceSimulacao = false;

    protected boolean simulacao() {
        return forceSimulacao || AppConfig.simulacao();
    }
}
