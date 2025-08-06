package com.banquito.Documentacion.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import com.banquito.Documentacion.dto.PersonaResponseDTO;

@FeignClient(name = "coreBancarioClient", url = "${cliente.service.url}")
public interface CoreBancarioClient {
  @GetMapping("/api/v1/clientes/personas/{tipoIdentificacion}/{numeroIdentificacion}")
  PersonaResponseDTO consultarPersonaPorIdentificacion(
    @PathVariable("tipoIdentificacion") String tipoIdentificacion,
    @PathVariable("numeroIdentificacion") String numeroIdentificacion
  );
}