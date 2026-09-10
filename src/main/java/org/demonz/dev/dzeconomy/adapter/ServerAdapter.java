package org.demonz.dev.dzeconomy.adapter;

public interface ServerAdapter {

    boolean loadSQLiteDriver();

    ServerPlatform getPlatform();
}
