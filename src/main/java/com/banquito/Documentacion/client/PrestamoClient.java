package com.banquito.Documentacion.client;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import com.banquito.Documentacion.dto.CrearPrestamoRequest;
import com.banquito.Documentacion.dto.PrestamoClienteDTO;

@FeignClient(name = "prestamoClient", url = "${prestamo.service.url}")
public interface PrestamoClient {
  @PostMapping("/api/prestamos/v1/prestamos-clientes")
  PrestamoClienteDTO crearPrestamo(@RequestBody CrearPrestamoRequest req);
}
