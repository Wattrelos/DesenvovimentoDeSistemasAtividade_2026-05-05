package com.desenvolvimento.sistemas.model.domain.entities;

import com.desenvolvimento.sistemas.model.domain.IEntity;
import java.util.Date;

public class Veiculo implements IEntity {
    private Long id;
    private String modelo;
    private String descricao;
    private String placa;
    private java.util.Date dataCadastro;

    public Veiculo() {}
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getModelo() { return modelo; }
    public void setModelo(String modelo) { this.modelo = modelo; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public String getPlaca() { return placa; }
    public void setPlaca(String placa) { this.placa = placa; }
    public Date getDataCadastro() { return dataCadastro; }
    public void setDataCadastro(java.util.Date dataCadastro) { this.dataCadastro = dataCadastro; }
}




