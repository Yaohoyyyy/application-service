package com.application.contoller;

import com.application.model.VendorDto;
import com.application.service.VendorService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vendor")
public class VendorController {

    private final VendorService vendorService;

    public VendorController(VendorService vendorService) {
        this.vendorService = vendorService;
    }

    @GetMapping
    public List<VendorDto> getAllVendors() {
        return vendorService.getAllVendors();
    }

    @GetMapping("/{id}")
    public VendorDto getVendorById(@PathVariable Long id) {
        return vendorService.getVendorById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VendorDto createVendor(@Valid @RequestBody VendorDto vendorDto) {
        return vendorService.createVendor(vendorDto);
    }

    @PutMapping("/{id}")
    public VendorDto updateVendor(@PathVariable Long id, @Valid @RequestBody VendorDto vendorDto) {
        return vendorService.updateVendor(id, vendorDto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteVendor(@PathVariable Long id) {
        vendorService.deleteVendor(id);
    }
}