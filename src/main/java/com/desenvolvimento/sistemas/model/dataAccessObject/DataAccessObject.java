package com.desenvolvimento.sistemas.model.dataAccessObject;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import com.desenvolvimento.sistemas.model.domain.IEntity;

import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;

public class DataAccessObject {
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
    private String tablePrefix = props.getProperty("app.database.prefix");


    // -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    // Método create, início.
    /*
    * O que mudou e por quê?
    *  a. method.getDeclaringClass() != clazz: Se o objeto for um Cliente, mas o método getNome() estiver declarado em Usuario, ele será ignorado neste INSERT. Assim, cada tabela recebe apenas seus respectivos campos.
    *  b. setObject vs String.valueOf: No banco, uma coluna INT ou DATE pode rejeitar uma String. O setObject deixa o driver do JDBC decidir a melhor conversão.
    *  c. executeUpdate(): Essencial para operações que modificam dados (INSERT, UPDATE, DELETE).
    *  d. RETURN_GENERATED_KEYS: Sem isso, o banco não devolve o ID auto-incrementado para o seu return 1L.
    *  e. Dica de Arquitetura: Como as tabelas estão separadas para Usuario e Cliente, você precisará chamar esse método create duas vezes (uma para a classe pai e outra para a filha) ou implementar uma lógica que percorra a hierarquia de classes e execute os inserts na ordem correta (pai primeiro para gerar o ID).
    *  f. Para fechar com chave de ouro e garantir que seu sistema seja robusto, aqui está como aplicar o Controle de Transação. Isso evita que o "Pai" seja gravado se o "Filho" der erro:
    */
    
    public Long create(IEntity entity) {
        List<Class<?>> hierarchy = getEntityHierarchy(entity.getClass());
        Long lastId = null;
        Connection conn = null; // Declaramos fora para acessar no catch/finally

        try {
            // 1. Obtemos a conexão
            conn = ConnectionDB.getInstance().getConnection();
            conn.setAutoCommit(false); // Inicia a transação

            // 2. Itera na hierarquia usando a MESMA conexão
            for (Class<?> clazz : hierarchy) {
                lastId = insertForClass(conn, entity, clazz, lastId);
            }

            // 3. Sucesso: Confirma tudo
            conn.commit();
            System.out.println("Transação concluída com sucesso!");

        } catch (Exception e) {
            // 4. Erro: Faz rollback
            if (conn != null) {
                try {
                    conn.rollback();
                    System.err.println("Rollback executado devido a: " + e.getMessage());
                } catch (SQLException ex) { ex.printStackTrace(); }
            }
            e.printStackTrace();
        } finally {
            // 5. Fechamento manual (obrigatório já que não usamos try-with-resources para a Connection)
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); // Restaura padrão
                    conn.close();             // Devolve para o banco
                } catch (SQLException e) { e.printStackTrace(); }
            }
        }
        return lastId;
    }

    private Long insertForClass(Connection conn, IEntity entity, Class<?> clazz, Long parentId) {
        ArrayList<String> columns = new ArrayList<>();
        ArrayList<Object> values = new ArrayList<>();
        ArrayList<String> placeholders = new ArrayList<>();

        // Se houver um ID do pai, ele deve ser incluído como FK se a tabela filha exigir
        // Aqui assumimos que o ID da filha é o mesmo ID da pai (comum em TABLE_PER_CLASS ou JOINED)
        if (parentId != null) {
            columns.add("id"); // ou o nome da sua FK
            placeholders.add("?");
            values.add(parentId);
        }

        Method[] methods = clazz.getDeclaredMethods(); // Pega apenas os métodos desta classe específica

        for (Method method : methods) {
            if (method.getName().startsWith("get") && !method.getName().equals("getClass") && method.getParameterCount() == 0) {
                if (!Collection.class.isAssignableFrom(method.getReturnType())) {
                    try {
                        // Ignoramos o getId pois ele será inserido via parentId ou gerado pelo banco no primeiro insert
                        if (method.getName().equalsIgnoreCase("getId")) continue;

                        Object value = method.invoke(entity);
                        columns.add(convertPascalCaseToSnakeCase(method.getName().substring(3)));
                        placeholders.add("?");
                        values.add(value);
                    } catch (Exception e) { e.printStackTrace(); }
                }
            }
        }

        String sql = buildInsertSql(clazz, columns);

        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            for (int i = 0; i < values.size(); i++) {
                pstmt.setObject(i + 1, values.get(i));
            }
            pstmt.executeUpdate();
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) return rs.getLong(1);
            }
        } catch (Exception e) {
            System.err.println("Erro na tabela " + clazz.getSimpleName() + ": " + e.getMessage());
        }
        // NOTA: Não fechamos 'conn' aqui para que o Singleton continue disponível
        return parentId; // Retorna o ID atual para a próxima iteração
    }

    private List<Class<?>> getEntityHierarchy(Class<?> startClass) {
        List<Class<?>> hierarchy = new ArrayList<>();
        Class<?> current = startClass;
        
        // Sobe na hierarquia enquanto a classe implementar IEntity
        while (current != null && IEntity.class.isAssignableFrom(current) && current != Object.class) {
            hierarchy.add(0, current); // Adiciona no início para o pai ficar na posição 0
            current = current.getSuperclass();
        }
        return hierarchy;
    }
    // Método create, fim.
    // -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------


    // Método read (início): -----------------------------------------------------------------------------------------------------------------------------------------------------------
    public List<IEntity> read(IEntity entity) {
        List<IEntity> listEntity = new ArrayList<>();
        Class<?> clazz = entity.getClass();
        
        // 1. Monta o WHERE dinâmico
        String where = buildWhereClause(entity);
        String tableName = tablePrefix + clazz.getSimpleName().toLowerCase();
        String sql = "SELECT * FROM `" + tableName + "` " + where;
       // System.out.println("DataAccessObject: sql = " + sql);

        // 2. Abre a conexão no try-with-resources para garantir o fechamento automático
        try (Connection conn = ConnectionDB.getInstance().getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                IEntity instance = (IEntity) clazz.getDeclaredConstructor().newInstance();

                // 2.1. Preenche os dados da tabela atual e sobe a hierarquia (Herança)
                fillEntityRecursively(instance, clazz, rs, conn);
                
                // 2.2. Processa as associações que NÃO são resolvidas por herança (Coleções e Objetos Externos)
                // Passamos a lógica para cá para evitar duplicidade dentro da recursão de campos simples.
                processAssociations(instance, conn);

                listEntity.add(instance);
            }
        } catch (Exception e) {
            System.err.println("Erro ao ler entidade: " + e.getMessage());
            e.printStackTrace();
        }
        return listEntity; // Ao chegar aqui, a Connection, o Stmt e o RS já foram fechados pelo try
    }
