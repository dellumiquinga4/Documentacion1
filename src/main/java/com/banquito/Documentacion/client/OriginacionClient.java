package com.banquito.Documentacion.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import com.banquito.Documentacion.dto.DetalleSolicitudResponseDTO;

@FeignClient(name = "originacionClient", url = "${originacion.service.url}/api/v1/solicitudes")
public interface OriginacionClient {
    @GetMapping("/{numeroSolicitud}/detalle")
    DetalleSolicitudResponseDTO obtenerDetalle(@PathVariable String numeroSolicitud);

    @PostMapping("/{idSolicitud}/cambiar-estado")
    void cambiarEstado(
            @PathVariable Long idSolicitud,
            @RequestParam String nuevoEstado,
            @RequestParam String motivo,
            @RequestParam String usuario);
}
