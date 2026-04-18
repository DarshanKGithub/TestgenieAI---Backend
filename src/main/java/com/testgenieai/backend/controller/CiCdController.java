package com.testgenieai.backend.controller;

import com.testgenieai.backend.dto.CiCdTriggerRequest;
import com.testgenieai.backend.dto.CiCdTriggerResponse;
import com.testgenieai.backend.service.CiCdTriggerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/cicd")
@RequiredArgsConstructor
public class CiCdController {

    private final CiCdTriggerService ciCdTriggerService;

    @PostMapping("/trigger")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public CiCdTriggerResponse trigger(
            @Valid @RequestBody CiCdTriggerRequest request,
            @RequestHeader(name = "X-TestGenie-CI-Secret", required = false) String secret
    ) {
        return ciCdTriggerService.trigger(request, secret);
    }
}