// Método read (fim): -----------------------------------------------------------------------------------------------------------------------------------------------------------

    /**
     * MÉTODO RECURSIVO: Preenche a instância com dados da tabela atual
     * e busca dados nas tabelas pai se houver herança.
     */
// Método fillEntityRecursively (início): -----------------------------------------------------------------------------------------------------------------------------------------------------------
    private void fillEntityRecursively(IEntity instance, Class<?> currentClazz, ResultSet rs, Connection conn) throws Exception {
        // 1. Preenche os atributos da classe atual
        Method[] methods = currentClazz.getDeclaredMethods();
        // 1.1. DENTRO DO LOOP DE MÉTODOS SETTERS
        for (Method method : methods) {
            if (isSetter(method)) {
                // System.out.println("DataAccessObject.fillEntityRecursively: method = " + method.getName());
                // Pega o tipo do parâmetro do setter
                Class<?> paramType = method.getParameterTypes()[0];

                // SE FOR UMA LISTA, PULE! (Trataremos fora deste loop)
                if (Collection.class.isAssignableFrom(paramType)) continue;

                String columnName = convertPascalCaseToSnakeCase(method.getName().substring(3));
                Object value = null;
                try {
                    value = rs.getObject(columnName);
                    if (value != null) {
                        Object convertedValue;

                        // 2. NOVA LÓGICA: Se o parâmetro for outra Entidade (ex: Cliente, Veiculo)
                        if (IEntity.class.isAssignableFrom(paramType)) {
                            // Instancia o objeto relacionado (ex: new Cliente())
                            IEntity childInstance = (IEntity) paramType.getDeclaredConstructor().newInstance();
                            
                            // Define o ID no objeto filho (o valor vindo da FK, ex: cliente_id)
                            long childId = ((Number) value).longValue();
                            
                            // Busca o setId do objeto filho para injetar o ID
                            Method setId = getMethodInHierarchy(paramType, "setId", Long.class);
                            if (setId == null) setId = getMethodInHierarchy(paramType, "setId", long.class);
                            if (setId != null) setId.invoke(childInstance, childId);

                            // Carrega os dados do objeto filho do banco de dados de forma recursiva
                            // Isso preencherá o Cliente com nome, CPF, etc.
                            readEntityComplete(childInstance, conn);
                            
                            // convertedValue = childInstance;
                            convertedValue = childInstance;
                            
                        } else {
                            // 3. Tipos comuns (String, Long, BigDecimal, etc.)
                            convertedValue = convertToTargetType(value, paramType);
                        }

                        method.invoke(instance, convertedValue);
                    }
                } catch (SQLException e) {
                    try {
                        value = rs.getObject(columnName + "_id");
                    } catch (SQLException e2) { /* Coluna realmente não existe */ }
                }
            }
        }

        // 2. Condição de parada e Recursão
        Class<?> superClass = currentClazz.getSuperclass();
        if (superClass != null && IEntity.class.isAssignableFrom(superClass) && superClass != Object.class) {
            
            // --- GARANTIA DO ID ---
            // Se o getId() retornar 0 ou null, tentamos pegar o 'id' do ResultSet atual
            // antes de subir para o pai, caso o ID esteja na tabela da classe filha.
            Long idObj = instance.getId(); // Pega como objeto Long (pode ser null)
            long currentId = (idObj != null) ? idObj : 0L;
            if (currentId <= 0) {
                try {
                   // Tenta pegar do ResultSet da tabela atual (filha)
                    currentId = rs.getLong("id"); 
                    
                    if (currentId > 0) {
                        // Busca o método setId em toda a hierarquia para garantir que o objeto receba o ID
                        Method setId = getMethodInHierarchy(instance.getClass(), "setId", Long.class);
                        if (setId == null) {
                            // Tenta com o primitivo caso o setter use long
                            setId = getMethodInHierarchy(instance.getClass(), "setId", long.class);
                        }
                        if (setId != null) {
                            setId.invoke(instance, currentId);
                        }
                    }
                } catch (SQLException e) {
                    // Se não achou a coluna 'id' na tabela atual, o ID obrigatoriamente
                    // viria da classe pai, mas precisamos dele para o WHERE.
                    System.err.println("Aviso: ID não encontrado na tabela " + currentClazz.getSimpleName());
                    return; // Se não tem ID, não tem como buscar o pai
                }
            }
            // Se chegamos aqui e o ID continua 0, não há como buscar na tabela pai
            if (currentId <= 0) {
                return; 
            }

            String parentTable = tablePrefix + superClass.getSimpleName().toLowerCase();
            String sqlParent = "SELECT * FROM `" + parentTable + "` WHERE id = ?";
            
            try (PreparedStatement stmtP = conn.prepareStatement(sqlParent)) {
                stmtP.setLong(1, currentId); // Usa o ID garantido
                try (ResultSet rsParent = stmtP.executeQuery()) {
                    if (rsParent.next()) {
                        fillEntityRecursively(instance, superClass, rsParent, conn);
                    }
                }
            }
        }
        // 3.TRATAMENTO DE ASSOCIAÇÕES (Coleções) BASEADO EM ANOTAÇÃOAPÓS O LOOP DE COLUNAS, TRATA A TABELA DE LIGAÇÃO
        //   3.1. field.isAnnotationPresent(...): O Java Reflection checa se o atributo tem a anotação específica. Isso elimina o "adivinhômetro".
        //   3.2. Lógica 1:N (OneToMany): Em vez de chamar buildM2mSelectSql, nós montamos um SQL simples que busca na tabela da classe filha.
        //        A convenção usada aqui é nome_da_classe_pai_id (ex: se a classe pai é Pessoa, a FK na tabela filha será pessoa_id).
        //   3.3. Lógica N:N (ManyToMany): Mantém a chamada original para a tabela de ligação.Isolamento de ResultSet:
        //      Note que usei stmtAssoc e rsAssoc. É vital não sobrescrever as variáveis stmt e rs do escopo superior para não quebrar a iteração principal.
        for (Field field : currentClazz.getDeclaredFields()) {
            if (Collection.class.isAssignableFrom(field.getType())) {
                
                // Verifica qual anotação está presente
                boolean isOneToMany = field.isAnnotationPresent(OneToMany.class);
                boolean isManyToMany = field.isAnnotationPresent(ManyToMany.class);

                if (!isOneToMany && !isManyToMany) continue; // Pula se não tiver nenhuma das duas

                ParameterizedType listType = (ParameterizedType) field.getGenericType();
                Class<?> childClass = (Class<?>) listType.getActualTypeArguments()[0];
                
                String sql;
                if (isManyToMany) {
                    // N:N - Usa tabela de ligação (ex: aluno_curso)
                    sql = buildM2mSelectSql(currentClazz, childClass);
                } else {
                    // 1:N - Busca direto na tabela filha usando a FK
                    // Ex: SELECT * FROM veiculo WHERE proprietario_id = ?
                    String fkColumn = currentClazz.getSimpleName().toLowerCase() + "_id";
                    String childTable = tablePrefix + childClass.getSimpleName().toLowerCase();
                    sql = "SELECT * FROM `" + childTable + "` WHERE `" + fkColumn + "` = ?";
                }

                try (PreparedStatement stmtAssoc = conn.prepareStatement(sql)) {
                    stmtAssoc.setLong(1, instance.getId());
                    ResultSet rsAssoc = stmtAssoc.executeQuery();
                    
                    List<Object> lista = new ArrayList<>();
                    while (rsAssoc.next()) {
                        IEntity child = (IEntity) childClass.getDeclaredConstructor().newInstance();
                        // Recursão para preencher os dados do objeto filho
                        fillEntityRecursively(child, childClass, rsAssoc, conn);
                        lista.add(child);
                    }
                    
                    String setterName = "set" + capitalize(field.getName());
                    Method method = currentClazz.getMethod(setterName, field.getType());
                    method.invoke(instance, lista);
                }
            }
        }
    }
