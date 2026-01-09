package com.groomerapp.api.visits.data;

import com.groomerapp.api.shared.domain.PaymentMethod;
import com.groomerapp.api.visits.domain.Visit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.groomerapp.api.visits.domain.VisitItemCategory;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface VisitRepository extends JpaRepository<Visit, Long> {

    long countByPetId(Long petId);

    Optional<Visit> findTopByPetIdOrderByVisitedAtDesc(Long petId);

    Optional<Visit> findByIdAndActiveTrue(Long id);

    List<Visit> findAllByActiveTrueAndVisitedAtBetweenOrderByVisitedAtAsc(LocalDateTime from, LocalDateTime to);

    List<Visit> findAllByPetIdAndActiveTrueOrderByVisitedAtDesc(Long petId);

    @Query("""
       select v from Visit v
       where v.active = true
         and v.petId in :petIds
       order by v.visitedAt desc
    """)
    List<Visit> findAllByPetIdInAndActiveTrue(@Param("petIds") List<Long> petIds);

    @Query("""
       select v from Visit v
       where v.active = true
         and lower(coalesce(v.notes, '')) like lower(concat('%', :q, '%'))
       order by v.visitedAt desc
    """)
    List<Visit> searchByNotes(@Param("q") String q);

    // =========================
    // REPORTS (sin filtro) - se mantienen
    // =========================
    @Query("""
       select coalesce(sum(v.totalAmount), 0)
       from Visit v
       where v.active = true
         and v.visitedAt between :from and :to
    """)
    BigDecimal sumTotalBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    interface TimePointRow {
        String getPeriod();
        BigDecimal getTotal();
    }

    @Query(value = """
       select date_format(v.visited_at, '%Y-%m-%d') as period,
              coalesce(sum(v.total_amount), 0) as total
       from visit v
       where v.active = true
         and v.visited_at between :from and :to
       group by date_format(v.visited_at, '%Y-%m-%d')
       order by period asc
    """, nativeQuery = true)
    List<TimePointRow> timeseriesDayVisits(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query(value = """
       select date_format(v.visited_at, '%Y-%m') as period,
              coalesce(sum(v.total_amount), 0) as total
       from visit v
       where v.active = true
         and v.visited_at between :from and :to
       group by date_format(v.visited_at, '%Y-%m')
       order by period asc
    """, nativeQuery = true)
    List<TimePointRow> timeseriesMonthVisits(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query(value = """
        select date_format(v.visited_at, '%Y') as period,
               coalesce(sum(v.total_amount), 0) as total
        from visit v
        where v.active = true
          and v.visited_at between :from and :to
        group by date_format(v.visited_at, '%Y')
        order by period asc
    """, nativeQuery = true)
    List<TimePointRow> timeseriesYearVisits(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    // =========================
    // REPORTS (con filtro paymentMethod) - NUEVOS overloads
    // =========================

    // Gross filtrado por Payment.method (JPQL con join a Payment por visitId)
    @Query("""
       select coalesce(sum(v.totalAmount), 0)
       from Visit v
       join Payment p on p.visitId = v.id
       where v.active = true
         and v.visitedAt between :from and :to
         and (:paymentMethod is null or p.method = :paymentMethod)
    """)
    BigDecimal sumTotalBetween(@Param("from") LocalDateTime from,
                               @Param("to") LocalDateTime to,
                               @Param("paymentMethod") PaymentMethod paymentMethod);

    // Timeseries (native) con join a payment y filtro opcional
    // NOTE: paymentMethod aquí es String ("CASH", "CARD", "MOBILE_BANKING") o null
    @Query(value = """
       select date_format(v.visited_at, '%Y-%m-%d') as period,
              coalesce(sum(v.total_amount), 0) as total
       from visit v
       join payment p on p.visit_id = v.id
       where v.active = true
         and v.visited_at between :from and :to
         and (:paymentMethod is null or p.method = :paymentMethod)
       group by date_format(v.visited_at, '%Y-%m-%d')
       order by period asc
    """, nativeQuery = true)
    List<TimePointRow> timeseriesDayVisits(@Param("from") LocalDateTime from,
                                           @Param("to") LocalDateTime to,
                                           @Param("paymentMethod") String paymentMethod);

    @Query(value = """
       select date_format(v.visited_at, '%Y-%m') as period,
              coalesce(sum(v.total_amount), 0) as total
       from visit v
       join payment p on p.visit_id = v.id
       where v.active = true
         and v.visited_at between :from and :to
         and (:paymentMethod is null or p.method = :paymentMethod)
       group by date_format(v.visited_at, '%Y-%m')
       order by period asc
    """, nativeQuery = true)
    List<TimePointRow> timeseriesMonthVisits(@Param("from") LocalDateTime from,
                                             @Param("to") LocalDateTime to,
                                             @Param("paymentMethod") String paymentMethod);

    @Query(value = """
        select date_format(v.visited_at, '%Y') as period,
               coalesce(sum(v.total_amount), 0) as total
        from visit v
        join payment p on p.visit_id = v.id
        where v.active = true
          and v.visited_at between :from and :to
          and (:paymentMethod is null or p.method = :paymentMethod)
        group by date_format(v.visited_at, '%Y')
        order by period asc
    """, nativeQuery = true)
    List<TimePointRow> timeseriesYearVisits(@Param("from") LocalDateTime from,
                                            @Param("to") LocalDateTime to,
                                            @Param("paymentMethod") String paymentMethod);

    @Query("""
   select v from Visit v
   where v.active = true
     and v.petId = :petId
     and exists (
        select 1 from VisitItem i
        where i.visitId = v.id
          and i.category = :category
     )
   order by v.visitedAt desc
""")
    List<Visit> findAllByPetIdAndActiveTrueAndCategory(@Param("petId") Long petId,
                                                       @Param("category") VisitItemCategory category);

}
