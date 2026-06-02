package com.evercare.repositories.impl;

import com.evercare.dtos.response.statistics.DiseaseStatisticsResponse;
import com.evercare.dtos.response.statistics.PatientStatisticsResponse;
import com.evercare.dtos.response.statistics.RevenueDetailResponse;
import com.evercare.dtos.response.statistics.RevenueSeriesResponse;
import com.evercare.dtos.response.statistics.RevenueStatisticsResponse;
import com.evercare.dtos.response.statistics.ServiceUsageStatisticsResponse;
import com.evercare.dtos.response.statistics.StatisticItemResponse;
import com.evercare.enums.AppointmentStatus;
import com.evercare.enums.InvoiceStatus;
import com.evercare.pojo.Appointment;
import com.evercare.pojo.Doctor;
import com.evercare.pojo.Department;
import com.evercare.pojo.Invoice;
import com.evercare.pojo.MedicalRecord;
import com.evercare.pojo.MedicalRecordService;
import com.evercare.pojo.MedicalService;
import com.evercare.pojo.Patient;
import com.evercare.repositories.StatisticRepository;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class StatisticRepositoryImpl implements StatisticRepository {
    @Autowired
    private LocalSessionFactoryBean factory;

    @Override
    public PatientStatisticsResponse getPatientStatistics(LocalDate fromDate, LocalDate toDate) {
        PatientStatisticsResponse response = new PatientStatisticsResponse();
        response.setByAgeGroups(getPatientAgeStatistics(fromDate, toDate));
        response.setByGender(getPatientGenderStatistics(fromDate, toDate));
        response.setByDepartment(getPatientDepartmentStatistics(fromDate, toDate));
        return response;
    }

    @Override
    public ServiceUsageStatisticsResponse getServiceUsageStatistics(LocalDate fromDate, LocalDate toDate) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<StatisticItemResponse> cq = cb.createQuery(StatisticItemResponse.class);
        Root<MedicalRecordService> root = cq.from(MedicalRecordService.class);
        Join<MedicalRecordService, MedicalRecord> medicalRecordJoin = root.join("medicalRecordId", JoinType.INNER);
        Join<MedicalRecordService, MedicalService> serviceJoin = root.join("serviceId", JoinType.INNER);

        Expression<BigDecimal> quantityExpr = cb.coalesce(root.<Integer>get("quantity"), 1).as(BigDecimal.class);
        Expression<BigDecimal> lineAmount = cb.prod(root.<BigDecimal>get("unitPrice"), quantityExpr);

        cq.select(cb.construct(
                StatisticItemResponse.class,
                serviceJoin.get("id"),
                serviceJoin.<String>get("name"),
                serviceJoin.<String>get("serviceType"),
                cb.count(root.get("id")),
                cb.coalesce(cb.sum(lineAmount), BigDecimal.ZERO)
        ));
        cq.where(
                cb.isTrue(root.get("active")),
                cb.isTrue(medicalRecordJoin.get("active")),
                cb.isTrue(serviceJoin.get("active")),
                betweenTimestamp(cb, medicalRecordJoin.<java.util.Date>get("visitDate"), fromDate, toDate)
        );
        cq.groupBy(
                serviceJoin.get("id"),
                serviceJoin.get("name"),
                serviceJoin.get("serviceType")
        );
        cq.orderBy(cb.desc(cb.count(root.get("id"))), cb.asc(serviceJoin.get("name")));

        List<StatisticItemResponse> items = session.createQuery(cq).getResultList();
        ServiceUsageStatisticsResponse response = new ServiceUsageStatisticsResponse();
        response.setItems(items);
        return response;
    }

    @Override
    public DiseaseStatisticsResponse getDiseaseStatistics(LocalDate fromDate, LocalDate toDate) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<StatisticItemResponse> cq = cb.createQuery(StatisticItemResponse.class);
        Root<MedicalRecord> root = cq.from(MedicalRecord.class);
        Expression<String> diagnosis = cb.trim(root.<String>get("diagnosis"));

        cq.select(cb.construct(
                StatisticItemResponse.class,
                diagnosis,
                cb.count(root.get("id"))
        ));
        cq.where(
                cb.isTrue(root.get("active")),
                cb.isNotNull(root.get("diagnosis")),
                cb.notEqual(diagnosis, ""),
                betweenTimestamp(cb, root.<java.util.Date>get("visitDate"), fromDate, toDate)
        );
        cq.groupBy(diagnosis);
        cq.orderBy(cb.desc(cb.count(root.get("id"))), cb.asc(diagnosis));

        List<StatisticItemResponse> items = session.createQuery(cq)
                
                .getResultList();

        DiseaseStatisticsResponse response = new DiseaseStatisticsResponse();
        response.setItems(items);
        return response;
    }

    @Override
    public RevenueStatisticsResponse getRevenueStatistics(LocalDate fromDate, LocalDate toDate, String groupBy) {
        RevenueStatisticsResponse response = new RevenueStatisticsResponse();
        response.setTotalRevenue(getTotalRevenue(fromDate, toDate));

        Map<String, Long> countsByStatus = getInvoiceStatusCounts(fromDate, toDate);
        response.setPaidInvoiceCount(countsByStatus.getOrDefault(InvoiceStatus.PAID.getCode(), 0L));
        response.setUnpaidInvoiceCount(
                countsByStatus.getOrDefault(InvoiceStatus.UNPAID.getCode(), 0L)
                        + countsByStatus.getOrDefault(InvoiceStatus.PARTIALLY_PAID.getCode(), 0L)
        );
        response.setRefundedInvoiceCount(countsByStatus.getOrDefault(InvoiceStatus.REFUNDED.getCode(), 0L));
        response.setSeries(getRevenueSeries(fromDate, toDate, groupBy));
        response.setDetails(getRevenueDetails(fromDate, toDate));
        return response;
    }

    private List<StatisticItemResponse> getPatientAgeStatistics(LocalDate fromDate, LocalDate toDate) {
        LocalDate today = LocalDate.now();
        List<StatisticItemResponse> items = new ArrayList<>();

        items.add(new StatisticItemResponse("0-5",
                countPatientsByBirthDateRange(fromDate, toDate, today.minusYears(6).plusDays(1), today)));
        items.add(new StatisticItemResponse("6-17",
                countPatientsByBirthDateRange(fromDate, toDate, today.minusYears(18).plusDays(1), today.minusYears(6))));
        items.add(new StatisticItemResponse("18-35",
                countPatientsByBirthDateRange(fromDate, toDate, today.minusYears(36).plusDays(1), today.minusYears(18))));
        items.add(new StatisticItemResponse("36-60",
                countPatientsByBirthDateRange(fromDate, toDate, today.minusYears(61).plusDays(1), today.minusYears(36))));
        items.add(new StatisticItemResponse("60+",
                countPatientsByBirthDateOlderThan(fromDate, toDate, today.minusYears(61))));
        items.add(new StatisticItemResponse("Unknown",
                countPatientsWithUnknownBirthDate(fromDate, toDate)));

        return items;
    }

    private List<StatisticItemResponse> getPatientGenderStatistics(LocalDate fromDate, LocalDate toDate) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<StatisticItemResponse> cq = cb.createQuery(StatisticItemResponse.class);
        Root<Appointment> root = cq.from(Appointment.class);
        Join<Appointment, Patient> patientJoin = root.join("patientId", JoinType.INNER);
        root.join("doctorId", JoinType.INNER);

        cq.select(cb.construct(
                StatisticItemResponse.class,
                patientJoin.<String>get("gender"),
                cb.countDistinct(patientJoin.get("id"))
        ));
        cq.where(
                cb.isTrue(root.get("active")),
                cb.isTrue(patientJoin.get("active")),
                cb.or(
                        cb.isNull(root.<String>get("status")),
                        cb.not(root.<String>get("status").in(
                                AppointmentStatus.CANCELLED.getCode(),
                                AppointmentStatus.NO_SHOW.getCode()
                        ))
                ),
                betweenDate(cb, root.<java.util.Date>get("appointmentDate"), fromDate, toDate)
        );
        cq.groupBy(patientJoin.get("gender"));
        cq.orderBy(cb.asc(patientJoin.get("gender")));

        return session.createQuery(cq).getResultList();
    }

    private List<StatisticItemResponse> getPatientDepartmentStatistics(LocalDate fromDate, LocalDate toDate) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<StatisticItemResponse> cq = cb.createQuery(StatisticItemResponse.class);
        Root<Appointment> root = cq.from(Appointment.class);
        Join<Appointment, Patient> patientJoin = root.join("patientId", JoinType.INNER);
        Join<Appointment, Doctor> doctorJoin = root.join("doctorId", JoinType.INNER);
        Join<Doctor, Department> departmentJoin = doctorJoin.join("departmentId", JoinType.LEFT);
        Expression<String> departmentName = cb.coalesce(departmentJoin.<String>get("name"), cb.literal("Chưa phân khoa"));

        cq.select(cb.construct(
                StatisticItemResponse.class,
                departmentName,
                cb.countDistinct(patientJoin.get("id"))
        ));
        cq.where(
                cb.isTrue(root.get("active")),
                cb.isTrue(patientJoin.get("active")),
                cb.or(
                        cb.isNull(root.<String>get("status")),
                        cb.not(root.<String>get("status").in(
                                AppointmentStatus.CANCELLED.getCode(),
                                AppointmentStatus.NO_SHOW.getCode()
                        ))
                ),
                betweenDate(cb, root.<java.util.Date>get("appointmentDate"), fromDate, toDate)
        );
        cq.groupBy(departmentJoin.get("id"), departmentJoin.get("name"));
        cq.orderBy(
                cb.desc(cb.countDistinct(patientJoin.get("id"))),
                cb.asc(departmentName)
        );

        return session.createQuery(cq).getResultList();
    }

    private BigDecimal getTotalRevenue(LocalDate fromDate, LocalDate toDate) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<BigDecimal> cq = cb.createQuery(BigDecimal.class);
        Root<Invoice> root = cq.from(Invoice.class);
        Expression<java.util.Date> activityDate = cb.coalesce(root.<java.util.Date>get("paidAt"), root.<java.util.Date>get("createdAt"));

        cq.select(cb.coalesce(cb.sum(root.get("totalAmount")), BigDecimal.ZERO));
        cq.where(
                cb.isTrue(root.get("active")),
                cb.equal(cb.upper(root.<String>get("paymentStatus")), InvoiceStatus.PAID.getCode()),
                betweenTimestamp(cb, activityDate, fromDate, toDate)
        );

        BigDecimal total = session.createQuery(cq).uniqueResult();
        return total != null ? total : BigDecimal.ZERO;
    }

    private Map<String, Long> getInvoiceStatusCounts(LocalDate fromDate, LocalDate toDate) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Object[]> cq = cb.createQuery(Object[].class);
        Root<Invoice> root = cq.from(Invoice.class);
        Expression<java.util.Date> activityDate = cb.coalesce(root.<java.util.Date>get("paidAt"), root.<java.util.Date>get("createdAt"));

        cq.multiselect(
                cb.upper(root.<String>get("paymentStatus")),
                cb.count(root.get("id"))
        );
        cq.where(
                cb.isTrue(root.get("active")),
                betweenTimestamp(cb, activityDate, fromDate, toDate)
        );
        cq.groupBy(cb.upper(root.<String>get("paymentStatus")));

        List<Object[]> rows = session.createQuery(cq).getResultList();
        Map<String, Long> result = new LinkedHashMap<>();
        for (Object[] row : rows) {
            if (row == null || row.length < 2 || row[0] == null) {
                continue;
            }

            result.put(row[0].toString(), row[1] != null ? ((Number) row[1]).longValue() : 0L);
        }

        return result;
    }

    private List<RevenueSeriesResponse> getRevenueSeries(LocalDate fromDate, LocalDate toDate, String groupBy) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Object[]> cq = cb.createQuery(Object[].class);
        Root<Invoice> root = cq.from(Invoice.class);
        Expression<java.util.Date> activityDate = cb.coalesce(root.<java.util.Date>get("paidAt"), root.<java.util.Date>get("createdAt"));
        String normalizedGroupBy = groupBy == null || groupBy.isBlank() ? "DAY" : groupBy.trim().toUpperCase();
        String pattern = "MONTH".equals(normalizedGroupBy) ? "%Y-%m" : "%Y-%m-%d";
        Expression<String> labelExpr = cb.function("date_format", String.class, activityDate, cb.literal(pattern));

        cq.multiselect(
                labelExpr,
                cb.coalesce(cb.sum(root.get("totalAmount")), BigDecimal.ZERO)
        );
        cq.where(
                cb.isTrue(root.get("active")),
                cb.equal(cb.upper(root.<String>get("paymentStatus")), InvoiceStatus.PAID.getCode()),
                betweenTimestamp(cb, activityDate, fromDate, toDate)
        );
        cq.groupBy(labelExpr);
        cq.orderBy(cb.asc(labelExpr));

        List<Object[]> rows = session.createQuery(cq).getResultList();
        List<RevenueSeriesResponse> series = new ArrayList<>();
        for (Object[] row : rows) {
            if (row == null || row.length < 2) {
                continue;
            }

            series.add(new RevenueSeriesResponse(
                    row[0] != null ? row[0].toString() : null,
                    row[1] != null ? new BigDecimal(row[1].toString()) : BigDecimal.ZERO
            ));
        }

        return series;
    }

    private List<RevenueDetailResponse> getRevenueDetails(LocalDate fromDate, LocalDate toDate) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<RevenueDetailResponse> cq = cb.createQuery(RevenueDetailResponse.class);
        Root<Invoice> root = cq.from(Invoice.class);
        Join<Invoice, Patient> patientJoin = root.join("patientId", JoinType.INNER);
        Join<Invoice, MedicalRecord> recordJoin = root.join("medicalRecordId", JoinType.INNER);
        Expression<java.util.Date> activityDate = cb.coalesce(root.<java.util.Date>get("paidAt"), root.<java.util.Date>get("createdAt"));

        cq.select(cb.construct(
                RevenueDetailResponse.class,
                root.get("id"),
                root.get("invoiceCode"),
                patientJoin.get("fullName"),
                recordJoin.get("recordCode"),
                root.get("totalAmount"),
                root.get("paymentStatus"),
                activityDate
        ));
        cq.where(
                cb.isTrue(root.get("active")),
                betweenTimestamp(cb, activityDate, fromDate, toDate)
        );
        cq.orderBy(cb.desc(activityDate), cb.desc(root.get("id")));

        return session.createQuery(cq).getResultList();
    }

    private long countPatientsByBirthDateRange(LocalDate fromDate, LocalDate toDate, LocalDate birthFrom, LocalDate birthTo) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<Appointment> root = cq.from(Appointment.class);
        Join<Appointment, Patient> patientJoin = root.join("patientId", JoinType.INNER);
        root.join("doctorId", JoinType.INNER);

        cq.select(cb.countDistinct(patientJoin.get("id")));
        cq.where(
                cb.isTrue(root.get("active")),
                cb.isTrue(patientJoin.get("active")),
                cb.isNotNull(patientJoin.get("dateOfBirth")),
                cb.or(
                        cb.isNull(root.<String>get("status")),
                        cb.not(root.<String>get("status").in(
                                AppointmentStatus.CANCELLED.getCode(),
                                AppointmentStatus.NO_SHOW.getCode()
                        ))
                ),
                betweenDate(cb, root.<java.util.Date>get("appointmentDate"), fromDate, toDate),
                cb.greaterThanOrEqualTo(patientJoin.get("dateOfBirth"), java.sql.Date.valueOf(birthFrom)),
                cb.lessThanOrEqualTo(patientJoin.get("dateOfBirth"), java.sql.Date.valueOf(birthTo))
        );

        Long count = session.createQuery(cq).uniqueResult();
        return count != null ? count : 0L;
    }

    private long countPatientsByBirthDateOlderThan(LocalDate fromDate, LocalDate toDate, LocalDate birthTo) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<Appointment> root = cq.from(Appointment.class);
        Join<Appointment, Patient> patientJoin = root.join("patientId", JoinType.INNER);
        root.join("doctorId", JoinType.INNER);

        cq.select(cb.countDistinct(patientJoin.get("id")));
        cq.where(
                cb.isTrue(root.get("active")),
                cb.isTrue(patientJoin.get("active")),
                cb.isNotNull(patientJoin.get("dateOfBirth")),
                cb.or(
                        cb.isNull(root.<String>get("status")),
                        cb.not(root.<String>get("status").in(
                                AppointmentStatus.CANCELLED.getCode(),
                                AppointmentStatus.NO_SHOW.getCode()
                        ))
                ),
                betweenDate(cb, root.<java.util.Date>get("appointmentDate"), fromDate, toDate),
                cb.lessThanOrEqualTo(patientJoin.get("dateOfBirth"), java.sql.Date.valueOf(birthTo))
        );

        Long count = session.createQuery(cq).uniqueResult();
        return count != null ? count : 0L;
    }

    private long countPatientsWithUnknownBirthDate(LocalDate fromDate, LocalDate toDate) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<Appointment> root = cq.from(Appointment.class);
        Join<Appointment, Patient> patientJoin = root.join("patientId", JoinType.INNER);
        root.join("doctorId", JoinType.INNER);

        cq.select(cb.countDistinct(patientJoin.get("id")));
        cq.where(
                cb.isTrue(root.get("active")),
                cb.isTrue(patientJoin.get("active")),
                cb.isNull(patientJoin.get("dateOfBirth")),
                cb.or(
                        cb.isNull(root.<String>get("status")),
                        cb.not(root.<String>get("status").in(
                                AppointmentStatus.CANCELLED.getCode(),
                                AppointmentStatus.NO_SHOW.getCode()
                        ))
                ),
                betweenDate(cb, root.<java.util.Date>get("appointmentDate"), fromDate, toDate)
        );

        Long count = session.createQuery(cq).uniqueResult();
        return count != null ? count : 0L;
    }

    private Predicate betweenDate(CriteriaBuilder cb, Expression<java.util.Date> datePath, LocalDate fromDate, LocalDate toDate) {
        return cb.and(
                cb.greaterThanOrEqualTo(datePath, java.sql.Date.valueOf(fromDate)),
                cb.lessThan(datePath, java.sql.Date.valueOf(toDate.plusDays(1)))
        );
    }

    private Predicate betweenTimestamp(CriteriaBuilder cb, Expression<java.util.Date> datePath, LocalDate fromDate, LocalDate toDate) {
        return cb.and(
                cb.greaterThanOrEqualTo(datePath, java.sql.Timestamp.valueOf(fromDate.atStartOfDay())),
                cb.lessThan(datePath, java.sql.Timestamp.valueOf(toDate.plusDays(1).atStartOfDay()))
        );
    }
}
