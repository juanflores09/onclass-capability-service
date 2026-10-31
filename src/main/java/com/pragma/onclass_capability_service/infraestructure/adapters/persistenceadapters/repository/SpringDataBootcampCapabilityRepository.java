package com.pragma.onclass_capability_service.infraestructure.adapters.persistenceadapters.repository;

import com.pragma.onclass_capability_service.infraestructure.adapters.persistenceadapters.entity.CapabilityBootcampEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface SpringDataBootcampCapabilityRepository extends ReactiveCrudRepository<CapabilityBootcampEntity, Long> {

    Flux<CapabilityBootcampEntity> findByBootcampId(Long bootcampId);

    Mono<Void> deleteByBootcampId(Long bootcampId);

    @Query("SELECT COUNT(*) FROM capability_bootcamp WHERE capability_id = :capabilityId")
    Mono<Long> countBootcampsByCapabilityId(Long capabilityId);
}
