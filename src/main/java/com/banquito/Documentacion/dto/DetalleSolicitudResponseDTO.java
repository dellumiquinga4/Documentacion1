package com.banquito.Documentacion.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class DetalleSolicitudResponseDTO {
  private Long   idSolicitud;
  private String numeroSolicitud;

    // Nuevos campos:
  private String cedulaSolicitante;
  private String nombresSolicitante;
  private String placaVehiculo;

  private String idPrestamo;
  private BigDecimal montoSolicitado;
  private Integer plazoMeses;

}
