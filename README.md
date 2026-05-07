Correções:
1) Alteração no Character set do banco de dados de utf8mb4_uca1400_ai_ci para utf8mb4_unicode_ci. Resolve o problema do XAMPP que usa banco de dados antigo no PC da Fatec.
2) Adicionar uma home "/"

Após a edição, temos um motor de persistência genérico (DAO) personalizado, com herança, associações complexas (1:N e N:N) e um sistema de validação automática (SchemaValidator) que protege a arquitetura do software.


   1. Banco de Dados: Normalizado com Categorias, Veículos, Manutenções e Acessórios;
   2. Segurança: O "Guardião" (Validator) impede erros de nomenclatura e esquecimento de anotações;
   3. Recursividade: O DAO carrega objetos dentro de objetos automaticamente.
   
   Arquitetura:
   1. Diagramas de Classe e Sequência estruturados;
   2. Backend: DAO genérico e dinâmico rodando em Java 17/21;
   3. Frontend: Listagem e Detalhes automatizados com Thymeleaf.

Próximo passo — como criar a interface gráfica ou implementar a lógica de devolução com multas.

Observação: Não lembro a versão do Java no PC da Fatec. Eu uso o Java 21, mas tenho um plano "B", caso seja o Java 17.