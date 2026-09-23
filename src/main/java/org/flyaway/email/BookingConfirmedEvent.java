package org.flyaway.email;

import org.flyaway.booking.dto.BookingResponse;

/** Published inside the booking transaction; handled once the transaction commits. */
public record BookingConfirmedEvent(BookingResponse booking, String customerEmail) {
}
