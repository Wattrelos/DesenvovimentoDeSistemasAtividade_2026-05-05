-- phpMyAdmin SQL Dump
-- version 5.2.2deb1+deb13u1
-- https://www.phpmyadmin.net/
--
-- Host: localhost:3306
-- Tempo de geração: 06/05/2026 às 21:44
-- Versão do servidor: 11.8.3-MariaDB-0+deb13u1 from Debian
-- Versão do PHP: 8.4.16

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Banco de dados: `locadora`
--

-- --------------------------------------------------------

--
-- Estrutura para tabela `table_acessorio`
--

CREATE TABLE IF NOT EXISTS `table_acessorio` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `nome` varchar(50) DEFAULT NULL,
  `valor_adicional` decimal(10,2) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Despejando dados para a tabela `table_acessorio`
--

INSERT IGNORE INTO `table_acessorio` (`id`, `nome`, `valor_adicional`) VALUES
(1, 'GPS Garmin', 15.00),
(2, 'Cadeira de Bebê', 25.00),
(3, 'Wi-Fi Móvel', 30.00),
(4, 'Rack de Teto', 40.00),
(5, 'Seguro para Terceiros', 50.00);

-- --------------------------------------------------------

--
-- Estrutura para tabela `table_categoria`
--

CREATE TABLE IF NOT EXISTS `table_categoria` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `nome` varchar(50) NOT NULL,
  `valor_diaria` decimal(10,2) NOT NULL,
  `descricao` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `nome` (`nome`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Despejando dados para a tabela `table_categoria`
--

INSERT IGNORE INTO `table_categoria` (`id`, `nome`, `valor_diaria`, `descricao`) VALUES
(1, 'Econômico', 110.00, 'Carros populares com motor 1.0 e ar-condicionado.'),
(2, 'Intermediário', 180.00, 'Sedans ou hatches com motor 1.6 e mais espaço.'),
(3, 'SUV', 320.00, 'Utilitários esportivos, ideais para viagens em família.'),
(4, 'Premium', 550.00, 'Carros de luxo ou executivos com câmbio automático.');

-- --------------------------------------------------------

--
-- Estrutura para tabela `table_cliente`
--

CREATE TABLE IF NOT EXISTS `table_cliente` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `nome` varchar(100) NOT NULL,
  `cpf_cnpj` varchar(14) NOT NULL,
  `cnh` varchar(15) DEFAULT NULL,
  `email` varchar(100) DEFAULT NULL,
  `telefone` varchar(15) DEFAULT NULL,
  `data_cadastro` timestamp NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`),
  UNIQUE KEY `cpf_cnpj` (`cpf_cnpj`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Despejando dados para a tabela `table_cliente`
--

INSERT IGNORE INTO `table_cliente` (`id`, `nome`, `cpf_cnpj`, `cnh`, `email`, `telefone`, `data_cadastro`) VALUES
(1, 'João Silva', '12345678901', '9876543210', 'joao.silva@email.com', '(11) 98888-7777', '2026-05-06 15:18:47'),
(2, 'Maria Oliveira', '98765432100', '1234567890', 'maria.oli@email.com', '(21) 97777-6666', '2026-05-06 15:18:47'),
(3, 'Empresa LocaTudo Ltda', '12345678000199', NULL, 'contato@locatudo.com', '(31) 3333-4444', '2026-05-06 15:18:47'),
(4, 'Carlos Eduardo', '45612378955', '5544332211', 'cadu@email.com', '(41) 99911-2233', '2026-05-06 15:18:47');

-- --------------------------------------------------------

--
-- Estrutura para tabela `table_locacao`
--

CREATE TABLE IF NOT EXISTS `table_locacao` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `cliente_id` bigint(20) NOT NULL,
  `veiculo_id` bigint(20) NOT NULL,
  `data_retirada` timestamp NOT NULL,
  `data_previsao_devolucao` timestamp NOT NULL,
  `valor_diaria_aplicado` decimal(10,2) NOT NULL,
  `status_locacao` varchar(20) DEFAULT 'ATIVA',
  PRIMARY KEY (`id`),
  KEY `fk_locacao_cliente` (`cliente_id`),
  KEY `fk_locacao_veiculo` (`veiculo_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Despejando dados para a tabela `table_locacao`
--

INSERT IGNORE INTO `table_locacao` (`id`, `cliente_id`, `veiculo_id`, `data_retirada`, `data_previsao_devolucao`, `valor_diaria_aplicado`, `status_locacao`) VALUES
(1, 1, 1, '2024-05-01 13:00:00', '2024-05-05 13:00:00', 150.00, 'ATIVA'),
(2, 2, 3, '2024-05-02 17:30:00', '2024-05-10 17:30:00', 280.00, 'ATIVA'),
(3, 4, 2, '2024-05-04 12:00:00', '2024-05-06 12:00:00', 120.00, 'ATIVA');

-- --------------------------------------------------------

--
-- Estrutura para tabela `table_locacao_acessorio`
--

CREATE TABLE IF NOT EXISTS `table_locacao_acessorio` (
  `locacao_id` bigint(20) NOT NULL,
  `acessorio_id` bigint(20) NOT NULL,
  PRIMARY KEY (`locacao_id`,`acessorio_id`),
  KEY `fk_m2m_acessorio` (`acessorio_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Despejando dados para a tabela `table_locacao_acessorio`
--

INSERT IGNORE INTO `table_locacao_acessorio` (`locacao_id`, `acessorio_id`) VALUES
(1, 1),
(3, 1),
(2, 2),
(1, 3),
(3, 3),
(1, 4),
(2, 4),
(3, 5);

-- --------------------------------------------------------

--
-- Estrutura para tabela `table_locacao_veiculo`
--

CREATE TABLE IF NOT EXISTS `table_locacao_veiculo` (
  `locacao_id` bigint(20) NOT NULL,
  `veiculo_id` bigint(20) NOT NULL,
  KEY `fk_locacao` (`locacao_id`),
  KEY `fk_veiculo` (`veiculo_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Despejando dados para a tabela `table_locacao_veiculo`
--

INSERT IGNORE INTO `table_locacao_veiculo` (`locacao_id`, `veiculo_id`) VALUES
(1, 1),
(2, 2),
(3, 3),
(4, 4);

-- --------------------------------------------------------

--
-- Estrutura para tabela `table_manutencao`
--

CREATE TABLE IF NOT EXISTS `table_manutencao` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `veiculo_id` bigint(20) NOT NULL,
  `descricao` varchar(255) DEFAULT NULL,
  `data_servico` date DEFAULT NULL,
  `valor_gasto` decimal(10,2) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_manutencao_veiculo` (`veiculo_id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Despejando dados para a tabela `table_manutencao`
--

INSERT IGNORE INTO `table_manutencao` (`id`, `veiculo_id`, `descricao`, `data_servico`, `valor_gasto`) VALUES
(1, 1, 'Troca de óleo e filtro', '2024-01-15', 250.00),
(2, 1, 'Alinhamento e balanceamento', '2024-03-10', 180.00),
(3, 2, 'Revisão de 10.000km', '2024-02-20', 850.00),
(4, 3, 'Reparo no sistema de freios', '2024-04-05', 1200.00);

-- --------------------------------------------------------

--
-- Estrutura para tabela `table_principal`
--

CREATE TABLE IF NOT EXISTS `table_principal` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `nome` varchar(80) DEFAULT NULL COMMENT 'Nome completo',
  `descricao` text DEFAULT NULL COMMENT 'Descrição',
  `data_cadastro` timestamp NOT NULL DEFAULT current_timestamp() COMMENT 'Data de Cadastro',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;

--
-- Despejando dados para a tabela `table_principal`
--

INSERT IGNORE INTO `table_principal` (`id`, `nome`, `descricao`, `data_cadastro`) VALUES
(1, 'João Batista', 'Cliente preferencial', '2026-05-05 13:43:51'),
(2, 'Dr. Matheus', 'Dar desconteos de até 10%', '2026-05-05 13:46:33'),
(3, 'Lucar Escritor', 'Revisão semestral.', '2026-05-05 13:46:33');

-- --------------------------------------------------------

--
-- Estrutura para tabela `table_registro`
--

CREATE TABLE IF NOT EXISTS `table_registro` (
  `id` bigint(20) NOT NULL COMMENT 'Chave primária',
  `id_usuario` bigint(20) NOT NULL COMMENT 'Chave estrangeira',
  `nome` varchar(80) DEFAULT NULL COMMENT 'Nome completo',
  `status` int(11) NOT NULL COMMENT 'Status',
  `data_registro` timestamp NOT NULL DEFAULT current_timestamp() COMMENT 'Data de Cadastro',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;

-- --------------------------------------------------------

--
-- Estrutura para tabela `table_usuario`
--

CREATE TABLE IF NOT EXISTS `table_usuario` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `nome_usuario` varchar(80) DEFAULT NULL COMMENT 'Nome completo',
  `senha` varchar(256) DEFAULT NULL COMMENT 'Endereço de email',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;

--
-- Despejando dados para a tabela `table_usuario`
--

INSERT IGNORE INTO `table_usuario` (`id`, `nome_usuario`, `senha`) VALUES
(1, 'JSilva', 'CF83E1357EEFB8BDF1542850D66D8007D620E4050B5715DC83F4A921D36CE9CE47D0D13C5D85F2B0FF8318D2877EEC2F63B931BD47417A81A538327AF927DA3E'),
(2, 'MariaOliveria', '0CA46B9A2C7704E7A790498D05CA4A997B2055A5CAD6BEB8F3269B09149C9C3C54F70EC179CD0F2EFAD1D9DC5B26F4002ACA11FBBF0D2948B1AC61F7252DEDF9'),
(3, 'LocaTudo', '225E107C2016DDBBCD590D4C2F3E913AF9058D3BF818975A69178720188CFE3DC6D9860DCD8AAC14E72770E980E84D278C4970FC58D4E02C3FF0F46FE29C94B2'),
(4, 'CarlosEduardo', '225E107C2016DDBBCD590D4C2F3E913AF9058D3BF818975A69178720188CFE3DC6D9860DCD8AAC14E72770E980E84D278C4970FC58D4E02C3FF0F46FE29C94B2');

-- --------------------------------------------------------

--
-- Estrutura para tabela `table_veiculo`
--

CREATE TABLE IF NOT EXISTS `table_veiculo` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `placa` varchar(10) NOT NULL,
  `modelo` varchar(50) NOT NULL,
  `marca` varchar(50) NOT NULL,
  `ano` int(11) NOT NULL,
  `cor` varchar(20) DEFAULT NULL,
  `km_atual` bigint(20) DEFAULT 0,
  `valor_diaria_padrao` decimal(10,2) NOT NULL,
  `status_veiculo` varchar(20) DEFAULT 'DISPONIVEL',
  `tipo_combustivel` varchar(20) DEFAULT NULL,
  `categoria_id` bigint(20) DEFAULT NULL,
  `descricao` text NOT NULL DEFAULT ' ',
  PRIMARY KEY (`id`),
  UNIQUE KEY `placa` (`placa`),
  KEY `fk_veiculo_categoria` (`categoria_id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Despejando dados para a tabela `table_veiculo`
--

INSERT IGNORE INTO `table_veiculo` (`id`, `placa`, `modelo`, `marca`, `ano`, `cor`, `km_atual`, `valor_diaria_padrao`, `status_veiculo`, `tipo_combustivel`, `categoria_id`, `descricao`) VALUES
(1, 'ABC-1234', 'Onix', 'Chevrolet', 2023, 'Branco', 15000, 120.00, 'DISPONIVEL', 'FLEX', 1, ' '),
(2, 'XYZ-9876', 'Compass', 'Jeep', 2024, 'Preto', 5000, 350.00, 'LOCADO', 'DIESEL', 3, ' '),
(3, 'KJG-5544', 'HB20', 'Hyundai', 2022, 'Prata', 45000, 110.00, 'MANUTENCAO', 'FLEX', 3, ' '),
(4, 'EV-1010', 'Dolphin', 'BYD', 2024, 'Cinza', 1200, 450.00, 'DISPONIVEL', 'ELETRICO', 2, ' ');
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
