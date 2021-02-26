package com.github.vikusku.happytires.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.vikusku.happytires.dto.ServiceProviderDto;
import com.github.vikusku.happytires.service.ServiceProviderService;
import com.google.common.collect.Lists;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.StringContains.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ServiceProviderController.class)
class ServiceProviderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ServiceProviderService serviceProviderService;

    private final Long spId = 111L;
    private final String spName = "Mikko Malikas";
    private final String spEmail = "mikko.malikas@test.com";
    private final String spPhone = "0401234567";

    @Test
    public void getAllShouldReturnAllServiceProviders() throws Exception {
        ServiceProviderDto spDto1 = new ServiceProviderDto(1L, "Foo Bar 1", "foo.bar1@test.com", "1231231234");
        ServiceProviderDto spDto2 = new ServiceProviderDto(2L, "Foo Bar 2", "foo.bar2@test.com", "1231231234");
        ServiceProviderDto spDto3 = new ServiceProviderDto(3L, "Foo Bar 3", "foo.bar3@test.com", "1231231234");

        when(serviceProviderService.getAll()).thenReturn(Lists.newArrayList(spDto1, spDto2, spDto3));

        this.mockMvc
                .perform(get("/api/v1/serviceproviders").accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].id").value("1"))
                .andExpect(jsonPath("$.[0].name").value("Foo Bar 1"))
                .andExpect(jsonPath("$.[0].email").value("foo.bar1@test.com"))
                .andExpect(jsonPath("$.[0].phoneNumber").value("1231231234"))
                .andExpect(jsonPath("$.[1].id").value("2"))
                .andExpect(jsonPath("$.[1].name").value("Foo Bar 2"))
                .andExpect(jsonPath("$.[1].email").value("foo.bar2@test.com"))
                .andExpect(jsonPath("$.[1].phoneNumber").value("1231231234"))
                .andExpect(jsonPath("$.[2].id").value("3"))
                .andExpect(jsonPath("$.[2].name").value("Foo Bar 3"))
                .andExpect(jsonPath("$.[2].email").value("foo.bar3@test.com"))
                .andExpect(jsonPath("$.[2].phoneNumber").value("1231231234"));
    }

    @Test
    public void getShouldReturnServiceProvider() throws Exception {
        ServiceProviderDto spDto = new ServiceProviderDto(1L, "Foo Bar", "foo.bar@test.com", "1231231234");

        when(serviceProviderService.get(1L)).thenReturn(Optional.of(spDto));

        this.mockMvc.perform(get("/api/v1/serviceproviders/1"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value("1"))
                    .andExpect(jsonPath("$.name").value("Foo Bar"))
                    .andExpect(jsonPath("$.email").value("foo.bar@test.com"))
                    .andExpect(jsonPath("$.phoneNumber").value("1231231234"));
    }

    @Test
    public void getShouldReturn404IfServiceProviderNotFound() throws Exception {
        when(serviceProviderService.get(1L)).thenReturn(Optional.empty());

        this.mockMvc.perform(get("/api/v1/serviceproviders/1"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    public void createShouldCreateAndReturnServiceProvider() throws Exception {
        ServiceProviderDto dto = new ServiceProviderDto();
        dto.setName(spName);
        dto.setEmail(spEmail);
        dto.setPhoneNumber(spPhone);

        when(serviceProviderService.create(dto))
                .thenReturn(Optional.of(new ServiceProviderDto(spId, spName, spEmail, spPhone)));

        this.mockMvc
                .perform(post("/api/v1/serviceproviders")
                .content(asJsonString(dto))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(header().string("Location", equalTo("http://localhost/api/v1/serviceproviders/" + spId)))
                .andExpect(jsonPath("$.id").value(spId))
                .andExpect(jsonPath("$.name").value(spName))
                .andExpect(jsonPath("$.email").value(spEmail))
                .andExpect(jsonPath("$.phoneNumber").value(spPhone));
    }

    @Test
    public void createShouldReturn400IfRequestBodyNull() throws Exception {
        this.mockMvc
                .perform(post("/api/v1/serviceproviders")
                .content("")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    public void createShouldReturn400IfRequestBodyInvalid() throws Exception  {
        final ServiceProviderDto request = new ServiceProviderDto();
        request.setName(spName);
        request.setEmail("invalid email");
        request.setPhoneNumber(spPhone);

        this.mockMvc
                .perform(post("/api/v1/serviceproviders")
                .content(asJsonString(request))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Email should be valid")));
    }

    @Test
    public void updateShouldUpdateAndReturnUpdatedServiceProvider() throws Exception {
        ServiceProviderDto spDto = new ServiceProviderDto(1L, spName, spEmail, spPhone);
        ServiceProviderDto spDtoUpd = new ServiceProviderDto(1L, spName, spEmail, "78912341212");

        when(serviceProviderService.updateContactInfo(1L, spDto)).thenReturn(Optional.of(spDtoUpd));

        this.mockMvc.perform(put("/api/v1/serviceproviders/1")
            .content(asJsonString(spDto))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value(spName))
            .andExpect(jsonPath("$.email").value(spEmail))
            .andExpect(jsonPath("$.phoneNumber").value("78912341212"));
    }

    private String asJsonString(final ServiceProviderDto obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}