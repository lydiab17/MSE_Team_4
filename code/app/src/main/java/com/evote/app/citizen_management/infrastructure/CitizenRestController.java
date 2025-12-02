package com.evote.app.citizen_management.infrastructure;

import com.evote.app.citizen_management.application.dto.CitizenLoginRequestDto;
import com.evote.app.citizen_management.application.dto.CitizenRegistrationRequestDto;
import com.evote.app.citizen_management.application.dto.CitizenRegistrationResponseDto;
import com.evote.app.citizen_management.application.services.CitizenService;
import com.evote.app.citizen_management.domain.model.Citizen;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


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
    ) {
        Citizen c = citizenService.registerCitizen(
                request.firstName(),
                request.lastName(),
                request.email(),
                request.password()
        );

        return CitizenRegistrationResponseDto.fromDomain(c);
    }


    @PostMapping("/login")
    public Boolean login (@RequestBody CitizenLoginRequestDto request) {
        Boolean b = citizenService.loginCitizen(
                request.email(),
                request.password()
        );
        return b;
    }




}
