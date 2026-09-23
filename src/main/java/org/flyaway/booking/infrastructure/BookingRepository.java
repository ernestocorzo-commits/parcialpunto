package org.flyaway.booking.infrastructure;

import org.flyaway.booking.domain.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    /** True if the customer already has a booking whose flight overlaps [departure, arrival). */
    @Query("""
            SELECT COUNT(b) > 0 FROM Booking b
            WHERE b.customer.id = :customerId
              AND b.flight.departureTime < :arrival
              AND b.flight.arrivalTime > :departure
            """)
    boolean existsOverlappingBooking(@Param("customerId") Long customerId,
                                     @Param("departure") LocalDateTime departure,
                                     @Param("arrival") LocalDateTime arrival);
}
