package com.groomerapp.api.clients.data;

import com.groomerapp.api.clients.domain.Client;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Long> {

    Optional<Client> findByIdAndActiveTrue(Long id);

    boolean existsByCode(String code);

    Page<Client> findAllByActiveTrue(Pageable pageable);

    Page<Client> findAllByActiveTrueAndZoneId(Long zoneId, Pageable pageable);

    @Query("""
   select c from Client c
   where c.active = true
     and (
       lower(c.code) like lower(concat('%', :q, '%'))
       or lower(c.firstName) like lower(concat('%', :q, '%'))
       or lower(c.lastName) like lower(concat('%', :q, '%'))
       or lower(concat(c.firstName, ' ', c.lastName)) like lower(concat('%', :q, '%'))
       or lower(c.zoneText) like lower(concat('%', :q, '%'))
     )
""")
    List<Client> searchActive(@Param("q") String q);
}
