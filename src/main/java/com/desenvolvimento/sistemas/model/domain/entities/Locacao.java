package com.desenvolvimento.sistemas.model.domain.entities;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.desenvolvimento.sistemas.model.domain.IEntity;

import jakarta.persistence.ManyToMany;

// Tabela Ficha de Locação
public class Locacao implements IEntity {
    private  Long          id;
    private  LocalDateTime dataRetirada;
    private  LocalDateTime dataPrevisaoDevolucao;
    private  BigDecimal    valorDiariaAplicado;
    private  StatusLocacao statusLocacao;

    // Coleções:
    private Cliente clienteId; // Impostante, pois se buscará primeiro o ID da cliente_id na tabela do banco de dados
    private Veiculo veiculoId; // Idem; veiculo_id (FK)
    @ManyToMany
    private List<Acessorio> acessorio = new ArrayList<>();

    // Construtores:
    public Locacao() {}

    // Métodos:
    public Long getId() { return id; }
    @Override
    public String toString() {
        return "Locacao [id=" + id + ", dataRetirada=" + dataRetirada + ", dataPrevisaoDevolucao="
                + dataPrevisaoDevolucao + ", valorDiariaAplicado=" + valorDiariaAplicado + ", statusLocacao="
                + statusLocacao + ", clienteId=" + clienteId + ", veiculoId=" + veiculoId + ", acessorio=" + acessorio
                + "]";
    }

    public void setId(Long id) { this.id = id; }
    public LocalDateTime getDataRetirada() {
        return dataRetirada;
    }
    public void setDataRetirada(LocalDateTime dataRetirada) {
        this.dataRetirada = dataRetirada;
    }
    public LocalDateTime getDataPrevisaoDevolucao() {
        return dataPrevisaoDevolucao;
    }
    public void setDataPrevisaoDevolucao(LocalDateTime dataPrevisaoDevolucao) {
        this.dataPrevisaoDevolucao = dataPrevisaoDevolucao;
    }
    public BigDecimal getValorDiariaAplicado() {
        return valorDiariaAplicado;
    }
    public void setValorDiariaAplicado(BigDecimal valorDiariaAplicado) {
        this.valorDiariaAplicado = valorDiariaAplicado;
    }
    public StatusLocacao getStatusLocacao() {
        return statusLocacao;
    }
    public void setStatusLocacao(StatusLocacao statusLocacao) {
        this.statusLocacao = statusLocacao;
    }
    public Cliente getClienteId() {
        return clienteId;
    }
    public void setClienteId(Cliente clienteId) {
        this.clienteId = clienteId;
    }
    public Veiculo getVeiculoId() {
        return veiculoId;
    }
    public void setVeiculoId(Veiculo veiculoId) {
        this.veiculoId = veiculoId;
    }
    public StatusLocacao getStatus() { return statusLocacao; }
    public void setStatus(StatusLocacao statusLocacao) { this.statusLocacao = statusLocacao; }

    public List<Acessorio> getAcessorio() {
        return acessorio;
    }
    public void setAcessorio(List<Acessorio> acessorio) {
        this.acessorio = acessorio;
    }

    // Definição do Enum
    public enum StatusLocacao {
        RESERVADA,
        ATIVA,
        FINALIZADA,
        CANCELADA;
    }

    public BigDecimal calcularValorTotal() {
        if (dataRetirada == null || dataPrevisaoDevolucao == null || valorDiariaAplicado == null) {
            return BigDecimal.ZERO;
        }

        // 1. Calcula a duração entre as datas
        Duration duracao = Duration.between(dataRetirada, dataPrevisaoDevolucao);
        
        // 2. Transforma em dias (ceil/teto: se passou 1 hora, já conta como nova diária)
        // Se preferir diárias fechadas de 24h, use toDays()
        long dias = duracao.toDays();
        long horasRestantes = duracao.toHours() % 24;

        // Lógica comum em locadoras: se houver fração de horas, cobra-se uma nova diária
        if (horasRestantes > 0 || duracao.toMinutes() % 60 > 0) {
            dias++;
        }
        
        // Se a locação durar menos de 24h, mas houver tempo, garante ao menos 1 diária
        if (dias == 0 && !duracao.isZero()) {
            dias = 1;
        }

        // 3. Multiplica pelo valor da diária usando BigDecimal
        return valorDiariaAplicado.multiply(new BigDecimal(dias));
    }


}
