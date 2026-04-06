package org.autoservicio.backendcontratoservicio.service.gestionclientes;

import org.autoservicio.backendcontratoservicio.config.responseModel;
import org.autoservicio.backendcontratoservicio.model.gestionclientes.BuscarClientes;
import org.autoservicio.backendcontratoservicio.model.gestionclientes.ClientesModel;
import org.autoservicio.backendcontratoservicio.repository.gestionclientes.ClientesRepository;
import org.autoservicio.backendcontratoservicio.response.ClientesRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

@Service
public class ClientesService {
    @Autowired
    private ClientesRepository repo;

    public Mono<List<ClientesRequest>> listadoclientes() {
        return Mono.fromCallable(() -> this.repo.listadoclientes());
    }

    public Mono<responseModel> registrarclientes(Integer op, ClientesModel obj) {
        return Mono.fromCallable(() -> this.repo.registrarclientes(op,obj));
    }
    public Mono<List<BuscarClientes>> buscarClientes(String valorBuscar) {
        return Mono.fromCallable(() -> this.repo.buscarclientes(valorBuscar))
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<List<BuscarClientes>> buscarClientes(String valorBuscar, Integer estado) {
        return Mono.fromCallable(() -> {
            List<BuscarClientes> result = this.repo.buscarclientes(valorBuscar);
            if (estado == null) return result;
            return result.stream().filter(c -> estado.equals(c.getEstareg())).toList();
        }).subscribeOn(Schedulers.boundedElastic());
    }



}
