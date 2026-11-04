package com.pragma.onclass_capability_service.infraestructure.adapters.persistenceadapters.repository;

import com.pragma.onclass_capability_service.infraestructure.adapters.persistenceadapters.entity.CapabilityEntity;
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
        SELECT c.id, c.name, c.description, c.technology_count
        FROM capability c
        ORDER BY 
            CASE WHEN :sortBy = 'name' AND :direction = 'ASC' THEN c.name END ASC,
            CASE WHEN :sortBy = 'name' AND :direction = 'DESC' THEN c.name END DESC,
            CASE WHEN :sortBy = 'technologies_count' AND :direction = 'ASC' THEN c.technology_count END ASC,
            CASE WHEN :sortBy = 'technologies_count' AND :direction = 'DESC' THEN c.technology_count END DESC
        LIMIT :limit OFFSET :offset
    """)
    Flux<CapabilityEntity> findAllPagedAndSorted(
            @Param("sortBy") String sortBy,
            @Param("direction") String direction,
            @Param("limit") int limit,
            @Param("offset") long offset
    );

    Mono<Long> countByIdIn(List<Long> ids);

    @Query("""
        SELECT cb.bootcamp_id,
                c.id,
                c.name,
                c.description FROM capability c
        JOIN capability_bootcamp cb ON c.id = cb.capability_id
        WHERE cb.bootcamp_id IN (:bootcampIds)
    """)
    Flux<BootcampCapabilityProjection> findCapabilitiesByBootcampId(List<Long> bootcampIds);

    @Query("SELECT SUM(c.technology_count) " +
            "FROM capability c " +
            "JOIN capability_bootcamp cb ON c.id = cb.capability_id " +
            "WHERE cb.bootcamp_id = :bootcampId")
    Mono<Integer> sumTechnologyCountByBootcampId(@Param("bootcampId") Long bootcampId);

}
