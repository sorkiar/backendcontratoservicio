package org.autoservicio.backendcontratoservicio.service;

import lombok.RequiredArgsConstructor;
import org.autoservicio.backendcontratoservicio.entity.SkuSequence;
import org.autoservicio.backendcontratoservicio.jparepository.SkuSequenceJpaRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SkuSequenceService {
  private final SkuSequenceJpaRepo repo;

  // type is already the prefix ("PRD" or "SRV")
  private static String prefix(String type) {
    return type;
  }

  @Transactional
  public String nextSku(String type) {
    SkuSequence seq = repo.findByTypeWithLock(type)
        .orElseThrow(() -> new RuntimeException("SKU sequence not found for type: " + type));
    int next = seq.getLastSeqValue() + 1;
    seq.setLastSeqValue(next);
    repo.save(seq);
    return prefix(type) + String.format("%05d", next);
  }

  /** Devuelve el siguiente SKU SIN consumirlo (sin incrementar el contador). */
  public String peekNextSku(String type) {
    SkuSequence seq = repo.findByType(type)
        .orElseThrow(() -> new RuntimeException("SKU sequence not found for type: " + type));
    return prefix(type) + String.format("%05d", seq.getLastSeqValue() + 1);
  }
}
