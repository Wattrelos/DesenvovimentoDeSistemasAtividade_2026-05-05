package com.desenvolvimento.sistemas.controller;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.desenvolvimento.sistemas.model.dataAccessObject.DataAccessObject;
import com.desenvolvimento.sistemas.model.domain.IEntity;
import com.desenvolvimento.sistemas.model.domain.factory.SimpleObjectFactory;

@Controller
public class ClienteController {
    
    @GetMapping("/welcome")
    public String welcome(Model model) {
        model.addAttribute("totalUsuarios", 150);
        DataAccessObject dao = new DataAccessObject();
        model.addAttribute("clientes", dao.read(SimpleObjectFactory.create("Cliente"))); // Exemplo
        return "home"; // Vai carregar com Aside
    }
    

    @GetMapping("/clientes")
    public String listarClientes(Model model) {
        DataAccessObject dao = new DataAccessObject();
        // Carrega a lista de clientes (seu método read já preenche as associações recursivamente)
        List<IEntity> clientes = dao.read(SimpleObjectFactory.create("Cliente")); 
        
        model.addAttribute("clientes", clientes);
        return "clientes"; // nome do arquivo html
    }
    @GetMapping("/dinamico")
    public String listarDinamico(Model model) {
        DataAccessObject dao = new DataAccessObject();
        List<IEntity> lista = dao.read(SimpleObjectFactory.create("Cliente"));
        
        if (!lista.isEmpty()) {
            // Obtém os nomes dos campos da classe via Reflection
            Field[] fields = lista.get(0).getClass().getDeclaredFields();
            List<String> nomesColunas = Arrays.stream(fields)
                .map(Field::getName)
                .toList();
                
            model.addAttribute("colunas", nomesColunas);
            model.addAttribute("dados", lista);
        }
        return "dinamico";
    }
    @GetMapping("/sobre-nos")
    public String sobreNos(Model model) {
        model.addAttribute("sobre-nos");
        return "sobre-nos"; // nome do arquivo html
    }
}

