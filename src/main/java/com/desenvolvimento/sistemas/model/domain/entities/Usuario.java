package com.desenvolvimento.sistemas.model.domain.entities;

import com.desenvolvimento.sistemas.model.domain.IEntity;



public class Usuario implements IEntity {

    private Long id;
    private String nomeUsuario;
    private String senha;

    public Usuario() {}

    public Long   getId() { return id; }
    public void   setId(Long id) { this.id = id; }
    public String getNomeUsuario() { return nomeUsuario; }
    public void   setNomeUsuario(String nomeUsuario) { this.nomeUsuario = nomeUsuario; }
    public String getSenha() { return senha; }
    public void   setSenha(String senha) { this.senha = senha; }

    @Override
    public String toString() {
        return "Usuario [id=" + id + ", nomeUsuario=" + nomeUsuario + ", senha=" + senha + "]";
    }

}
