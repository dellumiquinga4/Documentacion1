package com.banquito.Documentacion.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class CrearPrestamoRequest {
    private String idCliente;
    private String idPrestamo;
    private BigDecimal montoSolicitado;
    private Integer plazoMeses;

    // + constructor
    public CrearPrestamoRequest() {
    }

    public CrearPrestamoRequest(String idCliente, String idPrestamo, BigDecimal montoSolicitado, Integer plazoMeses) {
        this.idCliente = idCliente;
        this.idPrestamo = idPrestamo;
        this.montoSolicitado = montoSolicitado;
        this.plazoMeses = plazoMeses;
    }

}
