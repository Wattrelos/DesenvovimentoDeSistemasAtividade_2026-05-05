package com.desenvolvimento.sistemas.model.domain.factory;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.desenvolvimento.sistemas.model.dataAccessObject.ConnectionDB;

public class ClassGeneratorFromDataBase3 {
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

    public static void main(String[] args) {
        // Substitua pelo seu banco e prefixo
        new ClassGeneratorFromDataBase3().generateClasses("locadora", props.getProperty("app.database.prefix"));
    }

    public void generateClasses(String dbName, String prefix) {
        try (Connection conn = ConnectionDB.getInstance().getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet tables = metaData.getTables(dbName, null, "%", new String[]{"TABLE"});

            while (tables.next()) {
                String rawTableName = tables.getString("TABLE_NAME");
                if (rawTableName.startsWith("sys_") || rawTableName.contains("information_schema")) continue;

                String className = convertSnakeCaseToPascal(rawTableName.replaceFirst("^" + prefix, ""));
                generateJavaFile(dbName, rawTableName, className, metaData);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void generateJavaFile(String dbName, String rawTableName, String className, DatabaseMetaData metaData) throws Exception {
        // Caminho ajustado para estrutura Maven
        String path = "src/main/java/com/desenvolvimento/model/domain/entities/" + className + ".java";                       
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(path))) {
            writer.println("package com.desenvolvimento.model.domain.entities;");
            writer.println("\nimport com.desenvolvimento.model.domain.IEntity;"); // Import da Interface
            writer.println("import java.util.Date;");
            
            writer.println("\npublic class " + className + " implements IEntity {");

            List<String[]> fields = new ArrayList<>();
            ResultSet columns = metaData.getColumns(dbName, null, rawTableName, "%");

            while (columns.next()) {
                String rawColName = columns.getString("COLUMN_NAME");
                String type = mapSqlTypeToJava(columns.getString("TYPE_NAME"), rawColName);
                String fieldName = convertSnakeCaseToCamel(rawColName);

                writer.println("    private " + type + " " + fieldName + ";");
                fields.add(new String[]{type, fieldName});
            }

            // Construtor Padrão (Boa prática)
            writer.println("\n    public " + className + "() {}");

            // Gerar Getters e Setters
            for (String[] field : fields) {
                String type = field[0];
                String name = field[1];
                String capitalized = name.substring(0, 1).toUpperCase() + name.substring(1);

                // Getter
                // writer.println("\n    @Override"); // Adiciona Override para o getId se for o caso (Criar um algoritmo que verifica se o método existe na interface.)
                writer.println("    public " + type + " get" + capitalized + "() { return " + name + "; }");
                
                // Setter
                // writer.println("\n    @Override"); (Criar um algoritmo que verifica se o método existe na interface.)
                writer.println("    public void set" + capitalized + "(" + type + " " + name + ") { this." + name + " = " + name + "; }");
            }

            writer.println("}");
            System.out.println("Entidade gerada com sucesso: " + className);
        }
    }
    private String convertSnakeCaseToPascal(String s) {
        StringBuilder res = new StringBuilder();
        for (String p : s.split("_")) {
            if (!p.isEmpty()) res.append(p.substring(0, 1).toUpperCase()).append(p.substring(1).toLowerCase());
        }
        return res.toString();
    }

    private String convertSnakeCaseToCamel(String s) {
        String pascal = convertSnakeCaseToPascal(s);
        return pascal.substring(0, 1).toLowerCase() + pascal.substring(1);
    }

    private String mapSqlTypeToJava(String type, String col) {
        if (col.toLowerCase().endsWith("id") || type.contains("INT") || type.contains("BIGINT")) return "Long";
        if (type.contains("CHAR") || type.contains("TEXT")) return "String";
        if (type.contains("DECIMAL") || type.contains("DOUBLE")) return "Double";
        if (type.contains("DATE") || type.contains("TIME")) return "java.util.Date";
        return "Object";
    }
}
