package org.flyaway.booking.dto;

import jakarta.validation.constraints.NotNull;

public record BookFlightRequest(
        @NotNull(message = "flightId is required")
        Long flightId
) {
}
