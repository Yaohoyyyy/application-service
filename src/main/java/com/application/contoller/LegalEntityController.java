package com.application.contoller;

import com.application.model.LegalEntityRequest;
import com.application.model.LegalEntityResponse;
import com.application.service.LegalEntityService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/jur")
public class LegalEntityController {

    private final LegalEntityService legalEntityService;

    public LegalEntityController(LegalEntityService legalEntityService) {
        this.legalEntityService = legalEntityService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Long createLegalEntity(@Valid @RequestBody LegalEntityRequest request) {
        return legalEntityService.createLegalEntity(request);
    }

    @GetMapping("/{id}")
    public LegalEntityResponse getLegalEntity(@PathVariable Long id) {
        return legalEntityService.getLegalEntity(id);
    }
}