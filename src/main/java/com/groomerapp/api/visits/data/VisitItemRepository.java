package com.groomerapp.api.visits.data;

import com.groomerapp.api.shared.domain.PaymentMethod;
import com.groomerapp.api.visits.domain.VisitItem;
import com.groomerapp.api.visits.domain.VisitItemCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface VisitItemRepository extends JpaRepository<VisitItem, Long> {

    List<VisitItem> findAllByVisitIdInOrderByVisitIdAscIdAsc(List<Long> visitIds);


    List<VisitItem> findAllByVisitIdOrderByIdAsc(Long visitId);

    void deleteAllByVisitId(Long visitId);

    interface CategoryTotalRow {
        VisitItemCategory getCategory();
        BigDecimal getTotal();
    }

    // =========================
    // REPORTS (sin filtro) - se mantiene
    // =========================
    @Query("""
       select i.category as category, coalesce(sum(i.price), 0) as total
       from VisitItem i
       join Visit v on v.id = i.visitId
       where v.active = true
         and v.visitedAt between :from and :to
       group by i.category
       order by total desc
    """)
    List<CategoryTotalRow> sumByCategory(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    // =========================
    // REPORTS (con filtro paymentMethod) - NUEVO overload
    // =========================
    @Query("""
       select i.category as category, coalesce(sum(i.price), 0) as total
       from VisitItem i
       join Visit v on v.id = i.visitId
       join Payment p on p.visitId = v.id
       where v.active = true
         and v.visitedAt between :from and :to
         and (:paymentMethod is null or p.method = :paymentMethod)
       group by i.category
       order by total desc
    """)
    List<CategoryTotalRow> sumByCategory(@Param("from") LocalDateTime from,
                                         @Param("to") LocalDateTime to,
                                         @Param("paymentMethod") PaymentMethod paymentMethod);
}
