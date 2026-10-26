package com.pragma.onclass_capability_service.infraestructure.adapters.persistenceadapters.repository;

import com.pragma.onclass_capability_service.infraestructure.adapters.persistenceadapters.entity.CapabilityTechnologyEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataCapabilityTechnologyRepository extends ReactiveCrudRepository<CapabilityTechnologyEntity, Long> {

}
