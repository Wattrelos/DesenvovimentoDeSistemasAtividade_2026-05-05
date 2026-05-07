package com.desenvolvimento.sistemas.model.dataTransferObject;

import java.lang.reflect.Method;
import java.math.BigDecimal;

import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class EntityMapper {

    // Formato padrão para LocalDateTime (ajuste conforme necessário)
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    /**
     * Preenche a entidade com base nos parâmetros do request com conversão de tipos.
     */
    public static <T> T fillEntity(T entity, HttpServletRequest request) {
        Class<?> clazz = entity.getClass();

        for (Method method : clazz.getMethods()) {
            // Focamos nos setters com 1 parâmetro
            if (method.getName().startsWith("set") && method.getParameterCount() == 1) {
                
                String propertyName = method.getName().substring(3);
                // Transforma setNome -> nome
                String fieldName = propertyName.substring(0, 1).toLowerCase() + propertyName.substring(1);
                String paramValue = request.getParameter(fieldName);
                // Só tenta preencher se o parâmetro existir no request e não for nulo/vazio
                // Isso significa que, se o usuário apagar um texto no formulário (enviando ""), o setter não será chamado. Como o setter não é chamado, o atributo da entidade permanece nulo
                // Se você quiser permitir que o usuário limpe um campo de texto, mude a verificação para permitir Strings vazias, mas bloqueie apenas o que realmente não veio no request:
                if (paramValue != null && !paramValue.trim().isEmpty()) {
                    try {
                        Class<?> parameterType = method.getParameterTypes()[0];
                        Object convertedValue = convertValue(paramValue, parameterType);
                        
                        if (convertedValue != null) {
                            method.invoke(entity, convertedValue);
                        }
                    } catch (Exception e) {
                        System.err.println("Erro ao popular campo " + fieldName + ": " + e.getMessage());
                        // Em produção, use um logger adequado
                    }
                }
            }
        }
        return entity;
    }

    /**
     * Converte a String do request para o tipo específico do método.
     * 
     * A partir do Java 17/21, você pode usar o switch diretamente com objetos Class.
     * Veja como o código ficaria muito mais "limpo" e fácil de ler:
     */
    private static Object convertValue(String value, Class<?> targetType) {
        if (value == null) return null;
            return switch (targetType) {
                case Class<?> t when t == String.class -> value;
                case Class<?> t when t ==     int.class || t == Integer.class -> Integer.parseInt(value);
                case Class<?> t when t ==    long.class || t ==    Long.class -> Long.parseLong(value);
                case Class<?> t when t ==  double.class || t ==  Double.class -> Double.parseDouble(value);
                case Class<?> t when t ==   float.class || t ==   Float.class -> Float.parseFloat(value);
                case Class<?> t when t == boolean.class || t == Boolean.class -> value.equalsIgnoreCase("true") || value.equalsIgnoreCase("on") || value.equals("1");
                case Class<?> t when t ==    BigDecimal.class -> new java.math.BigDecimal(value);
                case Class<?> t when t == LocalDateTime.class -> LocalDateTime.parse(value, DATE_FORMATTER);
                case Class<?> t when t ==     LocalDate.class -> LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE);
                default -> null;
        };
    }
}

