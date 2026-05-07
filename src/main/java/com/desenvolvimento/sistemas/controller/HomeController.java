package com.desenvolvimento.sistemas.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.desenvolvimento.sistemas.AppConfig;


@Controller
public class HomeController {

    // Mapeia tanto "/" quanto "/index.html" (se configurado) para a home
    @GetMapping({"/", "/home"})
    public String listarEntidades(Model model) throws IOException {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        
        // Converte o pacote (com pontos) em caminho de recurso (com barras)
        String packagePath = AppConfig.ENTITIES_PATH.replace(".", "/");
        
        // Busca todos os arquivos .class no pacote
        Resource[] resources = resolver.getResources("classpath*:" + packagePath + "/*.class");

        List<String> nomesClasses = new ArrayList<>();
        for (Resource resource : resources) {
            String filename = resource.getFilename();
            if (filename != null) {
                String className = filename.replace(".class", "");
                
                try {
                    // Monta o nome completo: pacote + nome da classe
                    String fullClassName = AppConfig.ENTITIES_PATH + "." + className;
                    
                    // Carrega a classe para verificar o tipo
                    Class<?> clazz = Class.forName(fullClassName);
                    
                    // Adiciona apenas se NÃO for um Enum
                    if (!clazz.isEnum()) {
                        nomesClasses.add(className);
                    }
                } catch (ClassNotFoundException e) {
                    // Logar erro se necessário
                }
            }
        }
        model.addAttribute("entidades", nomesClasses);
        return "home";
    }

}
