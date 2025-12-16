package com.evote.app.citizen_management.infrastructure;

import com.evote.app.citizen_management.application.dto.*;
import com.evote.app.citizen_management.application.services.CitizenService;
import com.evote.app.citizen_management.application.services.TokenService;
import com.evote.app.citizen_management.domain.model.Citizen;
import com.evote.app.citizen_management.exceptions.UserAlreadyExistsException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/citizens")
public class CitizenRestController {

    private final CitizenService citizenService;

    public CitizenRestController(CitizenService citizenService) {
        this.citizenService = citizenService;
    }


    @PostMapping("/register")
    public CitizenRegistrationResponseDto register(
            @RequestBody CitizenRegistrationRequestDto request
    ) throws UserAlreadyExistsException {
        Citizen c = citizenService.registerCitizen(request);

        return CitizenRegistrationResponseDto.fromDomain(c);
    }


    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody CitizenLoginRequestDto request, HttpServletResponse response) {
        boolean b = citizenService.loginCitizen(
                request.email(),
                request.password()
        );

        if (!b) {
            return ResponseEntity.status(HttpServletResponse.SC_UNAUTHORIZED).build();
        }

        String token = TokenService.generateToken(request.email());
        System.out.println((token));
        response.addHeader("Authorization", "Bearer " + token);
        return ResponseEntity.ok(token);
    }

    @GetMapping("/user")
    public CitizenResponseDto getLoggedInCitizen (HttpServletRequest request) {
        return CitizenResponseDto.fromDomain(citizenService.getCurrentLoggedInCitizen());

    }


}
