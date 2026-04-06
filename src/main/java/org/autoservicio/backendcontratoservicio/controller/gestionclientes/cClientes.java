package org.autoservicio.backendcontratoservicio.controller.gestionclientes;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.autoservicio.backendcontratoservicio.config.genericModel;
import org.autoservicio.backendcontratoservicio.config.responseModel;
import org.autoservicio.backendcontratoservicio.excepciones.GenericoException;
import org.autoservicio.backendcontratoservicio.model.gestionclientes.BuscarClientes;
import org.autoservicio.backendcontratoservicio.model.gestionclientes.ClientesModel;
import org.autoservicio.backendcontratoservicio.response.ClientesRequest;
import org.autoservicio.backendcontratoservicio.service.gestionclientes.ClientesService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
public class cClientes {
    private final ClientesService service;

    @GetMapping("/listar")
    public Mono<ResponseEntity<genericModel<List<ClientesRequest>>>> obtener_listadoclientes() {
        return this.service.listadoclientes()
                .flatMap(GenericoException::success)
                .doOnSuccess(response -> log.info("Operación exitosa"))
                .doOnError((Throwable error) -> log.error("Error en Operación: {}", error.getMessage()))
                .onErrorResume(GenericoException::error);
    }
    @PostMapping("/registrar/{op}")
    public @ResponseBody Mono<ResponseEntity<genericModel<responseModel>>> registrarclientes(
            @PathVariable Integer op,
            @RequestBody ClientesModel form
    ) {
        return this.service.registrarclientes(op,form)
                .flatMap(GenericoException::success)
                .doOnSuccess(response -> log.info("Operación exitosa"))
                .doOnError((Throwable error) -> log.error("Error en Operación: {}", error.getMessage()))
                .onErrorResume(GenericoException::error);
    }
    @GetMapping("/buscar")
    public Mono<ResponseEntity<genericModel<List<BuscarClientes>>>> obtener_listadoclientes(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Integer estado) {
        String valorBuscar = (q == null) ? "" : q;
        return this.service.buscarClientes(valorBuscar, estado)
                .flatMap(GenericoException::success)
                .doOnSuccess(response ->
                        log.info("Operación exitosa: {} clientes encontrados",
                                response.getBody().getData().size()))
                .doOnError(error ->
                        log.error("Error en Operación: {}", error.getMessage()))
                .onErrorResume(GenericoException::error);
    }


}
