package com.desenvolvimento.sistemas.model.domain.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.desenvolvimento.sistemas.model.domain.IEntity;

public class Manutencao implements IEntity {

    private Long          id;
    private Long          veiculoId;
    private LocalDateTime dataServico;
    private String        descricao;
    private BigDecimal    valorGasto;
    

    // Métodos;
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public LocalDateTime getDataServico() {
        return dataServico;
    }
    public void setDataServico(LocalDateTime dataServico) {
        this.dataServico = dataServico;
    }
    public String getDescricao() {
        return descricao;
    }
    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
    @Override
    public String toString() {
        return "Manutencao [id=" + id + ", veiculoId=" + veiculoId + ", dataServico=" + dataServico + ", descricao="
                + descricao + ", valorGasto=" + valorGasto + "]";
    }
    public BigDecimal getValorGasto() {
        return valorGasto;
    }
    public void setValorGasto(BigDecimal valorGasto) {
        this.valorGasto = valorGasto;
    }
    public Long getVeiculoId() {
        return veiculoId;
    }
    public void setVeiculoId(Long veiculoId) {
        this.veiculoId = veiculoId;
    }

}
