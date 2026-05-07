package com.desenvolvimento.sistemas.model.domain.entities;

import java.math.BigDecimal;

import com.desenvolvimento.sistemas.model.domain.IEntity;

public class Acessorio implements IEntity {
    private Long          id;
    private String        nome;
    private BigDecimal    valorAdicional;
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
    public BigDecimal getValorAdicional() {
        return valorAdicional;
    }
    public void setValorAdicional(BigDecimal valorAdicional) {
        this.valorAdicional = valorAdicional;
    }
    @Override
    public String toString() {
        return "Acessorio: " + nome + ", R$" + valorAdicional;
    }
    

}
