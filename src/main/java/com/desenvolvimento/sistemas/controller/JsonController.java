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

import jakarta.servlet.http.HttpServletRequest;

@RestController
public class JsonController {
        // Instancia uma única vez para este controller
    private final DataAccessObject dataAccessObject = new DataAccessObject();

    @PostMapping("/create-json") // Operações de escrita devem ser POST
    public ResponseEntity<?> create(@RequestParam(value = "entity", required = false) String entityName, HttpServletRequest request) {

        if (entityName != null && !entityName.trim().isEmpty()) {
            IEntity entidade = SimpleObjectFactory.create(entityName);
            
            // Persiste no banco e recupera a PK
            Long primaryKey = dataAccessObject.create(EntityMapper.fillEntity(entidade, request));

            // Retorna a chave primária no corpo da resposta
            return ResponseEntity.ok(primaryKey);

        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body("<h1>Nenhuma entidade especificada!</h1>");
        }
    }

    @GetMapping("/read-json")
    public ResponseEntity<?> read(@RequestParam(value = "entity", required = false) String entityName, HttpServletRequest request) {
        
        if (entityName != null && !entityName.trim().isEmpty()) {
            // Lógica de negócio mantida
            IEntity entidade = SimpleObjectFactory.create(entityName);
            DataAccessObject dataAccessObject = new DataAccessObject();
            List<IEntity> listaEntity = dataAccessObject.read(EntityMapper.fillEntity(entidade, request));

            // O Spring converte a lista automaticamente para JSON usando o Jackson interno
            return ResponseEntity.ok(listaEntity);
        } else {
            // Retorna HTML ou uma mensagem de erro apropriada
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body("<h1>Nenhum valor recebido!</h1>");
        }
    }

    @PutMapping("/update-json") // Padrão REST para atualizações
    public ResponseEntity<?> update(@RequestParam(value = "entity", required = false) String entityName, HttpServletRequest request) {

        if (entityName != null && !entityName.trim().isEmpty()) {
            IEntity entidade = SimpleObjectFactory.create(entityName);
            DataAccessObject dataAccessObject = new DataAccessObject();
            
            // O EntityMapper continua funcionando com o HttpServletRequest que o Spring injeta
            Long primaryKey = dataAccessObject.update(EntityMapper.fillEntity(entidade, request));

            System.out.println("Update: Retornou de dataAccessObject.update");
            System.out.println("Update: Long = " + primaryKey);

            return ResponseEntity.ok(primaryKey);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body("<h1>Nenhuma chave primária recebida!</h1>");
        }
    }
    
    @DeleteMapping("/delete-json") // Padrão REST para exclusões
    public ResponseEntity<?> delete(@RequestParam(value = "entity", required = false) String entityName, HttpServletRequest request) {

        if (entityName != null && !entityName.trim().isEmpty()) {
            IEntity entidade = SimpleObjectFactory.create(entityName);
            DataAccessObject dataAccessObject = new DataAccessObject();
            
            // O EntityMapper extrai os IDs ou dados necessários do request
            Long primaryKey = dataAccessObject.delete(EntityMapper.fillEntity(entidade, request));

            System.out.println("Delete: Retornou de dataAccessObject.delete");
            System.out.println("Delete: Chave primária Long = " + primaryKey + " da entidade excluída.");

            // Retornamos a PK da entidade deletada
            return ResponseEntity.ok(primaryKey);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body("<h1>Nenhuma chave primária recebida!</h1>");
        }
    }
    
}
