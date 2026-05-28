package online.demonzdevelopment.dzeconomy.adapter;

public interface ServerAdapter {

    boolean loadSQLiteDriver();

    ServerPlatform getPlatform();
}
