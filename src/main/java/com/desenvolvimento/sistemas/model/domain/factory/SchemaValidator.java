package com.desenvolvimento.sistemas.model.domain.factory;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;

import com.desenvolvimento.sistemas.AppConfig;
import com.desenvolvimento.sistemas.model.domain.IEntity;

import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;

public class SchemaValidator {

    public static void validateAllEntities() {
        try {
            String path = AppConfig.ENTITIES_PATH.replaceAll("\\.$", "");
            List<Class<?>> classes = getClassesFromPackage(path);

            for (Class<?> clazz : classes) {
                // 1. IGNORA Interfaces, Classes Abstratas e ENUMS
                if (clazz.isInterface() ||
                    Modifier.isAbstract(clazz.getModifiers()) ||
                    clazz.isEnum()) {
                    continue;
                }

                // NOVA VERIFICAÇÃO: Obrigatoriedade da IEntity
                if (!IEntity.class.isAssignableFrom(clazz)) {
                    throw new RuntimeException(
                        "\n[ERRO DE ARQUITETURA]: A classe '" + clazz.getSimpleName() + 
                        "' está no pacote de entidades mas NÃO implementa 'IEntity'.\n" +
                        "Toda entidade de domínio deve seguir este contrato para o DAO funcionar."
                    );
                }

                // Se passou, valida os nomes dos setters (relação entre entidades)
                validateNamingConvention((Class<? extends IEntity>) clazz);
            }
            System.out.println("✅ Todas as classes em '" + path + "' implementam IEntity e seguem os padrões.");
        } catch (Exception e) {
            System.err.println("❌ Falha na validação: " + e.getMessage());
            System.exit(1);
        }
    }

    private static void validateNamingConvention(Class<? extends IEntity> clazz) {
        // 1. Validação de Campos (Coleções 1:N e N:N)
        for (Field field : clazz.getDeclaredFields()) {
            if (Collection.class.isAssignableFrom(field.getType())) {
                
                // Verifica se tem ao menos uma das duas anotações
                boolean hasOneToMany = field.isAnnotationPresent(OneToMany.class);
                boolean hasManyToMany = field.isAnnotationPresent(ManyToMany.class);

                if (!hasOneToMany && !hasManyToMany) {
                    throw new RuntimeException(
                        "\n[ERRO DE MAPEAMENTO]: Na classe '" + clazz.getSimpleName() + 
                        "', a coleção '" + field.getName() + "' precisa da anotação @OneToMany ou @ManyToMany " +
                        "para que o DAO saiba como carregá-la.");
                }

                // Validação Extra: Garante que a lista tenha um tipo genérico (ex: List<Acessorio>)
                if (!(field.getGenericType() instanceof ParameterizedType)) {
                    throw new RuntimeException(
                        "\n[ERRO DE GENERICO]: A coleção '" + field.getName() + "' na classe '" + 
                        clazz.getSimpleName() + "' deve especificar o tipo. Ex: List<Entidade>.");
                }
            }
        }

        // 2. Validação de Setters (O que já tínhamos antes)
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.getName().startsWith("set") && method.getParameterCount() == 1) {
                Class<?> paramType = method.getParameterTypes()[0];
                if (IEntity.class.isAssignableFrom(paramType) && !method.getName().endsWith("Id")) {
                    // ... erro do sufixo Id ...
                }
            }
        }
    }


    // Método auxiliar para ler as classes do classpath
    private static List<Class<?>> getClassesFromPackage(String packageName) throws Exception {
    String path = packageName.replace('.', '/');
    Enumeration<URL> resources = Thread.currentThread().getContextClassLoader().getResources(path);
    List<Class<?>> classes = new ArrayList<>();
    
    while (resources.hasMoreElements()) {
        URL resource = resources.nextElement();
        // O decode serve para tratar espaços ou caracteres especiais no caminho do diretório
        File directory = new File(java.net.URLDecoder.decode(resource.getFile(), "UTF-8"));
        
        if (directory.exists() && directory.isDirectory()) {
            for (File file : directory.listFiles()) {
                if (file.getName().endsWith(".class")) {
                    // REMOVE o ".class" do final
                    String fileName = file.getName().replace(".class", "");
                    // MONTA o nome completo: pacote.Classe (sem barras!)
                    String className = packageName + "." + fileName;
                    
                    try {
                        classes.add(Class.forName(className));
                    } catch (ClassNotFoundException e) {
                        System.err.println("Não foi possível carregar: " + className);
                    }
                }
            }
        }
    }
    return classes;
}
}
