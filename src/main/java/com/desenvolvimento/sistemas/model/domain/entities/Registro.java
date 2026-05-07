package com.desenvolvimento.sistemas.model.domain.entities;

import com.desenvolvimento.sistemas.model.domain.IEntity;
import java.util.Date;

public class Registro implements IEntity {
    private Long id;
    private Long idUsuario;
    private String nome;
    private Long status;
    private java.util.Date dataRegistro;

    public Registro() {}
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Long idUsuario) { this.idUsuario = idUsuario; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public Long getStatus() { return status; }
    public void setStatus(Long status) { this.status = status; }
    public Date getDataRegistro() { return dataRegistro; }
    public void setDataRegistro(java.util.Date dataRegistro) { this.dataRegistro = dataRegistro; }
    @Override
    public String toString() {
        return "Registro [id=" + id + ", idUsuario=" + idUsuario + ", nome=" + nome + ", status=" + status
                + ", dataRegistro=" + dataRegistro + "]";
    }
}
