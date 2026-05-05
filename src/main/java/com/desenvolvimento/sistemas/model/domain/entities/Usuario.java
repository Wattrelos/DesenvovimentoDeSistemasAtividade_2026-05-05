package com.desenvolvimento.sistemas.model.domain.entities;

import java.util.ArrayList;
import java.util.List;

import com.desenvolvimento.sistemas.model.domain.IEntity;

public class Usuario implements IEntity {

    private Long id;
    private String nomeUsuario;
    private String email;
    private String telefone;

    private List<Locacao> locacao = new ArrayList<>();

    public Usuario() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNomeUsuario() { return nomeUsuario; }
    public void setNomeUsuario(String nomeUsuario) { this.nomeUsuario = nomeUsuario; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public List<Locacao> getLocacao() {
        return locacao;
    }

    public void setLocacao(List<Locacao> locacao) {
        this.locacao = locacao;
    }
}
