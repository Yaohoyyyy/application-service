package com.application.service;

import com.application.entity.Vendor;
import com.application.model.VendorDto;
import com.application.repository.VendorRepository;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class VendorService {

    private static final String CACHE_NAME = "vendors";
    private static final String ALL_KEY = "all";

    private final VendorRepository vendorRepository;
    private final HazelcastInstance hazelcastInstance;
    private final long ttlSeconds;

    public VendorService(VendorRepository vendorRepository,
                         HazelcastInstance hazelcastInstance,
                         @Value("${app.vendor.cache.ttl-seconds:30}") long ttlSeconds) {
        this.vendorRepository = vendorRepository;
        this.hazelcastInstance = hazelcastInstance;
        this.ttlSeconds = ttlSeconds;
    }

    private IMap<String, Object> vendors() {
        return hazelcastInstance.getMap(CACHE_NAME);
    }

    /** Кидаем значение в кэш с TTL (время жизни задаётся на каждый entry). */
    private void cache(String key, Object value) {
        vendors().put(key, value, ttlSeconds, TimeUnit.SECONDS);
    }

    @SuppressWarnings("unchecked")
    public List<VendorDto> getAllVendors() {
        List<VendorDto> cached = (List<VendorDto>) vendors().get(ALL_KEY);
        if (cached != null) {
            return cached;
        }
        List<VendorDto> result = vendorRepository.findAll().stream().map(VendorDto::fromEntity).toList();
        cache(ALL_KEY, result);
        return result;
    }

    public VendorDto getVendorById(Long id) {
        String key = String.valueOf(id);
        VendorDto cached = (VendorDto) vendors().get(key);
        if (cached != null) {
            return cached;
        }
        VendorDto result = VendorDto.fromEntity(findVendorById(id));
        cache(key, result);
        return result;
    }

    @Transactional
    public VendorDto createVendor(VendorDto vendorDto) {
        Vendor vendor = new Vendor(vendorDto.name(), vendorDto.active());
        VendorDto result = VendorDto.fromEntity(vendorRepository.save(vendor));
        cache(String.valueOf(result.id()), result);
        vendors().remove(ALL_KEY);
        return result;
    }

    @Transactional
    public VendorDto updateVendor(Long id, VendorDto vendorDto) {
        Vendor vendor = findVendorById(id);
        vendor.setName(vendorDto.name());
        vendor.setActive(vendorDto.active());
        VendorDto result = VendorDto.fromEntity(vendorRepository.save(vendor));
        cache(String.valueOf(id), result);
        vendors().remove(ALL_KEY);
        return result;
    }

    @Transactional
    public void deleteVendor(Long id) {
        Vendor vendor = findVendorById(id);
        vendorRepository.delete(vendor);
        vendors().remove(String.valueOf(id));
        vendors().remove(ALL_KEY);
    }

    private Vendor findVendorById(Long id) {
        return vendorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("vendor not found with id: " + id));
    }
}