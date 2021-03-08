package com.github.vikusku.happytires.controller;

import com.github.vikusku.happytires.dto.ServiceProviderDto;
import com.github.vikusku.happytires.model.ServiceProvider;
import com.github.vikusku.happytires.service.ServiceProviderService;
import com.github.vikusku.happytires.util.ModelMapperUtil;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping(path = "/api/v1/serviceproviders")
public class ServiceProviderController {

    @Autowired
    private ServiceProviderService serviceProviderService;

    @GetMapping
    public ResponseEntity<List<ServiceProviderDto>> getAll() {
        return ResponseEntity.ok(serviceProviderService.getAll());
    }

    // TODO use ServiceProviderNotFound exception
    @GetMapping("/{id}")
    public ResponseEntity<ServiceProviderDto> get(@PathVariable long id) {
        return serviceProviderService.get(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // TODO use ServiceProviderNotFound exception
    @PostMapping(consumes = "application/json", produces = "application/json")
    public ResponseEntity<ServiceProviderDto> create(@Valid @NotNull @RequestBody ServiceProviderDto serviceProviderDto) throws URISyntaxException {
        return serviceProviderService.create(serviceProviderDto).map(spDto -> {
            URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                    .path("/{id}")
                    .buildAndExpand(spDto.getId())
                    .toUri();

            return ResponseEntity.created(uri).body(spDto);
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // TODO use ServiceProviderNotFound exception
    @PutMapping(path = "/{id}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<ServiceProviderDto> update(@PathVariable long id, @Valid @NotNull @RequestBody ServiceProviderDto serviceProviderDto) {
        return serviceProviderService.updateContactInfo(id, serviceProviderDto)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
