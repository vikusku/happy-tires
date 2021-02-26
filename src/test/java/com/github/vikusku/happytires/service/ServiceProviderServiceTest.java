package com.github.vikusku.happytires.service;

import com.github.vikusku.happytires.dto.ServiceProviderDto;
import com.github.vikusku.happytires.model.ServiceProvider;
import com.github.vikusku.happytires.repository.ServiceProviderRepository;
import com.github.vikusku.happytires.util.ModelMapperUtil;
import com.google.common.collect.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.modelmapper.ModelMapper;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class ServiceProviderServiceTest {

    @MockBean
    private ServiceProviderRepository spRespository;

    @MockBean
    private ModelMapper modelMapper;

    @MockBean
    private ModelMapperUtil modelMapperUtil;

    @Captor
    ArgumentCaptor<ServiceProvider> serviceProviderArgumentCaptor;

    private ServiceProviderService spService;

    private final Long spId = 111L;
    private final String spName = "Mikko Malikas";
    private final String spEmail = "mikko.malikas@test.com";
    private final String spPhone = "0401234567";

    @BeforeEach
    void setUp() {
        spService = new ServiceProviderService(spRespository, modelMapper, modelMapperUtil);
    }

    @Test
    public void createShouldCallRepository() {
        final ServiceProviderDto spDto = new ServiceProviderDto(null, spName, spEmail, spPhone);
        final ServiceProvider sp = new ServiceProvider(spId, spName, spEmail, spPhone, Lists.newArrayList());

        when(modelMapper.map(spDto, ServiceProvider.class)).thenReturn(sp);
        spService.create(spDto);

        verify(spRespository, times(1)).save(sp);
    }

    @Test
    public void updateContactInfoShouldCallRepository() {
        final ServiceProviderDto dto = new ServiceProviderDto(spId, "Foo Bar 2", "foo.bar2@test.com", "12121341345");

        when(spRespository.findById(spId)).thenReturn(
                Optional.of(new ServiceProvider(spId, spName, spEmail, spPhone, Lists.newArrayList())));

        spService.updateContactInfo(spId, dto);

        verify(spRespository, times(1)).save(serviceProviderArgumentCaptor.capture());

        ServiceProvider persisted = serviceProviderArgumentCaptor.getValue();
        assertThat(persisted.getName()).isEqualTo("Foo Bar 2");
        assertThat(persisted.getEmail()).isEqualTo("foo.bar2@test.com");
        assertThat(persisted.getPhoneNumber()).isEqualTo("12121341345");
    }
}