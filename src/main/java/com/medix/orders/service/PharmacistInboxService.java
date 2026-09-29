package com.medix.orders.service;

import com.medix.orders.domain.Prescription;
import com.medix.orders.repo.PrescriptionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * Backs the pharmacist shared inbox. Pharmacists list the pending prescriptions, pick
 * whichever one they want, and approve or reject it. There is no assignment step, so the
 * same prescription can be opened and approved by more than one pharmacist.
 */
@Service
public class PharmacistInboxService {

    private static final Logger log = LoggerFactory.getLogger(PharmacistInboxService.class);

    private final PrescriptionRepository prescriptionRepository;

    public PharmacistInboxService(PrescriptionRepository prescriptionRepository) {
        this.prescriptionRepository = prescriptionRepository;
    }

    /** Returns the shared pending inbox, oldest upload first. */
    public List<Prescription> pendingInbox() {
        return prescriptionRepository.findPendingInbox();
    }

    @Transactional
    public Prescription approve(Long prescriptionId, String pharmacistId) {
        Prescription rx = prescriptionRepository.findById(prescriptionId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "prescription not found: " + prescriptionId));
        rx.setReviewStatus(Prescription.ReviewStatus.APPROVED);
        rx.setReviewedBy(pharmacistId);
        rx.setReviewedAt(Instant.now());
        log.info("review_event prescription_id={} approved by {}", prescriptionId, pharmacistId);
        return prescriptionRepository.save(rx);
    }

    @Transactional
    public Prescription reject(Long prescriptionId, String pharmacistId) {
        Prescription rx = prescriptionRepository.findById(prescriptionId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "prescription not found: " + prescriptionId));
        rx.setReviewStatus(Prescription.ReviewStatus.REJECTED);
        rx.setReviewedBy(pharmacistId);
        rx.setReviewedAt(Instant.now());
        log.info("review_event prescription_id={} rejected by {}", prescriptionId, pharmacistId);
        return prescriptionRepository.save(rx);
    }
}
