package com.desenvolvimento.sistemas.model.domain.entities;

import java.math.BigDecimal;

import com.desenvolvimento.sistemas.model.domain.IEntity;

public class Categoria implements IEntity{
    private Long          id;
    private String        nome;
    private BigDecimal    valor_diaria;
    private String        descricao;
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getNome() {
        return nome;
    }
    public void setNome(String nome) {
        this.nome = nome;
    }
    public BigDecimal getValor_diaria() {
        return valor_diaria;
    }
    public void setValor_diaria(BigDecimal valor_diaria) {
        this.valor_diaria = valor_diaria;
    }
    public String getDescricao() {
        return descricao;
    }
    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
    @Override
    public String toString() {
        return nome;
    }
    

}
