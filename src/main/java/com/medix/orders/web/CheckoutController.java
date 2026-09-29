package com.medix.orders.web;

import com.medix.orders.domain.Order;
import com.medix.orders.service.CheckoutService;
import com.medix.orders.web.dto.CheckoutRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/checkout")
public class CheckoutController {

    private final CheckoutService checkoutService;

    public CheckoutController(CheckoutService checkoutService) {
        this.checkoutService = checkoutService;
    }

    /**
     * Single blocking checkout endpoint. The response is only returned once payment,
     * the insurer claim and courier booking have all completed inline.
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Order> checkout(
            @Valid @RequestPart("order") CheckoutRequest request,
            @RequestPart("prescription") MultipartFile prescriptionImage) {
        Order order = checkoutService.checkout(request, prescriptionImage);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }
}
