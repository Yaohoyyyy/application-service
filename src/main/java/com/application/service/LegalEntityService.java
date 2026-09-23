package com.application.service;

import com.application.model.LegalEntityIdResponse;
import com.application.model.LegalEntityRequest;
import com.application.model.LegalEntityResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class LegalEntityService {

    private static final String JUR_PATH = "/jur";

    private final RestClient restClient;

    public LegalEntityService(@Value("${app.external.jur.base-url}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    public Long createLegalEntity(LegalEntityRequest request) {
        return restClient.post()
                .uri(JUR_PATH)
                .header("proxyInn", request.inn())
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(LegalEntityIdResponse.class)
                .id();
    }

    public LegalEntityResponse getLegalEntity(Long id) {
        return restClient.get()
                .uri(JUR_PATH + "/" + id)
                .retrieve()
                .body(LegalEntityResponse.class);
    }
}