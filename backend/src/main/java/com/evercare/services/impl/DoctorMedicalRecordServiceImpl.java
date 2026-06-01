package com.evercare.services.impl;

import com.evercare.dtos.request.MedicalRecordServiceRequest;
import com.evercare.dtos.request.UpdateMedicalRecordRequest;
import com.evercare.dtos.response.MedicalRecordServiceResponse;
import com.evercare.dtos.response.MedicalRecordResponse;
import com.evercare.enums.AppointmentStatus;
import com.evercare.enums.InvoiceStatus;
import com.evercare.enums.MedicalServiceType;
import com.evercare.mappers.MedicalRecordServiceMapper;
import com.evercare.mappers.MedicalRecordMapper;
import com.evercare.pojo.Appointment;
import com.evercare.pojo.Doctor;
import com.evercare.pojo.Invoice;
import com.evercare.pojo.MedicalRecord;
import com.evercare.pojo.MedicalRecordService;
import com.evercare.pojo.MedicalService;
import com.evercare.pojo.Prescription;
import com.evercare.pojo.PrescriptionItem;
import com.evercare.pojo.TestResult;
import com.evercare.pojo.User;
import com.evercare.repositories.AppointmentRepository;
import com.evercare.repositories.InvoiceRepository;
import com.evercare.repositories.MedicalRecordRepository;
import com.evercare.repositories.MedicalRecordServiceRepository;
import com.evercare.repositories.MedicalServiceRepository;
import com.evercare.repositories.PrescriptionRepository;
import com.evercare.repositories.TestResultRepository;
import com.evercare.services.DoctorMedicalRecordService;
import com.evercare.utils.AuthSupport;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DoctorMedicalRecordServiceImpl implements DoctorMedicalRecordService {
    @Autowired
    private MedicalRecordRepository medicalRecordRepo;

    @Autowired
    private AppointmentRepository appointmentRepo;

    @Autowired
    private InvoiceRepository invoiceRepo;

    @Autowired
    private MedicalRecordServiceRepository medicalRecordServiceRepo;

    @Autowired
    private MedicalServiceRepository medicalServiceRepo;

    @Autowired
    private PrescriptionRepository prescriptionRepo;

    @Autowired
    private TestResultRepository testResultRepo;

    @Autowired
    private AuthSupport authSupport;

    @Override
    public MedicalRecordResponse updateMedicalRecord(String username, Long recordId, UpdateMedicalRecordRequest request) {
        Doctor doctor = this.authSupport.requireCurrentDoctor(username);
        MedicalRecord medicalRecord = this.medicalRecordRepo.getMedicalRecordById(recordId);

        if (medicalRecord == null || Boolean.FALSE.equals(medicalRecord.getActive())) {
            throw new NoSuchElementException("Không tìm thấy bệnh án");
        }

        validateOwnedMedicalRecord(doctor, medicalRecord);
        validateAppointmentNotCompleted(medicalRecord);
        applyRequest(medicalRecord, request);
        medicalRecord.setUpdatedAt(new java.util.Date());

        this.medicalRecordRepo.updateMedicalRecord(medicalRecord);

        return MedicalRecordMapper.toResponse(medicalRecord);
    }

    @Override
    public MedicalRecordResponse completeMedicalRecord(String username, Long recordId) {
        Doctor doctor = this.authSupport.requireCurrentDoctor(username);
        MedicalRecord medicalRecord = loadMedicalRecordForDoctor(doctor, recordId);
        Appointment appointment = medicalRecord.getAppointmentId();

        if (appointment == null) {
            throw new IllegalStateException("Bệnh án chưa gắn với lịch hẹn");
        }

        if (AppointmentStatus.COMPLETED.getCode().equalsIgnoreCase(appointment.getStatus())) {
            Date now = new Date();
            if (createOrUpdateUnpaidInvoice(medicalRecord, now)) {
                medicalRecord.setUpdatedAt(now);
                this.medicalRecordRepo.updateMedicalRecord(medicalRecord);
            }
            return MedicalRecordMapper.toResponse(medicalRecord);
        }

        if (!AppointmentStatus.IN_PROGRESS.getCode().equalsIgnoreCase(appointment.getStatus())) {
            throw new IllegalStateException("Chỉ có thể hoàn tất bệnh án khi lịch khám đang IN_PROGRESS");
        }

        if (medicalRecord.getDiagnosis() == null || medicalRecord.getDiagnosis().isBlank()) {
            throw new IllegalStateException("Vui lòng nhập chẩn đoán trước khi hoàn tất bệnh án");
        }

        validateRequiredResultsCompleted(medicalRecord);

        Date now = new Date();
        appointment.setStatus(AppointmentStatus.COMPLETED.getCode());
        appointment.setUpdatedAt(now);
        medicalRecord.setPaymentStatus(InvoiceStatus.UNPAID.getCode());
        medicalRecord.setUpdatedAt(now);
        createOrUpdateUnpaidInvoice(medicalRecord, now);

        this.medicalRecordRepo.updateMedicalRecord(medicalRecord);
        this.appointmentRepo.updateAppointment(appointment);

        return MedicalRecordMapper.toResponse(medicalRecord);
    }

    @Override
    public MedicalRecordServiceResponse addService(String username, Long recordId, MedicalRecordServiceRequest request) {
        Doctor doctor = this.authSupport.requireCurrentDoctor(username);
        MedicalRecord medicalRecord = loadEditableMedicalRecordForDoctor(doctor, recordId);

        if (request == null || request.getServiceId() == null) {
            throw new IllegalArgumentException("Vui lòng chọn dịch vụ");
        }

        int quantity = request.getQuantity() != null ? request.getQuantity() : 1;
        if (quantity <= 0) {
            throw new IllegalArgumentException("Số lượng dịch vụ phải lớn hơn 0");
        }

        MedicalService service = this.medicalServiceRepo.getServiceById(request.getServiceId().intValue());
        if (service == null || Boolean.FALSE.equals(service.getActive())) {
            throw new IllegalArgumentException("Dịch vụ không tồn tại hoặc đã ngưng hoạt động");
        }

        validateOrderableService(service);

        Date now = new Date();
        MedicalRecordService recordService = new MedicalRecordService();
        recordService.setMedicalRecordId(medicalRecord);
        recordService.setServiceId(service);
        recordService.setQuantity(quantity);
        recordService.setUnitPrice(service.getPrice());
        recordService.setResultSummary(request.getResultSummary() != null ? request.getResultSummary().trim() : null);
        recordService.setCreatedAt(now);
        recordService.setUpdatedAt(now);
        recordService.setActive(true);

        this.medicalRecordServiceRepo.addMedicalRecordService(recordService);

        return MedicalRecordServiceMapper.toResponse(recordService, Collections.emptyList());
    }

    private void validateOrderableService(MedicalService service) {
        String serviceType = service.getServiceType();
        if (!MedicalServiceType.TEST.getCode().equalsIgnoreCase(serviceType)
                && !MedicalServiceType.IMAGING.getCode().equalsIgnoreCase(serviceType)) {
            throw new IllegalArgumentException("Bác sĩ chỉ có thể chỉ định xét nghiệm hoặc chẩn đoán hình ảnh");
        }
    }

    @Override
    public List<MedicalRecordServiceResponse> getServices(String username, Long recordId) {
        Doctor doctor = this.authSupport.requireCurrentDoctor(username);
        MedicalRecord medicalRecord = loadMedicalRecordForDoctor(doctor, recordId);
        List<MedicalRecordService> services = this.medicalRecordServiceRepo.getServicesByMedicalRecordId(medicalRecord.getId());
        Map<Long, List<TestResult>> resultsByServiceId = this.testResultRepo
                .getTestResultsByMedicalRecordId(medicalRecord.getId())
                .stream()
                .filter(r -> r.getServiceId() != null)
                .collect(Collectors.groupingBy(r -> r.getServiceId().getId()));

        return services.stream()
                .map(s -> MedicalRecordServiceMapper.toResponse(
                        s,
                        s.getServiceId() != null
                                ? resultsByServiceId.getOrDefault(s.getServiceId().getId(), Collections.emptyList())
                                : Collections.emptyList()
                ))
                .toList();
    }

    private void validateRequiredResultsCompleted(MedicalRecord medicalRecord) {
        List<MedicalRecordService> orderedServices = this.medicalRecordServiceRepo
                .getServicesByMedicalRecordId(medicalRecord.getId());

        List<MedicalRecordService> requiredServices = orderedServices.stream()
                .filter(service -> !Boolean.FALSE.equals(service.getActive()))
                .filter(this::isRequiredResultService)
                .toList();

        if (requiredServices.isEmpty()) {
            return;
        }

        var completedServiceIds = this.testResultRepo
                .getTestResultsByMedicalRecordId(medicalRecord.getId())
                .stream()
                .filter(result -> !Boolean.FALSE.equals(result.getActive()))
                .filter(result -> result.getServiceId() != null && result.getServiceId().getId() != null)
                .map(result -> result.getServiceId().getId())
                .collect(Collectors.toSet());

        List<String> pendingServices = requiredServices.stream()
                .filter(service -> service.getServiceId() == null
                        || service.getServiceId().getId() == null
                        || !completedServiceIds.contains(service.getServiceId().getId()))
                .map(service -> service.getServiceId() != null && service.getServiceId().getName() != null
                        ? service.getServiceId().getName()
                        : "Dịch vụ #" + service.getId())
                .distinct()
                .toList();

        if (!pendingServices.isEmpty()) {
            throw new IllegalStateException(
                    "Không thể hoàn tất bệnh án vì còn chỉ định chưa có kết quả: "
                            + String.join(", ", pendingServices)
            );
        }
    }

    private boolean isRequiredResultService(MedicalRecordService recordService) {
        if (recordService == null
                || recordService.getServiceId() == null
                || recordService.getServiceId().getServiceType() == null) {
            return false;
        }

        String serviceType = recordService.getServiceId().getServiceType().trim();
        return MedicalServiceType.TEST.getCode().equalsIgnoreCase(serviceType)
                || MedicalServiceType.IMAGING.getCode().equalsIgnoreCase(serviceType);
    }

    private boolean createOrUpdateUnpaidInvoice(MedicalRecord medicalRecord, Date now) {
        Invoice invoice = this.invoiceRepo.getInvoiceByMedicalRecordId(medicalRecord.getId());
        if (invoice != null && InvoiceStatus.PAID.getCode().equalsIgnoreCase(invoice.getPaymentStatus())) {
            medicalRecord.setInvoice(invoice);
            return false;
        }

        BigDecimal serviceAmount = calculateServiceAmount(medicalRecord);
        BigDecimal medicineAmount = calculateMedicineAmount(medicalRecord.getId());
        BigDecimal discountAmount = invoice != null && invoice.getDiscountAmount() != null
                ? invoice.getDiscountAmount()
                : BigDecimal.ZERO;
        BigDecimal totalAmount = serviceAmount.add(medicineAmount).subtract(discountAmount);

        if (invoice == null) {
            invoice = new Invoice();
            invoice.setInvoiceCode(generateInvoiceCode(medicalRecord.getId()));
            invoice.setMedicalRecordId(medicalRecord);
            invoice.setPatientId(medicalRecord.getPatientId());
            invoice.setDiscountAmount(discountAmount);
            invoice.setPaymentStatus(InvoiceStatus.UNPAID.getCode());
            invoice.setCreatedAt(now);
            invoice.setActive(true);
        } else {
            invoice.setPaymentStatus(InvoiceStatus.UNPAID.getCode());
        }

        invoice.setTotalServiceAmount(serviceAmount);
        invoice.setTotalMedicineAmount(medicineAmount);
        invoice.setTotalAmount(totalAmount.max(BigDecimal.ZERO));
        invoice.setUpdatedAt(now);
        medicalRecord.setInvoice(invoice);
        medicalRecord.setPaymentStatus(InvoiceStatus.UNPAID.getCode());

        if (invoice.getId() == null) {
            this.invoiceRepo.addInvoice(invoice);
        } else {
            this.invoiceRepo.updateInvoice(invoice);
        }

        return true;
    }

    private BigDecimal calculateServiceAmount(MedicalRecord medicalRecord) {
        BigDecimal appointmentServiceAmount = calculateAppointmentServiceAmount(medicalRecord);
        BigDecimal orderedServiceAmount = this.medicalRecordServiceRepo.getServicesByMedicalRecordId(medicalRecord.getId())
                .stream()
                .filter(service -> !Boolean.FALSE.equals(service.getActive()))
                .map(service -> {
                    BigDecimal unitPrice = service.getUnitPrice() != null ? service.getUnitPrice() : BigDecimal.ZERO;
                    int quantity = service.getQuantity() != null ? service.getQuantity() : 0;
                    return unitPrice.multiply(BigDecimal.valueOf(quantity));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return appointmentServiceAmount.add(orderedServiceAmount);
    }

    private BigDecimal calculateAppointmentServiceAmount(MedicalRecord medicalRecord) {
        if (medicalRecord.getAppointmentId() == null
                || medicalRecord.getAppointmentId().getServiceId() == null
                || Boolean.FALSE.equals(medicalRecord.getAppointmentId().getServiceId().getActive())) {
            return BigDecimal.ZERO;
        }

        MedicalService appointmentService = medicalRecord.getAppointmentId().getServiceId();
        return appointmentService.getPrice() != null ? appointmentService.getPrice() : BigDecimal.ZERO;
    }

    private BigDecimal calculateMedicineAmount(Long recordId) {
        Prescription prescription = this.prescriptionRepo.getPrescriptionByMedicalRecordId(recordId);
        if (prescription == null || prescription.getPrescriptionItemSet() == null) {
            return BigDecimal.ZERO;
        }

        return prescription.getPrescriptionItemSet()
                .stream()
                .filter(item -> !Boolean.FALSE.equals(item.getActive()))
                .map(this::calculateMedicineItemAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateMedicineItemAmount(PrescriptionItem item) {
        BigDecimal unitPrice = item.getUnitPrice() != null ? item.getUnitPrice() : BigDecimal.ZERO;
        int quantity = item.getQuantity() != null ? item.getQuantity() : 0;
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    private String generateInvoiceCode(Long recordId) {
        return String.format("INV%012d", recordId);
    }

    private void validateOwnedMedicalRecord(Doctor doctor, MedicalRecord medicalRecord) {
        if (medicalRecord.getDoctorId() == null
                || medicalRecord.getPatientId() == null
                || medicalRecord.getAppointmentId() == null) {
            throw new IllegalStateException("Bệnh án thiếu thông tin bác sĩ, bệnh nhân hoặc lịch hẹn");
        }

        if (!doctor.getId().equals(medicalRecord.getDoctorId().getId())) {
            throw new SecurityException("Chỉ bác sĩ phụ trách bệnh án mới được cập nhật");
        }
    }

    private void validateAppointmentNotCompleted(MedicalRecord medicalRecord) {
        Appointment appointment = medicalRecord.getAppointmentId();
        if (AppointmentStatus.COMPLETED.getCode().equalsIgnoreCase(appointment.getStatus())) {
            throw new IllegalStateException("Không thể sửa bệnh án khi lịch khám đã COMPLETED");
        }

        if (appointment.getMedicalRecord() != null
                && !medicalRecord.getId().equals(appointment.getMedicalRecord().getId())) {
            throw new IllegalStateException("Một lịch hẹn chỉ được gắn với một bệnh án");
        }
    }

    private MedicalRecord loadEditableMedicalRecordForDoctor(Doctor doctor, Long recordId) {
        MedicalRecord medicalRecord = loadMedicalRecordForDoctor(doctor, recordId);
        Appointment appointment = medicalRecord.getAppointmentId();

        if (!AppointmentStatus.IN_PROGRESS.getCode().equalsIgnoreCase(appointment.getStatus())) {
            throw new IllegalStateException("Chỉ có thể chỉ định dịch vụ khi lịch khám đang IN_PROGRESS");
        }

        return medicalRecord;
    }

    private MedicalRecord loadMedicalRecordForDoctor(Doctor doctor, Long recordId) {
        MedicalRecord medicalRecord = this.medicalRecordRepo.getMedicalRecordById(recordId);

        if (medicalRecord == null || Boolean.FALSE.equals(medicalRecord.getActive())) {
            throw new NoSuchElementException("Không tìm thấy bệnh án");
        }

        validateOwnedMedicalRecord(doctor, medicalRecord);

        return medicalRecord;
    }

    private void applyRequest(MedicalRecord medicalRecord, UpdateMedicalRecordRequest request) {
        if (request == null) {
            return;
        }

        if (request.getChiefComplaint() != null) {
            medicalRecord.setChiefComplaint(request.getChiefComplaint().trim());
        }

        if (request.getDiagnosis() != null) {
            medicalRecord.setDiagnosis(request.getDiagnosis().trim());
        }

        if (request.getTreatmentPlan() != null) {
            medicalRecord.setTreatmentPlan(request.getTreatmentPlan().trim());
        }

        if (request.getDoctorNote() != null) {
            medicalRecord.setDoctorNote(request.getDoctorNote().trim());
        }
    }

}
