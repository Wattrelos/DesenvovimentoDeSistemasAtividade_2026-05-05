package com.desenvolvimento.sistemas.model.domain.entities;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.desenvolvimento.sistemas.model.domain.IEntity;

// Tabela Ficha de Locação
public class Locacao implements IEntity {
    private  Long id;
    private  Long usuarioId;
    private  java.util.Date dataRegistro;
    private  Object status;
    private  List<Veiculo> veiculo = new ArrayList<>();

    public Locacao() {}
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }
    public Date getDataRegistro() { return dataRegistro; }
    public void setDataRegistro(java.util.Date dataRegistro) { this.dataRegistro = dataRegistro; }
    public Object getStatus() { return status; }
    public void setStatus(Object status) { this.status = status; }
    public List<Veiculo> getVeiculo() {
        return veiculo;
    }
    public void setVeiculo(List<Veiculo> veiculo) {
        this.veiculo = veiculo;
    }
    
}
