package com.desenvolvimento.sistemas.model.dataAccessObject;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Connection;

import java.util.Properties;

public class ConnectionDB {

    private static ConnectionDB instance;
    private Connection connection;
    
    // 1. O Properties deve ser inicializado antes de tudo
    private static Properties props = new Properties();

    // 2. BLOCO ESTÁTICO: Carrega o arquivo assim que a classe é lida pela JVM
    static {
        try (var is = ConnectionDB.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (is == null) {
                throw new RuntimeException("Arquivo application.properties não encontrado!");
            }
            props.load(is);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao carregar configurações", e);
        }
    }

    // 3. Pegue os valores dentro do construtor ou métodos, não direto na declaração da variável
    private String url      = props.getProperty("spring.datasource.url");
    private String user     = props.getProperty("spring.datasource.username");
    private String password = props.getProperty("spring.datasource.password");

    private ConnectionDB() {
        try {
            Class.forName("org.mariadb.jdbc.Driver");
            // Agora url, user e password já possuem os valores do arquivo
            this.connection = DriverManager.getConnection(url, user, password);
        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException("Erro ao conectar ao banco", e);
        }
    }

    public static synchronized ConnectionDB getInstance() {
        if (instance == null) {
            instance = new ConnectionDB();
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }
}

