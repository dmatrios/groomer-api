package com.groomerapp.api.clients.data;

import com.groomerapp.api.clients.domain.ClientPhone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ClientPhoneRepository extends JpaRepository<ClientPhone, Long> {

    List<ClientPhone> findAllByClientIdAndActiveTrueOrderByIdAsc(Long clientId);

    Optional<ClientPhone> findByIdAndClientIdAndActiveTrue(Long id, Long clientId);

    @Query("""
   select distinct p.clientId from ClientPhone p
   where p.active = true
     and replace(p.phone, ' ', '') like concat('%', :q, '%')
""")
    List<Long> findClientIdsByPhoneLike(@Param("q") String q);
}
