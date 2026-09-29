package com.medix.orders.web;

import com.medix.orders.domain.Prescription;
import com.medix.orders.service.PharmacistInboxService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/pharmacist")
public class PharmacistController {

    private final PharmacistInboxService inboxService;

    public PharmacistController(PharmacistInboxService inboxService) {
        this.inboxService = inboxService;
    }

    /** Shared pending inbox open to every pharmacist. */
    @GetMapping("/inbox")
    public List<Prescription> inbox() {
        return inboxService.pendingInbox();
    }

    @PostMapping("/prescriptions/{id}/approve")
    public Prescription approve(@PathVariable Long id,
                                @RequestParam("pharmacistId") String pharmacistId) {
        return inboxService.approve(id, pharmacistId);
    }

    @PostMapping("/prescriptions/{id}/reject")
    public Prescription reject(@PathVariable Long id,
                               @RequestParam("pharmacistId") String pharmacistId) {
        return inboxService.reject(id, pharmacistId);
    }
}
