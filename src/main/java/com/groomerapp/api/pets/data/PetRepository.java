package com.groomerapp.api.pets.data;

import com.groomerapp.api.pets.domain.Pet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PetRepository extends JpaRepository<Pet, Long> {

    Optional<Pet> findByIdAndActiveTrue(Long id);

    List<Pet> findAllByActiveTrueOrderByNameAsc();

    List<Pet> findAllByClientIdAndActiveTrueOrderByNameAsc(Long clientId);

    boolean existsByClientIdAndNameIgnoreCaseAndActiveTrue(Long clientId, String name);

    @Query("""
   select p from Pet p
   where p.active = true
     and (
       lower(p.code) like lower(concat('%', :q, '%'))
       or lower(p.name) like lower(concat('%', :q, '%'))
     )
""")
    List<Pet> searchActive(@Param("q") String q);

    List<Pet> findAllByClientIdInAndActiveTrueOrderByNameAsc(List<Long> clientIds);

    List<Pet> findAllByIdInAndActiveTrue(Collection<Long> ids);

    List<Pet> findByClientId(Long clientId);
}
