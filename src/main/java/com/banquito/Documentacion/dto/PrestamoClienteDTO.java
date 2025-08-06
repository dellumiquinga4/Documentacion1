package com.banquito.Documentacion.dto;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class PrestamoClienteDTO {
  private Integer id;
  private String idCliente;
  private String idPrestamo;
  private BigDecimal montoSolicitado;
  private Integer plazoMeses;
  // … cualquier otro campo que devuelva el servicio de transacciones
}
