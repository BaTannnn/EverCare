package com.evercare.services.impl;

import com.evercare.dtos.response.PrescriptionResponse;
import com.evercare.mappers.PrescriptionMapper;
import com.evercare.pojo.Prescription;
import com.evercare.repositories.MedicineBatchRepository;
import com.evercare.repositories.PrescriptionRepository;
import com.evercare.services.PrescriptionService;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PrescriptionServiceImpl implements PrescriptionService {
    @Autowired
    private PrescriptionRepository prescriptionRepo;

    @Autowired
    private MedicineBatchRepository batchRepo;

    @Override
    public List<PrescriptionResponse> getPrescriptions(Map<String, String> params) {
        return this.prescriptionRepo.getPrescriptions(params)
                .stream()
                .map(p -> PrescriptionMapper.toResponse(p, this.batchRepo::getAvailableQuantityByMedicineId))
                .toList();
    }

    @Override
    public PrescriptionResponse getPrescriptionById(Long id) {
        Prescription prescription = this.prescriptionRepo.getPrescriptionById(id);

        if (prescription == null) {
            throw new NoSuchElementException("Không tìm thấy đơn thuốc");
        }

        return PrescriptionMapper.toResponse(prescription, this.batchRepo::getAvailableQuantityByMedicineId);
    }
}
