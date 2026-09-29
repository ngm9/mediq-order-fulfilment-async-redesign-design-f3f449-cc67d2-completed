# Mediq Order Fulfilment Async Redesign Design

Web and mobile clients hit an nginx load balancer that forwards to one Order Monolith, which performs the entire fulfilment sequence inline within the checkout HTTP request: it stores the prescription image to local disk, writes the order to a single PostgreSQL instance, calls the external Payment Gateway to capture payment, then calls the external Insurer Claim API, and finally calls the Courier Booking API — all before returning a response. Prescription review is the one step that is not inline: pharmacists open a shared, unassigned PostgreSQL view of 'pending' prescriptions sorted by upload time, pick whatever they want, and after approving, a staff member manually re-enters the approved order back into the order flow.

This repository is the Java implementation of that system.

## Helpful Tips

- Read `docs/DESIGN.md` — the design document — for the full background on this task.
- Review the source code below to see exactly how the current system works.
- On the Utkrusht whiteboard for this task, the current system is already drawn and locked for reference — draw your redesigned architecture in the same High-Level Design area.
- You're welcome to update `docs/DESIGN.md` or add your own files to this repository to capture your design notes as you work.

## Objectives
Please commit an updated DESIGN.md capturing your rationale, decisions and assumptions. Also update the excalidraw diagram to show the changes you propose. You are tasked with the following changes:
- Redesign the fulfilment flow so checkout stays fast while slow steps run in the background.
- Ensure a slow insurer or courier never loses or duplicates an order.
- Design the pharmacist review flow with one owner per order and a 30-minute escalation.
- Make every order state change traceable and auditable.
- Complete the remaining board sections: NFRs, API routes, trade-offs, capacity, failure modes.

## What's in this repository

- `docs/DESIGN.md` — the design document
- The current implementation, organized as a standard Java/Maven project:
  - Order Monolith — `src/main/java/com/medix/orders/OrderMonolithApplication.java`, `src/main/java/com/medix/orders/service/CheckoutService.java`, `src/main/java/com/medix/orders/web/ApiExceptionHandler.java`, `src/main/java/com/medix/orders/web/CheckoutController.java`, `src/main/java/com/medix/orders/web/dto/CheckoutRequest.java`
  - PostgreSQL — `src/main/java/com/medix/orders/domain/Order.java`, `src/main/java/com/medix/orders/domain/OrderStatus.java`, `src/main/java/com/medix/orders/domain/Prescription.java`, `src/main/java/com/medix/orders/repo/OrderRepository.java`, `src/main/java/com/medix/orders/repo/PrescriptionRepository.java`
  - Payment Gateway — `src/main/java/com/medix/orders/external/ExternalClients.java`
  - Pharmacist Shared Inbox — `src/main/java/com/medix/orders/service/PharmacistInboxService.java`, `src/main/java/com/medix/orders/web/PharmacistController.java`
