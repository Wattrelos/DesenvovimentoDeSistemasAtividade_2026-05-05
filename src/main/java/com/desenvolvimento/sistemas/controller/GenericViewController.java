package com.desenvolvimento.sistemas.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping; // Para o @PutMapping
import org.springframework.web.bind.annotation.DeleteMapping; // Para o @DeleteMapping
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.desenvolvimento.sistemas.model.dataAccessObject.DataAccessObject;
import com.desenvolvimento.sistemas.model.dataTransferObject.EntityMapper;
import com.desenvolvimento.sistemas.model.domain.IEntity;
import com.desenvolvimento.sistemas.model.domain.factory.SimpleObjectFactory;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import java.util.List; // Para o List
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import java.util.Arrays;
import java.lang.reflect.Field;

import jakarta.servlet.http.HttpServletRequest;

/* Por que essa solução é ideal para este caso:
* Filtro Automático: Como o seu dao.read já ignora nulos, se o usuário chamar /listar?entity=Cliente&nome=João, o DAO fará um WHERE nome = 'João'. Se chamar apenas /listar?entity=Cliente, ele trará todos.
* Herança e Coleções: Como o SimpleObjectFactory e o EntityMapper já tratam a complexidade dos objetos, o Controller atua apenas como um "maestro", sem precisar saber se está lidando com um Cliente ou um Fornecedor.
* Manutenção Zero: Se você criar uma nova classe Transportadora amanhã, não precisa mexer no Java. Basta acessar /listar?entity=Transportadora, por exemplo.
*/

@Controller
public class GenericViewController {

    private final DataAccessObject dao = new DataAccessObject();

    @GetMapping("/listar")
    public String listar(@RequestParam("entity") String entityName, HttpServletRequest request, Model model) {
        IEntity entidadeBase = SimpleObjectFactory.create(entityName);
        IEntity filtro = EntityMapper.fillEntity(entidadeBase, request);
        List<IEntity> resultados = dao.read(filtro);

        // Extrai os nomes dos atributos da classe para usar como cabeçalho
        Field[] campos = entidadeBase.getClass().getDeclaredFields();
        List<String> colunas = Arrays.stream(campos)
                                    .map(Field::getName)
                                    .collect(Collectors.toList());

        model.addAttribute("lista", resultados);
        model.addAttribute("colunas", colunas);
        model.addAttribute("entidadeNome", entityName);
        return "listagem-dinamica";
    }

}
