package org.flyaway.booking.application;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.flyaway.booking.domain.BookingService;
import org.flyaway.booking.dto.BookFlightRequest;
import org.flyaway.booking.dto.BookingResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping("/flights/book")
    public ResponseEntity<BookingResponse> book(@Valid @RequestBody BookFlightRequest request,
                                                @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(bookingService.book(request, principal.getUsername()));
    }

    @GetMapping("/flight/book/{id}")
    public ResponseEntity<BookingResponse> getById(@PathVariable Long id,
                                                   @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(bookingService.getById(id, principal.getUsername()));
    }
}
