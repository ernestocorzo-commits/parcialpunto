package org.flyaway.email;

import lombok.extern.slf4j.Slf4j;
import org.flyaway.booking.dto.BookingResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Simulates the confirmation email by writing flight_booking_email_${booking_id}.txt
 * once the booking has been committed. All dates are ISO 8601.
 */
@Slf4j
@Component
public class BookingEmailWriter {

    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final Path outputDir;

    public BookingEmailWriter(@Value("${booking.email.dir}") String outputDir) {
        this.outputDir = Path.of(outputDir);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onBookingConfirmed(BookingConfirmedEvent event) {
        BookingResponse booking = event.booking();
        Path file = outputDir.resolve("flight_booking_email_" + booking.id() + ".txt");
        try {
            Files.createDirectories(outputDir);
            Files.writeString(file, render(booking, event.customerEmail()), StandardCharsets.UTF_8);
            log.info("Booking confirmation email written to {}", file.toAbsolutePath());
        } catch (IOException e) {
            // The booking is already committed; a failed email must not turn it into an error.
            log.error("Could not write booking confirmation email {}", file.toAbsolutePath(), e);
        }
    }

    private String render(BookingResponse b, String customerEmail) {
        return """
                To: %s
                Subject: Fly Away Travel - Booking confirmation #%d

                Hello %s %s,

                Your booking has been confirmed.

                Booking ID:     %d
                Passenger:      %s %s
                Flight number:  %s
                Airline:        %s
                Departure:      %s
                Arrival:        %s
                Booking date:   %s

                Thank you for flying with Fly Away Travel!
                """.formatted(
                customerEmail, b.id(),
                b.customerFirstName(), b.customerLastName(),
                b.id(),
                b.customerFirstName(), b.customerLastName(),
                b.flightNumber(),
                b.airline(),
                iso(b.departureTime()),
                iso(b.arrivalTime()),
                iso(b.bookingDate())
        );
    }

    private static String iso(LocalDateTime dateTime) {
        return dateTime.format(ISO);
    }
}
