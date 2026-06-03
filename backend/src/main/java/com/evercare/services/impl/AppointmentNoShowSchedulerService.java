package com.evercare.services.impl;

import com.evercare.enums.AppointmentStatus;
import com.evercare.pojo.Appointment;
import com.evercare.repositories.AppointmentRepository;
import com.evercare.utils.DateTimeUtils;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AppointmentNoShowSchedulerService {
    private static final Logger logger = LoggerFactory.getLogger(AppointmentNoShowSchedulerService.class);

    @Autowired
    private AppointmentRepository appointmentRepo;

    @Scheduled(cron = "0 * * * * *")
    @Transactional
    public void markOverdueAppointmentsAsNoShow() {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        List<Appointment> candidates = this.appointmentRepo.getAppointmentsEligibleForNoShow(today);
        if (candidates == null || candidates.isEmpty()) {
            return;
        }

        int updatedCount = 0;
        for (Appointment appointment : candidates) {
            if (appointment == null || appointment.getAppointmentDate() == null || appointment.getStartTime() == null) {
                continue;
            }

            LocalDate appointmentDate = DateTimeUtils.toLocalDate(appointment.getAppointmentDate());
            LocalTime cutoffTime = DateTimeUtils.toNormalizedLocalTime(
                    appointment.getEndTime() != null ? appointment.getEndTime() : appointment.getStartTime()
            );
            if (appointmentDate == null || cutoffTime == null) {
                continue;
            }

            boolean overdue = appointmentDate.isBefore(today)
                    || (appointmentDate.isEqual(today) && cutoffTime.isBefore(now));
            if (!overdue) {
                continue;
            }

            appointment.setStatus(AppointmentStatus.NO_SHOW.getCode());
            appointment.setUpdatedAt(new Date());
            this.appointmentRepo.updateAppointment(appointment);
            updatedCount++;
        }

        if (updatedCount > 0) {
            logger.info("Marked {} overdue appointments as NO_SHOW", updatedCount);
        }
    }

}