//  -----------------------------------------------------------------------------------------------------------------------------------------------------------
    private void readEntityComplete(IEntity entity, Connection conn) throws Exception {
    Class<?> clazz = entity.getClass();
    String tableName = tablePrefix + clazz.getSimpleName().toLowerCase();
    String sql = "SELECT * FROM `" + tableName + "` WHERE id = ?";

    // IMPORTANTE: Não use ConnectionDB.getInstance() aqui, use a 'conn' recebida!
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setLong(1, entity.getId());
        try (ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                // Chama a recursão para preencher campos simples e herança do objeto filho
                fillEntityRecursively(entity, clazz, rs, conn);
            }
        }
    }
}

//  -----------------------------------------------------------------------------------------------------------------------------------------------------------

    // Método getMethodInHierarchy ----------------------------------------------------------------------------------------------------------------------------------------------------------- 
    private Method getMethodInHierarchy(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            try {
                return current.getDeclaredMethod(methodName, parameterTypes);
            } catch (NoSuchMethodException e) {
                current = current.getSuperclass();
            }
        }
        return null;
    }

    private boolean isSetter(Method method) {
        return method.getName().startsWith("set") && method.getParameterCount() == 1 && Modifier.isPublic(method.getModifiers());
    }

    // Métodos convertPascalCaseToSnakeCase:
    private String convertPascalCaseToSnakeCase(String name) {
        // 1. Converte de PascalCase para snake_case (ex: Cliente -> cliente, ClienteEspecial -> cliente_especial)
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (Character.isUpperCase(c) && i > 0) {
                result.append('_');
            }
            result.append(Character.toLowerCase(c));
        }

        String snakeCase = result.toString();

        return snakeCase;
    }


    // Método buildWhereClause (início): -----------------------------------------------------------------------------------------------------------------------------------------------------------   
    private String buildWhereClause(IEntity entity) {
        StringBuilder where = new StringBuilder();
        Class<?> clazz = entity.getClass();
        // Usamos getMethods para pegar getters da classe atual e das herdadas (ex: nome, sobrenome)
        Method[] methods = clazz.getMethods();

        for (Method method : methods) {
            if (isGetter(method)) { // Verifica se é um método get.
                try {
                    Object value = method.invoke(entity);

                    // Só adiciona ao WHERE se o valor não for nulo/vazio/zero
                    if (isValidValue(value)) {
                        // System.out.println("DataAccessObject: isValidValue = " + value);
                        // Remove 'get' ou 'is', converte para snake_case
                        // String fieldName = method.getName().startsWith("get") ? 3 : 2;
                        // Chama o conversor passando o nome e o tipo do parâmetro
                        String columnName = convertPascalCaseToSnakeCase(method.getName().substring(3));
                        
                        if (where.length() == 0) {
                            where.append(" WHERE ");
                        } else {
                            where.append(" AND ");
                        }

                        // Trata aspas para Strings e formatação para números
                        if (value instanceof String) {
                            where.append("`").append(columnName).append("` LIKE '%").append(value).append("%'");
                        } else {
                            where.append("`").append(columnName).append("` = ").append(value);
                        }
                    }
                } catch (Exception e) {
                    // Log de erro silencioso para métodos que falharem
                }
            }
        }
        return where.toString();
    }
    // Método buildWhereClause (fim): -----------------------------------------------------------------------------------------------------------------------------------------------------------

    private void processAssociations(IEntity instance, Connection conn) throws Exception {
        for (Field field : instance.getClass().getDeclaredFields()) {
            if (Collection.class.isAssignableFrom(field.getType())) {
                
                ParameterizedType listType = (ParameterizedType) field.getGenericType();
                Class<?> childClass = (Class<?>) listType.getActualTypeArguments()[0];

                if (IEntity.class.isAssignableFrom(childClass)) {
                    
                    // 1. Tenta identificar se é 1:N (Chave Estrangeira na Filha)
                    // Ex: Se o Pai é Usuario, procura setUsuarioId na Locacao
                    String fkSetterName = "set" + instance.getClass().getSimpleName() + "Id";
                    Method fkSetter = getMethodInHierarchy(childClass, fkSetterName, Long.class);

                    if (fkSetter != null) {
                        // --- LÓGICA 1:N ---
                        IEntity criteria = (IEntity) childClass.getDeclaredConstructor().newInstance();
                        fkSetter.invoke(criteria, instance.getId());
                        
                        // Chama o read dinâmico que já trata o WHERE automaticamente
                        List<IEntity> result = this.read(criteria);
                        
                        field.setAccessible(true);
                        field.set(instance, result);
                        
                    } else {
                        // --- LÓGICA N:N (Tabela de Ligação) ---
                        // Se não encontrou o setter da FK, assume que há uma tabela intermediária
                        try {
                            String sqlM2M = buildM2mSelectSql(instance.getClass(), childClass);
                            
                            try (PreparedStatement stmt = conn.prepareStatement(sqlM2M)) {
                                stmt.setLong(1, instance.getId());
                                try (ResultSet rsM2M = stmt.executeQuery()) {
                                    List<IEntity> listaM2M = new ArrayList<>();
                                    while (rsM2M.next()) {
                                        IEntity child = (IEntity) childClass.getDeclaredConstructor().newInstance();
                                        // Preenche o objeto com os dados vindo do JOIN
                                        fillEntityRecursively(child, childClass, rsM2M, conn);
                                        listaM2M.add(child);
                                    }
                                    field.setAccessible(true);
                                    field.set(instance, listaM2M);
                                }
                            }
                        } catch (SQLException e) {
                            // Se a tabela N:N também não existir, logamos o aviso
                            System.err.println("Aviso: Não foi possível associar " + field.getName() + 
                                            ". Nem FK nem Tabela de Ligação encontradas.");
                        }
                    }
                }
            }
        }
    }
   // -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    private boolean isValidValue(Object value) {
        if (value == null) return false;

        // 1. Tratamento para Boolean (Evita que 'false' entre no filtro)
        if (value instanceof Boolean) {
            return (Boolean) value; // Só retorna true se o valor for true
        }

        // 2. Filtra IDs e números (Long, Integer, Double, etc.)
        // Se value for Long, entra aqui e verifica se é > 0
        if (value instanceof Number) {
            return ((Number) value).doubleValue() > 0;
        }

        // 3. Filtra Strings vazias
        if (value instanceof String) {
            return !((String) value).trim().isEmpty();
        }

        // 4. Ignora coleções
        if (value instanceof Collection) {
            return false;
        }

        return true;
    }

    // Este método garante que o valor vindo do banco seja compatível com o tipo do método setter. A conversão seja feita antes do invoke.
    private Object convertToTargetType(Object value, Class<?> targetType) {
        
        if (value == null) return null; // Se o valor for nulo, ignora.
        if (targetType.isInstance(value)) return value; // Se o valor for coleção, ignora, pois será tratado em outro local.
        // Verificação inicial ou dentro do default
        if (targetType.isEnum()) {
            return Enum.valueOf((Class<Enum>) targetType, value.toString());
        }
        // System.out.println("DataAccessObject.convertToTargetType: Tipo de atributo = " + targetType.getSimpleName());

        // Switch moderno simplifica a lógica de "Para qual tipo vou?"
        return switch (targetType.getSimpleName()) {
            case "Integer", "int" -> switch (value) {
                case Number n -> n.intValue();
                case String s -> Integer.parseInt(s.trim());
                default -> 0;
            };
            // Proteção para null antes de chamar .longValue():
            case "Long", "long" -> {
                if (value == null) yield 0L; // Evita o NPE
                if (value instanceof Number n) yield n.longValue();
                yield 0L;
            }
            case "BigDecimal" -> new java.math.BigDecimal(value.toString());
            case "Boolean", "boolean" -> switch (value) {
                case Boolean b -> b;
                case Number n -> n.intValue() != 0;
                case String s -> s.equalsIgnoreCase("true") || s.equals("1");
                default -> false;
            };
            case "Date" -> (value instanceof java.util.Date d) ? new java.util.Date(d.getTime()) : 0;
            case "LocalDateTime" -> switch (value) {
                case java.sql.Timestamp t -> t.toLocalDateTime();
                case java.sql.Date d -> d.toLocalDate().atStartOfDay();
                case String s -> java.time.LocalDateTime.parse(s);
                default -> 0;
            };
            default -> {
                if (targetType.isEnum()) {
                    yield Enum.valueOf((Class<Enum>) targetType, value.toString().toUpperCase().trim());
                }
                yield value; // ou null
            }
        };
    }
    // Método update, início.

    /*
    * A lógica é muito similar à do create, inclusive na necessidade de percorrer a hierarquia de tabelas. A principal diferença é a montagem da cláusula SET do SQL, que deve ser dinâmica para incluir apenas o que foi preenchido.
    * Aqui está a implementação do update, focada em ser "parcial" (ignora nulos e vazios):
    * O que essa implementação faz de especial:
    *     Atualização Seletiva: O if (value != null && !value.toString().trim().isEmpty()) garante que, se você enviar um objeto Cliente apenas com o nome preenchido, o cpf atual no banco não será sobrescrito por nulo.
    *     Independência de Tabelas: O getDeclaredMethods() garante que a query da tabela usuario só contenha campos de usuário, e a de cliente apenas campos de cliente.
    *     Segurança: O uso de setObject protege contra SQL Injection e lida com diferentes tipos de dados.
    */
    public Long update(IEntity entity) {
        if (entity.getId() == null || entity.getId() <= 0) {
            throw new RuntimeException("ID inválido para atualização.");
        }

        List<Class<?>> hierarchy = getEntityHierarchy(entity.getClass());
        Connection conn = null; // Declarada fora para ser acessível em todos os blocos

        try {
            conn = ConnectionDB.getInstance().getConnection();
            conn.setAutoCommit(false); // Inicia transação

            for (Class<?> clazz : hierarchy) {
                // Passa a conexão para manter a transação atômica entre as tabelas da hierarquia
                updateForClass(conn, entity, clazz);
            }

            conn.commit();
            return entity.getId();
        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.rollback();
                    System.err.println("Rollback executado no Update devido a: " + e.getMessage());
                } catch (SQLException ex) { ex.printStackTrace(); }
            }
            e.printStackTrace();
            return null;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); // Restaura o padrão do driver
                    conn.close();             // Fecha manualmente a conexão
                } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }


    private void updateForClass(Connection conn, IEntity entity, Class<?> clazz) throws Exception {
        ArrayList<String> setClauses = new ArrayList<>();
        ArrayList<Object> values = new ArrayList<>();

        // getDeclaredMethods() para garantir que cada tabela só atualize suas colunas
        Method[] methods = clazz.getDeclaredMethods();

        for (Method method : methods) {
            if (method.getName().startsWith("get") && !method.getName().equals("getClass") && method.getParameterCount() == 0) {
                
                // Ignoramos coleções e o próprio ID (ID vai no WHERE)
                if (!Collection.class.isAssignableFrom(method.getReturnType()) && !method.getName().equalsIgnoreCase("getId")) {
                    
                    Object value = method.invoke(entity);

                    /*
                    // FILTRO: Só adiciona ao SQL se NÃO for nulo e NÃO for String vazia
                    if (value != null && !value.toString().trim().isEmpty()) {
                        String colName = convertPascalCaseToSnakeCase(method.getName().substring(3));
                        setClauses.add(colName + " = ?");
                        values.add(value);
                    }
                    */
                    // Dentro do loop de métodos no seu update:
                    // FILTRO REFINADO:
                    // Se for null, ignoramos (presume-se que não foi alterado no formulário)
                    if (value != null) {
                        String colName = convertPascalCaseToSnakeCase(method.getName().substring(3));
                        
                        // Se for uma String vazia "", ela VAI para o SET para limpar o campo no banco
                        setClauses.add(colName + " = ?");
                        values.add(value);
                    }
                }
            }
        }

        // Se não houver nada para atualizar nesta classe específica, pula para a próxima
        if (setClauses.isEmpty()) return;

        String sql = "UPDATE " + tablePrefix + clazz.getSimpleName().toLowerCase() +
                    " SET " + String.join(", ", setClauses) + " WHERE id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            int i = 1;
            for (Object val : values) {
                pstmt.setObject(i++, val);
            }
            pstmt.setLong(i, entity.getId()); // O ID é sempre o último parâmetro
            pstmt.executeUpdate();
        }
    }
    // Método update, fim.
    // -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    // -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    // Método delete, início.
    public Long delete(IEntity entity) {
        if (entity.getId() == null || entity.getId() <= 0) {
            throw new RuntimeException("ID inválido para exclusão.");
        }

        List<Class<?>> hierarchy = getEntityHierarchy(entity.getClass());
        Collections.reverse(hierarchy); // Essencial: deleta do mais específico para o mais genérico

        Connection conn = null;

        try {
            conn = ConnectionDB.getInstance().getConnection();
            conn.setAutoCommit(false); 

            for (Class<?> clazz : hierarchy) {
                String tableName = tablePrefix + clazz.getSimpleName().toLowerCase();
                String sql = "DELETE FROM `" + tableName + "` WHERE id = ?"; // Use crases por segurança

                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setLong(1, entity.getId());
                    int affected = pstmt.executeUpdate();
                    System.out.println("Deletado de " + tableName + ": " + affected + " linha(s)");
                }
            }

            conn.commit(); 
            return entity.getId();
        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.rollback();
                    System.err.println("Erro ao deletar hierarquia. Rollback executado: " + e.getMessage());
                } catch (SQLException ex) { ex.printStackTrace(); }
            }
            e.printStackTrace();
            return 0L;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close(); // ADICIONADO: Devolve a conexão para o banco
                } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }
    // Método delete, fim.
    // -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    /*
    * É um método simples, mas essencial para converter o nome do atributo (ex: veiculos) no padrão camelCase do setter (ex: setVeiculos).
    * Aqui está uma implementação eficiente que você pode adicionar como um método private na classe DAO:
    */
    private String capitalize(String str) {
        // Pega a primeira letra, coloca em maiúsculo e concatena com o resto da string
        return (str == null || str.length() == 0) ? str
            : str.substring(0, 1).toUpperCase() + str.substring(1);
    }
    private String buildInsertSql(Class<?> clazz, List<String> columns) {
        return "INSERT INTO " + tablePrefix + clazz.getSimpleName().toLowerCase() 
            + " (" + String.join(", ", columns) + ") VALUES (" 
            + String.join(", ", Collections.nCopies(columns.size(), "?")) + ")";
    }

    private String buildM2mSelectSql(Class<?> parentClass, Class<?> childClass) {
        String tableChild = tablePrefix + childClass.getSimpleName().toLowerCase();
        String tableLink = tablePrefix + parentClass.getSimpleName().toLowerCase() + "_" + childClass.getSimpleName().toLowerCase();
        String fkChild = childClass.getSimpleName().toLowerCase() + "_id";
        String fkParent = parentClass.getSimpleName().toLowerCase() + "_id";
        
        return "SELECT c.* FROM `" + tableChild + "` c " +
            "INNER JOIN `" + tableLink + "` l ON c.id = l." + fkChild + " " +
            "WHERE l." + fkParent + " = ?";
    }
    // Valida se o método é um getter padrão Java Bean.
    private boolean isGetter(Method method) {
        String name = method.getName();
        return (name.startsWith("get") || name.startsWith("is"))
                && method.getParameterCount() == 0
                && !name.equals("getClass")
                && Modifier.isPublic(method.getModifiers());
    }

}
