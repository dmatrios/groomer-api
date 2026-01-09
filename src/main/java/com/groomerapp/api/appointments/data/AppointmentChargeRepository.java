package com.groomerapp.api.appointments.data;

import com.groomerapp.api.appointments.domain.AppointmentCharge;
import com.groomerapp.api.shared.domain.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AppointmentChargeRepository extends JpaRepository<AppointmentCharge, Long> {

    Optional<AppointmentCharge> findByAppointmentId(Long appointmentId);

    // =========================
    // REPORTS (sin filtro) - se mantiene
    // =========================
    @Query("""
        select coalesce(sum(c.amount), 0)
        from AppointmentCharge c
        where c.createdAt between :from and :to
    """)
    BigDecimal sumAmountBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    // =========================
    // REPORTS (con filtro paymentMethod) - NUEVO overload
    // =========================
    @Query("""
        select coalesce(sum(c.amount), 0)
        from AppointmentCharge c
        where c.createdAt between :from and :to
          and (:paymentMethod is null or c.method = :paymentMethod)
    """)
    BigDecimal sumAmountBetween(@Param("from") LocalDateTime from,
                                @Param("to") LocalDateTime to,
                                @Param("paymentMethod") PaymentMethod paymentMethod);

    interface TimePointRow {
        String getPeriod();
        BigDecimal getTotal();
    }

    // =========================
    // TIMESERIES (sin filtro) - se mantienen
    // =========================
    @Query(value = """
        select date_format(c.created_at, '%Y-%m-%d') as period,
               coalesce(sum(c.amount), 0) as total
        from appointment_charge c
        where c.created_at between :from and :to
        group by date_format(c.created_at, '%Y-%m-%d')
        order by period asc
    """, nativeQuery = true)
    List<TimePointRow> timeseriesDayCharges(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query(value = """
        select date_format(c.created_at, '%Y-%m') as period,
               coalesce(sum(c.amount), 0) as total
        from appointment_charge c
        where c.created_at between :from and :to
        group by date_format(c.created_at, '%Y-%m')
        order by period asc
    """, nativeQuery = true)
    List<TimePointRow> timeseriesMonthCharges(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query(value = """
        select date_format(c.created_at, '%Y') as period,
               coalesce(sum(c.amount), 0) as total
        from appointment_charge c
        where c.created_at between :from and :to
        group by date_format(c.created_at, '%Y')
        order by period asc
    """, nativeQuery = true)
    List<TimePointRow> timeseriesYearCharges(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    // =========================
    // TIMESERIES (con filtro paymentMethod) - NUEVOS overloads
    // NOTE: paymentMethod es String ("CASH", "CARD", "MOBILE_BANKING") o null
    // =========================
    @Query(value = """
        select date_format(c.created_at, '%Y-%m-%d') as period,
               coalesce(sum(c.amount), 0) as total
        from appointment_charge c
        where c.created_at between :from and :to
          and (:paymentMethod is null or c.method = :paymentMethod)
        group by date_format(c.created_at, '%Y-%m-%d')
        order by period asc
    """, nativeQuery = true)
    List<TimePointRow> timeseriesDayCharges(@Param("from") LocalDateTime from,
                                            @Param("to") LocalDateTime to,
                                            @Param("paymentMethod") String paymentMethod);

    @Query(value = """
        select date_format(c.created_at, '%Y-%m') as period,
               coalesce(sum(c.amount), 0) as total
        from appointment_charge c
        where c.created_at between :from and :to
          and (:paymentMethod is null or c.method = :paymentMethod)
        group by date_format(c.created_at, '%Y-%m')
        order by period asc
    """, nativeQuery = true)
    List<TimePointRow> timeseriesMonthCharges(@Param("from") LocalDateTime from,
                                              @Param("to") LocalDateTime to,
                                              @Param("paymentMethod") String paymentMethod);

    @Query(value = """
        select date_format(c.created_at, '%Y') as period,
               coalesce(sum(c.amount), 0) as total
        from appointment_charge c
        where c.created_at between :from and :to
          and (:paymentMethod is null or c.method = :paymentMethod)
        group by date_format(c.created_at, '%Y')
        order by period asc
    """, nativeQuery = true)
    List<TimePointRow> timeseriesYearCharges(@Param("from") LocalDateTime from,
                                             @Param("to") LocalDateTime to,
                                             @Param("paymentMethod") String paymentMethod);
}
