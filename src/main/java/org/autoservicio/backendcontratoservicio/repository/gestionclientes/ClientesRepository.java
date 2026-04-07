package org.autoservicio.backendcontratoservicio.repository.gestionclientes;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.autoservicio.backendcontratoservicio.config.IConfigGeneric;
import org.autoservicio.backendcontratoservicio.config.responseModel;
import org.autoservicio.backendcontratoservicio.excepciones.RepositorioException;
import org.autoservicio.backendcontratoservicio.interfaces.gestionclientes.IClientes;
import org.autoservicio.backendcontratoservicio.model.gestionclientes.BuscarClientes;
import org.autoservicio.backendcontratoservicio.model.gestionclientes.ClientesModel;
import org.autoservicio.backendcontratoservicio.response.ClientesRequest;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;

@Repository
public class ClientesRepository  extends IConfigGeneric implements IClientes {

    @Override
    public List<ClientesRequest> listadoclientes() {
        try {
            String query = "CALL usp_listar_clientes()";
            return this.jTemplate().query(query,
                    new BeanPropertyRowMapper<ClientesRequest>(ClientesRequest.class));
        } catch (Exception ex) {

            throw new RepositorioException("error en listado: " + ex.getMessage());
        }
    }

    @Override
    public responseModel registrarclientes(Integer op, ClientesModel obj) {
        try {
            // Convertir a JSON plano
            ObjectMapper mapper = new ObjectMapper();
            String cliente = mapper.writeValueAsString(obj);

            String sql = "CALL usp_registrar_clientes(?, ?)";

            String mensaje = this.jTemplate().queryForObject(sql, String.class, op, cliente);

            return responseModel.builder()
                    .response(mensaje)
                    .build();
        } catch (Exception ex) {
            throw new RepositorioException("Error al registrar kardex: " + ex.getMessage());
        }
    }

    @Override
    public List<BuscarClientes> buscarclientes(String valorBuscar) {
        try {
            String query = "CALL buscar_clientes(?)";
            return this.jTemplate().query(query,
                    new Object[]{valorBuscar},
                    new BeanPropertyRowMapper<>(BuscarClientes.class));
        } catch (Exception ex) {
            throw new RepositorioException("error en listado: " + ex.getMessage());
        }
    }

    @Override
    public BuscarClientes buscarClientePorId(Integer id) {
        try {
            String query = "SELECT id, nombres, apellidos, tipodocident, " +
                    "nrodocident, email, telefono, estareg FROM clientes WHERE id = ?";
            List<BuscarClientes> result = this.jTemplate().query(query,
                    new Object[]{id},
                    new BeanPropertyRowMapper<>(BuscarClientes.class));
            return result.isEmpty() ? null : result.get(0);
        } catch (Exception ex) {
            throw new RepositorioException("error al buscar cliente por id: " + ex.getMessage());
        }
    }

    public List<BuscarClientes> buscarClientesPorIds(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) return List.of();
        try {
            String placeholders = String.join(",", Collections.nCopies(ids.size(), "?"));
            String query = "SELECT id, nombres, apellidos, tipodocident, " +
                    "nrodocident, email, telefono, estareg FROM clientes WHERE id IN (" + placeholders + ")";
            return this.jTemplate().query(query, ids.toArray(),
                    new BeanPropertyRowMapper<>(BuscarClientes.class));
        } catch (Exception ex) {
            throw new RepositorioException("error al buscar clientes por ids: " + ex.getMessage());
        }
    }

}
