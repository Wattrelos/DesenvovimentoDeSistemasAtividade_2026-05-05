package com.desenvolvimento.sistemas.model.domain.entities;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.desenvolvimento.sistemas.model.domain.IEntity;

import jakarta.persistence.OneToMany;

public class Veiculo implements IEntity {
    private Long          id;
    private String        modelo;
    private String        marca;
    private int           ano;
    private String        cor;
    private Long          kmAtual;
    private BigDecimal    valorDiariaPadrao;
    private String        descricao;
    private String        placa;
    private StatusVeiculo statusVeiculo;
    //Coleções:
    private Categoria     categoriaId; // Por padrão, se busca a classe pelo ID.
    @OneToMany
    private List<Manutencao> listaManutencoes = new ArrayList<>();

    // Construtores:
    public Veiculo() {}

    // Métodos
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getModelo() { return modelo; }
    public void setModelo(String modelo) { this.modelo = modelo; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public String getPlaca() { return placa; }
    public void setPlaca(String placa) { this.placa = placa; }

    public String getMarca() {
        return marca;
    }
    public void setMarca(String marca) {
        this.marca = marca;
    }
    public int getAno() {
        return ano;
    }
    public void setAno(int ano) {
        this.ano = ano;
    }
    public String getCor() {
        return cor;
    }
    public void setCor(String cor) {
        this.cor = cor;
    }
    public Long getKmAtual() {
        return kmAtual;
    }
    public void setKmAtual(Long kmAtual) {
        this.kmAtual = kmAtual;
    }
    public StatusVeiculo getStatusVeiculo() {
        return statusVeiculo;
    }
    public void setStatusVeiculo(StatusVeiculo statusVeiculo) {
        this.statusVeiculo = statusVeiculo;
    }
        public Categoria getCategoriaId() {
        return categoriaId;
    }
    public void setCategoriaId(Categoria categoriaId) {
        this.categoriaId = categoriaId;
    }

    public BigDecimal getValorDiariaPadrao() {
        return valorDiariaPadrao;
    }

    public void setValorDiariaPadrao(BigDecimal valorDiariaPadrao) {
        this.valorDiariaPadrao = valorDiariaPadrao;
    }


    // Definição do Enum
    public enum StatusVeiculo {
        DISPONIVEL,
        LOCADO,
        MANUTENCAO,
        ALIENADO;
    }


    public List<Manutencao> getListaManutencoes() {
        return listaManutencoes;
    }

    public void setListaManutencoes(List<Manutencao> listaManutencoes) {
        this.listaManutencoes = listaManutencoes;
    }

    @Override
    public String toString() {
        return modelo;
    }

}




