package org.autoservicio.backendcontratoservicio.interfaces.gestionclientes;

import org.autoservicio.backendcontratoservicio.config.responseModel;
import org.autoservicio.backendcontratoservicio.model.gestionclientes.ClientesModel;
import org.autoservicio.backendcontratoservicio.model.gestionclientes.BuscarClientes;
import org.autoservicio.backendcontratoservicio.response.ClientesRequest;


import java.util.List;

public interface IClientes {
    List<ClientesRequest> listadoclientes();
    responseModel registrarclientes(Integer op, ClientesModel obj);
    List<BuscarClientes> buscarclientes(String valorBuscar);
    BuscarClientes buscarClientePorId(Integer id);
}
