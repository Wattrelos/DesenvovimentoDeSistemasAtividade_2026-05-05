package com.desenvolvimento.sistemas.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.desenvolvimento.sistemas.model.dataAccessObject.DataAccessObject;
import com.desenvolvimento.sistemas.model.dataTransferObject.EntityMapper;
import com.desenvolvimento.sistemas.model.domain.IEntity;
import com.desenvolvimento.sistemas.model.domain.factory.SimpleObjectFactory;

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

    @GetMapping("/listar/{entity}")
    public String listar(@PathVariable("entity") String entityName, HttpServletRequest request, Model model) {
        IEntity entidadeBase = SimpleObjectFactory.create(entityName);
        IEntity filtro = EntityMapper.fillEntity(entidadeBase, request); // request para preencher os atributos das classes com valores, que serão utilizado para formar o WHERE. Se requeste não tiver valores, traz todos os registros.
        List<IEntity> resultados = dao.read(filtro); // Traz todos os registros ou os encontrados no critério de busca.

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

    @GetMapping("/detalhe/{entity}")
    public String detalhe(@PathVariable("entity") String entityName, HttpServletRequest request, Model model) {
        // 1. Pega o ID do request (parâmetro da URL ou form)
        String idParam = request.getParameter("id");
        
        // 2. Tenta converter com segurança
        Long entityId = null;
        try {
            if (idParam != null && !idParam.isBlank()) {
                entityId = Long.valueOf(idParam.trim());
            }
        } catch (NumberFormatException e) {
            // ID inválido (ex: "abc")
        }

        if (entityName != null && !entityName.isBlank() && entityId != null) {
            IEntity entidadeBase = SimpleObjectFactory.create(entityName);
            
            // Preenche a entidade com os dados do request
            IEntity filtro = EntityMapper.fillEntity(entidadeBase, request); 
            
            List<IEntity> resultados = dao.read(filtro);

            // Reflexão para os cabeçalhos
            List<String> colunas = Arrays.stream(entidadeBase.getClass().getDeclaredFields())
                                        .map(Field::getName)
                                        .toList();

            model.addAttribute("detalhe", resultados);
            model.addAttribute("colunas", colunas);
            model.addAttribute("entidadeNome", entityName);
            
            return "detalhe";
        }
        
        return "error"; // Ou redirecionamento para lista
    }

}
