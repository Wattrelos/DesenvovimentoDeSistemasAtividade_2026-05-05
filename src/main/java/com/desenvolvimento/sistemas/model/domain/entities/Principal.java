package com.desenvolvimento.sistemas.model.domain.entities;

import com.desenvolvimento.sistemas.model.domain.IEntity;
import java.util.Date;

public class Principal implements IEntity {
    private Long id;
    private String nome;
    private String descricao;
    private java.util.Date dataCadastro;

    public Principal() {}
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public Date getDataCadastro() { return dataCadastro; }
    public void setDataCadastro(java.util.Date dataCadastro) { this.dataCadastro = dataCadastro; }
}
