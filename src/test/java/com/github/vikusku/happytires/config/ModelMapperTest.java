package com.github.vikusku.happytires.config;

import com.github.vikusku.happytires.dto.ServiceProviderDto;
import com.github.vikusku.happytires.model.ServiceProvider;
import com.google.common.collect.Lists;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = MapperConfig.class)
class ModelMapperTest {

    @Autowired
    private ModelMapper modelMapper;

    private final Long spId = 111L;
    private final String spName = "Mikko Malikas";
    private final String spEmail = "mikko.malikas@test.com";
    private final String spPhone = "0401234567";

    @Test
    public void testServiceProviderDtoMapToServiceProvider() {
        ServiceProviderDto dto = new ServiceProviderDto();
        dto.setName(spName);
        dto.setEmail(spEmail);
        dto.setPhoneNumber(spPhone);

        ServiceProvider sp = modelMapper.map(dto, ServiceProvider.class);

        assertThat(sp.getName()).isEqualTo(spName);
        assertThat(sp.getEmail()).isEqualTo(spEmail);
        assertThat(sp.getPhoneNumber()).isEqualTo(spPhone);
    }

    @Test
    public void testServiceProviderMapToServiceProviderDto() {
        ServiceProvider sp = new ServiceProvider(spId, spName, spEmail, spPhone, Lists.newArrayList());

        ServiceProviderDto dto = modelMapper.map(sp, ServiceProviderDto.class);

        assertThat(dto.getId()).isEqualTo(spId);
        assertThat(dto.getName()).isEqualTo(spName);
        assertThat(dto.getEmail()).isEqualTo(spEmail);
        assertThat(dto.getPhoneNumber()).isEqualTo(spPhone);
    }
}