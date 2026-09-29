package com.medix.orders.service;

import com.medix.orders.domain.Order;
import com.medix.orders.domain.OrderStatus;
import com.medix.orders.domain.Prescription;
import com.medix.orders.external.ExternalClients;
import com.medix.orders.repo.OrderRepository;
import com.medix.orders.repo.PrescriptionRepository;
import com.medix.orders.web.dto.CheckoutRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.UUID;

/**
 * Performs the full fulfilment sequence for a checkout. The prescription must already
 * have been approved by a pharmacist and re-entered by staff into the order flow.
 */
@Service
public class CheckoutService {

    private static final Logger log = LoggerFactory.getLogger(CheckoutService.class);

    private final OrderRepository orderRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final ExternalClients externalClients;
    private final Path uploadDir;

    public CheckoutService(OrderRepository orderRepository,
                           PrescriptionRepository prescriptionRepository,
                           ExternalClients externalClients,
                           @Value("${pharmacy.upload-dir}") String uploadDir) {
        this.orderRepository = orderRepository;
        this.prescriptionRepository = prescriptionRepository;
        this.externalClients = externalClients;
        this.uploadDir = Paths.get(uploadDir);
    }

    /**
     * Runs the entire checkout inline: store the prescription image on local disk, write
     * the order, capture payment, file the insurer claim and book the courier. Every
     * external call is executed within the same synchronous checkout transaction before a
     * response is returned to the customer.
     */
    @Transactional
    public Order checkout(CheckoutRequest request, MultipartFile prescriptionImage) {
        Order order = new Order();
        order.setCustomerId(request.customerId());
        order.setCountry(request.country());
        order.setAmount(request.amount());
        order.setCurrency(request.currency());

        Prescription rx = storePrescription(request.customerId(), prescriptionImage);
        order.setPrescriptionId(rx.getId());
        order.moveTo(OrderStatus.RX_STORED);
        order = orderRepository.save(order);

        String captureId = externalClients.capturePayment(
                order.getCustomerId(), order.getAmount(), order.getCurrency());
        order.setPaymentReference(captureId);
        order.moveTo(OrderStatus.PAID);
        order = orderRepository.save(order);

        String claimId = externalClients.fileClaim(
                order.getId(), order.getCountry(), order.getAmount());
        order.setClaimReference(claimId);
        order.moveTo(OrderStatus.CLAIM_FILED);
        order = orderRepository.save(order);

        String bookingId = externalClients.bookCourier(order.getId(), order.getCountry());
        order.setCourierBookingId(bookingId);
        order.moveTo(OrderStatus.COMPLETED);
        order = orderRepository.save(order);

        log.info("checkout completed order={} claim={} booking={}",
                order.getId(), claimId, bookingId);
        return order;
    }

    private Prescription storePrescription(String customerId, MultipartFile image) {
        try {
            Files.createDirectories(uploadDir);
            String fileName = UUID.randomUUID() + "-" + image.getOriginalFilename();
            Path target = uploadDir.resolve(fileName);
            image.transferTo(target);

            Prescription rx = new Prescription();
            rx.setCustomerId(customerId);
            rx.setImagePath(target.toString());
            rx.setReviewStatus(Prescription.ReviewStatus.APPROVED);
            rx.setReviewedAt(Instant.now());
            return prescriptionRepository.save(rx);
        } catch (IOException e) {
            throw new UncheckedIOException("failed to store prescription image", e);
        }
    }
}
