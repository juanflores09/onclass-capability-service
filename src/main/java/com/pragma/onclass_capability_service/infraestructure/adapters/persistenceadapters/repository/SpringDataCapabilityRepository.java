package com.pragma.onclass_capability_service.infraestructure.adapters.persistenceadapters.repository;

import com.pragma.onclass_capability_service.infraestructure.adapters.persistenceadapters.entity.CapabilityEntity;
import com.pragma.onclass_capability_service.infraestructure.entrypoints.dto.CapabilityWithCountDTO;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Repository
public interface SpringDataCapabilityRepository extends ReactiveCrudRepository<CapabilityEntity, Long> {

    Mono<Boolean> existsByName(String name);

    @Query("""
        SELECT c.id, c.name, c.description, COUNT(ct.technology_id) as tech_count
        FROM capability c
        LEFT JOIN capability_technology ct ON c.id = ct.capability_id
        GROUP BY c.id, c.name, c.description
        ORDER BY 
            CASE WHEN :sortBy = 'name' AND :direction = 'ASC' THEN c.name END ASC,
            CASE WHEN :sortBy = 'name' AND :direction = 'DESC' THEN c.name END DESC,
            CASE WHEN :sortBy = 'technologies_count' AND :direction = 'ASC' THEN COUNT(ct.technology_id) END ASC,
            CASE WHEN :sortBy = 'technologies_count' AND :direction = 'DESC' THEN COUNT(ct.technology_id) END DESC
        LIMIT :limit OFFSET :offset
    """)
    Flux<CapabilityWithCountDTO> findAllPagedAndSorted(
            @Param("sortBy") String sortBy,
            @Param("direction") String direction,
            @Param("limit") int limit,
            @Param("offset") long offset
    );

    Mono<Long> countByIdIn(List<Long> ids);
}
