package org.flyaway.flight.domain;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.flyaway.exception.BadRequestException;
import org.flyaway.exception.ConflictException;
import org.flyaway.flight.dto.CreateFlightRequest;
import org.flyaway.flight.dto.FlightResponse;
import org.flyaway.flight.infrastructure.FlightRepository;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FlightService {

    private final FlightRepository flightRepository;

    @Transactional
    public FlightResponse create(CreateFlightRequest request) {
        if (flightRepository.existsByFlightNumber(request.flightNumber())) {
            throw new ConflictException("Flight number " + request.flightNumber() + " already exists");
        }

        Flight flight = Flight.builder()
                .flightNumber(request.flightNumber())
                .airline(request.airline().trim())
                .departureTime(request.departureTime())
                .arrivalTime(request.arrivalTime())
                .availableSeats(request.availableSeats())
                .build();

        return FlightResponse.from(flightRepository.save(flight));
    }

    @Transactional(readOnly = true)
    public List<FlightResponse> search(String flightNumber, String airline,
                                       LocalDateTime departureFrom, LocalDateTime departureTo) {
        if (departureFrom != null && departureTo != null && departureFrom.isAfter(departureTo)) {
            throw new BadRequestException("departureFrom must be before or equal to departureTo");
        }

        Specification<Flight> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (flightNumber != null && !flightNumber.isBlank()) {
                predicates.add(cb.like(cb.upper(root.get("flightNumber")),
                        "%" + flightNumber.trim().toUpperCase() + "%"));
            }
            if (airline != null && !airline.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("airline")),
                        "%" + airline.trim().toLowerCase() + "%"));
            }
            if (departureFrom != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("departureTime"), departureFrom));
            }
            if (departureTo != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("departureTime"), departureTo));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };

        return flightRepository.findAll(spec, Sort.by("departureTime")).stream()
                .map(FlightResponse::from)
                .toList();
    }
}
