package com.evercare.services.impl;

import com.evercare.dtos.request.MedicineRequest;
import com.evercare.dtos.response.MedicineLowStockResponse;
import com.evercare.dtos.response.MedicineResponse;
import com.evercare.dtos.response.MedicineSearchResponse;
import com.evercare.enums.MedicineUnit;
import com.evercare.mappers.MedicineMapper;
import com.evercare.pojo.Medicine;
import com.evercare.repositories.MedicineBatchRepository;
import com.evercare.repositories.MedicineRepository;
import com.evercare.services.MedicineService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class MedicineServiceImpl implements MedicineService {
    @Autowired
    private MedicineRepository medicineRepo;
    @Autowired
    private MedicineBatchRepository batchRepo;

    @Override
    public List<MedicineResponse> getMedicines(Map<String, String> params) {
        return this.medicineRepo.getMedicines(params)
                .stream()
                .map(MedicineMapper::toResponse)
                .toList();
    }

    @Override
    public List<MedicineSearchResponse> searchMedicines(Map<String, String> params) {
        Date today = java.sql.Date.valueOf(LocalDate.now());
        List<Medicine> medicines = this.medicineRepo.getMedicines(params);
        Map<Long, Long> availableQuantities = this.batchRepo.getAvailableNonExpiredQuantitiesByMedicineIds(
                medicines.stream()
                        .map(Medicine::getId)
                        .toList(),
                today
        );

        return medicines
                .stream()
                .map(medicine -> {
                    MedicineSearchResponse res = new MedicineSearchResponse();
                    res.setMedicineId(medicine.getId());
                    res.setMedicineName(medicine.getName());
                    res.setUnit(medicine.getUnit());
                    res.setUnitPrice(medicine.getUnitPrice());
                    res.setAvailableQuantity(availableQuantities.getOrDefault(medicine.getId(), 0L));

                    return res;
                })
                .toList();
    }

    @Override
    public List<MedicineLowStockResponse> getLowStockMedicines() {
        return this.medicineRepo.getLowStockMedicines()
                .stream()
                .map(row -> {
                    Medicine medicine = (Medicine) row[0];
                    Number totalRemaining = (Number) row[1];

                    MedicineLowStockResponse res = new MedicineLowStockResponse();
                    res.setId(medicine.getId());
                    res.setMedicineCode(medicine.getMedicineCode());
                    res.setName(medicine.getName());
                    res.setUnit(medicine.getUnit());
                    res.setUnitPrice(medicine.getUnitPrice());
                    res.setMinStockQuantity(medicine.getMinStockQuantity());
                    res.setTotalRemainingQuantity(totalRemaining != null ? totalRemaining.longValue() : 0L);

                    return res;
                })
                .toList();
    }

    @Override
    public MedicineResponse getMedicineById(Long id) {
        Medicine medicine = this.medicineRepo.getMedicineById(id);

        if (medicine == null || Boolean.FALSE.equals(medicine.getActive())) {
            throw new NoSuchElementException("Không tìm thấy thuốc");
        }

        return MedicineMapper.toResponse(medicine);
    }

    @Override
    public MedicineResponse createMedicine(MedicineRequest request) {
        validateMedicineRequest(request, null);

        Date now = new Date();
        Medicine medicine = new Medicine();
        MedicineMapper.updateEntity(medicine, request);
        medicine.setCreatedAt(now);
        medicine.setUpdatedAt(now);
        medicine.setActive(true);

        this.medicineRepo.addMedicine(medicine);

        return MedicineMapper.toResponse(medicine);
    }

    @Override
    public MedicineResponse updateMedicine(Long id, MedicineRequest request) {
        Medicine medicine = loadMedicine(id);
        validateMedicineRequest(request, id);
        MedicineMapper.updateEntity(medicine, request);
        medicine.setUpdatedAt(new Date());

        this.medicineRepo.updateMedicine(medicine);

        return MedicineMapper.toResponse(medicine);
    }

    @Override
    public MedicineResponse updateStatus(Long id, Boolean active) {
        if (active == null) {
            throw new IllegalArgumentException("Vui lòng truyền trạng thái active");
        }

        Medicine medicine = loadMedicine(id);
        medicine.setActive(active);
        medicine.setUpdatedAt(new Date());
        this.medicineRepo.updateMedicine(medicine);

        return MedicineMapper.toResponse(medicine);
    }

    @Override
    public void softDelete(Long id) {
        Medicine medicine = loadMedicine(id);

        if (this.medicineRepo.hasPrescriptionItems(id)) {
            medicine.setActive(false);
            medicine.setUpdatedAt(new Date());
            this.medicineRepo.updateMedicine(medicine);
            return;
        }

        medicine.setActive(false);
        medicine.setUpdatedAt(new Date());
        this.medicineRepo.updateMedicine(medicine);
    }

    private Medicine loadMedicine(Long id) {
        Medicine medicine = this.medicineRepo.getMedicineById(id);

        if (medicine == null) {
            throw new NoSuchElementException("Không tìm thấy thuốc");
        }

        return medicine;
    }

    private void validateMedicineRequest(MedicineRequest request, Long excludeId) {
        if (request == null) {
            throw new IllegalArgumentException("Dữ liệu thuốc không hợp lệ");
        }

        if (request.getMedicineCode() == null || request.getMedicineCode().isBlank()) {
            throw new IllegalArgumentException("Mã thuốc không được để trống");
        }

        request.setMedicineCode(request.getMedicineCode().trim().toUpperCase());

        if (this.medicineRepo.existsByCode(request.getMedicineCode(), excludeId)) {
            throw new IllegalArgumentException("Mã thuốc đã tồn tại");
        }

        if (request.getName() == null || request.getName().isBlank()) {
            throw new IllegalArgumentException("Tên thuốc không được để trống");
        }

        request.setName(request.getName().trim());
        request.setUnit(MedicineUnit.normalize(request.getUnit()));

        if (request.getUnitPrice() == null) {
            request.setUnitPrice(BigDecimal.ZERO);
        }

        if (request.getUnitPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Đơn giá thuốc không được âm");
        }

        if (request.getMinStockQuantity() == null) {
            request.setMinStockQuantity(0);
        }

        if (request.getMinStockQuantity() < 0) {
            throw new IllegalArgumentException("Tồn kho tối thiểu không được âm");
        }

        if (request.getDescription() != null) {
            request.setDescription(request.getDescription().trim());
        }

        if (request.getUsageNote() != null) {
            request.setUsageNote(request.getUsageNote().trim());
        }
    }
}
