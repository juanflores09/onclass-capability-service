package com.pragma.onclass_capability_service.infraestructure.adapters.persistenceadapters.repository;

import com.pragma.onclass_capability_service.infraestructure.adapters.persistenceadapters.entity.CapabilityEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface SpringDataTechnologyRepository extends ReactiveCrudRepository<CapabilityEntity, Long> {

    Mono<Boolean> existsByName(String name);

}
