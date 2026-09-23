package org.flyaway.booking.domain;

import lombok.RequiredArgsConstructor;
import org.flyaway.booking.dto.BookFlightRequest;
import org.flyaway.booking.dto.BookingResponse;
import org.flyaway.booking.infrastructure.BookingRepository;
import org.flyaway.email.BookingConfirmedEvent;
import org.flyaway.exception.BadRequestException;
import org.flyaway.exception.ConflictException;
import org.flyaway.exception.ForbiddenException;
import org.flyaway.exception.ResourceNotFoundException;
import org.flyaway.flight.domain.Flight;
import org.flyaway.flight.infrastructure.FlightRepository;
import org.flyaway.user.domain.User;
import org.flyaway.user.domain.UserService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final FlightRepository flightRepository;
    private final UserService userService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public BookingResponse book(BookFlightRequest request, String customerEmail) {
        User customer = userService.getByEmail(customerEmail);
        Flight flight = flightRepository.findByIdForUpdate(request.flightId())
                .orElseThrow(() -> new ResourceNotFoundException("Flight " + request.flightId() + " not found"));

        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

        if (!flight.getDepartureTime().isAfter(now)) {
            boolean inTransit = flight.getArrivalTime().isAfter(now);
            throw new BadRequestException(inTransit
                    ? "Flight " + flight.getFlightNumber() + " is already in transit"
                    : "Flight " + flight.getFlightNumber() + " has already departed");
        }

        if (flight.getAvailableSeats() <= 0) {
            throw new ConflictException("Flight " + flight.getFlightNumber() + " has no available seats");
        }

        if (bookingRepository.existsOverlappingBooking(
                customer.getId(), flight.getDepartureTime(), flight.getArrivalTime())) {
            throw new ConflictException("You already have a booking that overlaps with flight "
                    + flight.getFlightNumber());
        }

        flight.setAvailableSeats(flight.getAvailableSeats() - 1);

        Booking booking = bookingRepository.save(Booking.builder()
                .flight(flight)
                .customer(customer)
                .customerFirstName(customer.getFirstName())
                .customerLastName(customer.getLastName())
                .bookingDate(now)
                .build());

        BookingResponse response = BookingResponse.from(booking);
        eventPublisher.publishEvent(new BookingConfirmedEvent(response, customer.getEmail()));
        return response;
    }

    @Transactional(readOnly = true)
    public BookingResponse getById(Long id, String requesterEmail) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking " + id + " not found"));

        if (!booking.getCustomer().getEmail().equals(requesterEmail)) {
            throw new ForbiddenException("You can only view your own bookings");
        }
        return BookingResponse.from(booking);
    }
}
