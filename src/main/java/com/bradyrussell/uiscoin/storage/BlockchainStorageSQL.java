package com.bradyrussell.uiscoin.storage;

import com.bradyrussell.uiscoin.blockchain.BlockChainStorageBase;
import com.bradyrussell.uiscoin.transaction.Transaction;
import com.mchange.v2.c3p0.ComboPooledDataSource;

import java.beans.PropertyVetoException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;

public class BlockchainStorageSQL extends BlockChainStorageBase {
    private final String type;
    private final String server;
    private final int port;
    private final String database;
    private final String username;
    private final String password;
    private ArrayList<Transaction> mempool = null;
    private final HashSet<String> tablesCache = new HashSet<>();

    private ComboPooledDataSource dataSource = null;

    public BlockchainStorageSQL(String database, String username, String password) {
        this.type = "jdbc:mysql";
        this.server = "localhost";
        this.port = 3306;
        this.database = database;
        this.username = username;
        this.password = password;
    }

    public BlockchainStorageSQL(String server, int port, String database, String username, String password) {
        this.type = "jdbc:mysql";
        this.server = server;
        this.port = port;
        this.database = database;
        this.username = username;
        this.password = password;
    }

    public BlockchainStorageSQL(String type, String server, int port, String database, String username, String password) {
        this.type = type;
        this.server = server;
        this.port = port;
        this.database = database;
        this.username = username;
        this.password = password;
    }

    private Connection getConnection() {
        try {
            return dataSource.getConnection();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean open() {
        try {
            mempool = new ArrayList<>();
            tablesCache.clear();

            dataSource = new ComboPooledDataSource();
            dataSource.setDriverClass("com.mysql.cj.jdbc.Driver");
            dataSource.setJdbcUrl(type+"://"+server+":"+port+"/"+database);
            dataSource.setUser(username);
            dataSource.setPassword(password);
            return true;
        } catch (PropertyVetoException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void close() {
        mempool = null;
        tablesCache.clear();
        dataSource.close();
        dataSource = null;
    }

    @Override
    public void addToMempool(Transaction transaction) {
        mempool.add(transaction);
    }

    @Override
    public void removeFromMempool(Transaction transaction) {
        mempool.remove(transaction);
    }

    @Override
    public List<Transaction> getMempool() {
        return mempool;
    }

    @Override
    public byte[] get(byte[] bytes, String s) {
        Connection connection = getConnection();
        if(connection == null) return null;
        createTableIfNotExists(s, connection);
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT uiscoin_value FROM uiscoin." + s + " WHERE uiscoin_key = ?;");
            statement.setString(1, Base64.getEncoder().encodeToString(bytes));
            ResultSet resultSet = statement.executeQuery();
            if(resultSet.next()) {
                Blob blob = resultSet.getBlob(1);
                return blob.getBinaryStream().readAllBytes();
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        } finally {
            try {
                connection.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public void put(byte[] bytes, byte[] bytes1, String s) {
        Connection connection = getConnection();
        if(connection == null) return;
        createTableIfNotExists(s, connection);
        try {
            PreparedStatement statement = connection.prepareStatement("INSERT into uiscoin." + s + " VALUES(?, ?) ON DUPLICATE KEY UPDATE uiscoin_value = ?;");
            statement.setString(1, Base64.getEncoder().encodeToString(bytes));
            statement.setBlob(2, new ByteArrayInputStream(bytes1));
            statement.setBlob(3, new ByteArrayInputStream(bytes1));
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                connection.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
    }

    @Override
    public void remove(byte[] bytes, String s) {
        Connection connection = getConnection();
        if(connection == null) return;
        createTableIfNotExists(s, connection);
        try {
            PreparedStatement statement = connection.prepareStatement("DELETE from uiscoin." + s + " WHERE uiscoin_key = ?;");
            statement.setString(1, Base64.getEncoder().encodeToString(bytes));
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                connection.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
    }

    @Override
    public boolean exists(byte[] bytes, String s) {
        Connection connection = getConnection();
        if(connection == null) return false;
        createTableIfNotExists(s, connection);
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT COUNT(uiscoin_value) FROM uiscoin." + s + " WHERE uiscoin_key = ?;");
            statement.setString(1, Base64.getEncoder().encodeToString(bytes));
            ResultSet resultSet = statement.executeQuery();
            if(resultSet.next()) {
                return resultSet.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                connection.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
        return false;
    }

    @Override
    public List<byte[]> keys(String s) {
        Connection connection = getConnection();
        if(connection == null) return null;
        createTableIfNotExists(s, connection);
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT uiscoin_key FROM uiscoin." + s + ";");
            ResultSet resultSet = statement.executeQuery();

            ArrayList<byte[]> keySet = new ArrayList<>();

            while(resultSet.next()) {
                keySet.add(Base64.getDecoder().decode(resultSet.getString(1)));
            }
            return keySet;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                connection.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
        return null;
    }

    // returns whether a new table was created
    public boolean createTableIfNotExists(String tableName, Connection connection){
        if(tablesCache.contains(tableName)) return false;
        try {
            // cannot prepare table names...
            PreparedStatement statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS uiscoin."+tableName+" (uiscoin_key varchar(255) primary key, uiscoin_value blob);");
            boolean execute = statement.execute();
            tablesCache.add(tableName);
            return execute;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
