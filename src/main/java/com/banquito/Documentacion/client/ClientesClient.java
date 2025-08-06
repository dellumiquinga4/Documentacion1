// src/main/java/com/banquito/Documentacion/client/ClientesClient.java
package com.banquito.Documentacion.client;

import com.banquito.Documentacion.dto.ClienteDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "clientesClient", url = "${cliente.service.url}/api/v1/clientes")
public interface ClientesClient {
  /**
   * Llama a GET /api/v1/clientes?tipoIdentificacion=CEDULA&numeroIdentificacion=0102020202
   * y devuelve una lista (normalmente con un único elemento).
   */
  @GetMapping("/clientes")
  List<ClienteDTO> findByIdentificacion(
    @RequestParam("tipoIdentificacion") String tipoIdentificacion,
    @RequestParam("numeroIdentificacion") String numeroIdentificacion
  );
}
