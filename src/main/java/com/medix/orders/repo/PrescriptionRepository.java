package com.medix.orders.repo;

import com.medix.orders.domain.Prescription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {

    /**
     * The pharmacist shared inbox: every pending prescription across all pharmacists,
     * sorted by upload time. There is no assignment or locking column, so any pharmacist
     * sees and can pick any row.
     */
    @Query("select p from Prescription p where p.reviewStatus = com.medix.orders.domain.Prescription$ReviewStatus.PENDING order by p.uploadedAt asc")
    List<Prescription> findPendingInbox();
}
