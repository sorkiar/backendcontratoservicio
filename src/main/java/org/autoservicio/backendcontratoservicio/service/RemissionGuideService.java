package org.autoservicio.backendcontratoservicio.service;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.autoservicio.backendcontratoservicio.dto.mapper.RemissionGuideMapper;
import org.autoservicio.backendcontratoservicio.dto.request.RemissionGuideDriverRequest;
import org.autoservicio.backendcontratoservicio.dto.request.RemissionGuideItemRequest;
import org.autoservicio.backendcontratoservicio.dto.request.RemissionGuideRequest;
import org.autoservicio.backendcontratoservicio.dto.response.RemissionGuideResponse;
import org.autoservicio.backendcontratoservicio.entity.RemissionGuide;
import org.autoservicio.backendcontratoservicio.entity.RemissionGuideDriver;
import org.autoservicio.backendcontratoservicio.entity.RemissionGuideItem;
import org.autoservicio.backendcontratoservicio.jparepository.DocumentSeriesJpaRepo;
import org.autoservicio.backendcontratoservicio.jparepository.ProductJpaRepo;
import org.autoservicio.backendcontratoservicio.jparepository.RemissionGuideJpaRepo;
import org.autoservicio.backendcontratoservicio.model.gestionclientes.BuscarClientes;
import org.autoservicio.backendcontratoservicio.repository.gestionclientes.ClientesRepository;
import org.autoservicio.backendcontratoservicio.specification.RemissionGuideSpec;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

@Service
@RequiredArgsConstructor
public class RemissionGuideService {
  private final RemissionGuideJpaRepo repo;
  private final RemissionGuideMapper mapper;
  private final DocumentSeriesJpaRepo documentSeriesRepo;
  private final ProductJpaRepo productRepo;
  private final ClientesRepository clientesRepository;
  private final PlatformTransactionManager txManager;

  private <T> T inTx(TransactionCallback<T> cb) {
    return new TransactionTemplate(txManager).execute(cb);
  }

  private <T> T inReadTx(TransactionCallback<T> cb) {
    TransactionTemplate t = new TransactionTemplate(txManager);
    t.setReadOnly(true);
    return t.execute(cb);
  }

  public Mono<List<RemissionGuideResponse>> listar(Long clientId, String status,
                                                   LocalDate startDate, LocalDate endDate) {
    return Mono.fromCallable(() -> inReadTx(txStatus -> {
      Specification<RemissionGuide> spec = RemissionGuideSpec.build(clientId, status, startDate, endDate);
      return repo.findAll(spec).stream()
          .map(g -> enrichWithClient(mapper.toResponse(g))).toList();
    })).subscribeOn(Schedulers.boundedElastic());
  }

  public Mono<RemissionGuideResponse> obtenerPorId(Long id) {
    return Mono.fromCallable(() -> inReadTx(txStatus -> {
      RemissionGuide g = repo.findById(id)
          .orElseThrow(() -> new RuntimeException("Guía no encontrada: " + id));
      return enrichWithClient(mapper.toResponse(g));
    })).subscribeOn(Schedulers.boundedElastic());
  }

  public Mono<RemissionGuideResponse> crear(RemissionGuideRequest request) {
    return Mono.fromCallable(() -> inTx(txStatus -> {
      RemissionGuide guide = new RemissionGuide();
      guide.setClientId(request.getClientId());

      if (request.getDocumentSeriesId() != null) {
        documentSeriesRepo.findByIdWithLock(request.getDocumentSeriesId()).ifPresent(ds -> {
          int next = ds.getCurrentSequence() + 1;
          ds.setCurrentSequence(next);
          documentSeriesRepo.save(ds);
          guide.setDocumentSeries(ds);
          guide.setSeries(ds.getSeries());
          guide.setSequence(next);
        });
      }
      guide.setIssueDate(request.getIssueDate() != null ? request.getIssueDate() : LocalDate.now());
      guide.setTransferDate(request.getTransferDate());
      guide.setTransferReason(request.getTransferReason());
      guide.setTransferReasonDescription(request.getTransferReasonDescription());
      guide.setTransportMode(request.getTransportMode());
      guide.setGrossWeight(request.getGrossWeight());
      guide.setWeightUnit(request.getWeightUnit());
      guide.setPackageCount(request.getPackageCount());
      guide.setOriginAddress(request.getOriginAddress());
      guide.setOriginUbigeo(request.getOriginUbigeo());
      guide.setDestinationAddress(request.getDestinationAddress());
      guide.setDestinationUbigeo(request.getDestinationUbigeo());
      guide.setMinorVehicleTransfer(request.getMinorVehicleTransfer());
      guide.setCarrierRuc(request.getCarrierRuc());
      guide.setCarrierName(request.getCarrierName());
      guide.setCarrierAuthorizationCode(request.getCarrierAuthorizationCode());
      guide.setObservations(request.getObservations());
      guide.setStatus("PENDIENTE");

      List<RemissionGuideDriver> drivers = new ArrayList<>();
      if (request.getDrivers() != null) {
        for (RemissionGuideDriverRequest dr : request.getDrivers()) {
          RemissionGuideDriver driver = new RemissionGuideDriver();
          driver.setRemissionGuide(guide);
          driver.setDriverFirstName(dr.getDriverFirstName());
          driver.setDriverLastName(dr.getDriverLastName());
          driver.setDriverDocType(dr.getDriverDocType());
          driver.setDriverDocNumber(dr.getDriverDocNumber());
          driver.setDriverLicenseNumber(dr.getDriverLicenseNumber());
          driver.setVehiclePlate(dr.getVehiclePlate());
          drivers.add(driver);
        }
      }
      guide.setDrivers(drivers);

      List<RemissionGuideItem> items = new ArrayList<>();
      if (request.getItems() != null) {
        for (RemissionGuideItemRequest ir : request.getItems()) {
          RemissionGuideItem item = new RemissionGuideItem();
          item.setRemissionGuide(guide);
          if (ir.getProductId() != null)
            productRepo.findById(ir.getProductId()).ifPresent(item::setProduct);
          item.setDescription(ir.getDescription());
          item.setQuantity(ir.getQuantity() != null ? ir.getQuantity() : BigDecimal.ONE);
          item.setUnitMeasureSunat(ir.getUnitMeasureSunat());
          item.setUnitPrice(ir.getUnitPrice() != null ? ir.getUnitPrice() : BigDecimal.ZERO);
          items.add(item);
        }
      }
      guide.setItems(items);

      return enrichWithClient(mapper.toResponse(repo.save(guide)));
    })).subscribeOn(Schedulers.boundedElastic());
  }

  private RemissionGuideResponse enrichWithClient(RemissionGuideResponse response) {
    try {
      if (response.getClientId() == null) return response;
      List<BuscarClientes> clients = clientesRepository.buscarclientes(
          String.valueOf(response.getClientId()));
      if (!clients.isEmpty()) {
        BuscarClientes c = clients.get(0);
        String fullName = c.getNombre_completo() != null ? c.getNombre_completo()
            : ((c.getNombres() != null ? c.getNombres() : "") + " "
            + (c.getApellidos() != null ? c.getApellidos() : "")).trim();
        response.setClientName(fullName);
      }
    } catch (Exception ignored) {
      // Client enrichment is non-critical
    }
    return response;
  }
}
