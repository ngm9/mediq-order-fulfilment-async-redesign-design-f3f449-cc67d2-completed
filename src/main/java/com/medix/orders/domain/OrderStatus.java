package com.medix.orders.domain;

import java.util.EnumSet;
import java.util.Set;

/**
 * Lifecycle states an order moves through during inline checkout.
 *
 * <p>Because the whole fulfilment sequence runs synchronously inside a single checkout
 * transaction, most orders are only ever observed as {@link #NEW} or {@link #COMPLETED}.
 * Intermediate states such as {@link #PAID} become visible only when a downstream call
 * (insurer or courier) fails after payment has already been captured and the row was
 * flushed before the failing step.
 */
public enum OrderStatus {

    /** Order row created, nothing charged yet. */
    NEW,

    /** Prescription image persisted to the upload store. */
    RX_STORED,

    /** Payment gateway confirmed the capture. */
    PAID,

    /** Insurer accepted the claim (or it was recorded as filed). */
    CLAIM_FILED,

    /** Courier booking succeeded; order is fully fulfilled. */
    COMPLETED,

    /** Checkout aborted before any money moved. */
    FAILED;

    private static final Set<OrderStatus> TERMINAL = EnumSet.of(COMPLETED, FAILED);

    public boolean isTerminal() {
        return TERMINAL.contains(this);
    }

    public boolean canTransitionTo(OrderStatus target) {
        if (this == target) {
            return true;
        }
        return switch (this) {
            case NEW -> target == RX_STORED || target == FAILED;
            case RX_STORED -> target == PAID || target == FAILED;
            case PAID -> target == CLAIM_FILED || target == FAILED;
            case CLAIM_FILED -> target == COMPLETED || target == FAILED;
            case COMPLETED, FAILED -> false;
        };
    }

    public OrderStatus advanceTo(OrderStatus target) {
        if (!canTransitionTo(target)) {
            throw new IllegalStateException(
                    "Illegal order transition " + this + " -> " + target);
        }
        return target;
    }
}
