package com.github.vikusku.happytires.service;

import com.github.vikusku.happytires.dto.ServiceProviderDto;
import com.github.vikusku.happytires.model.ServiceProvider;
import com.github.vikusku.happytires.model.TimeSlot;
import com.github.vikusku.happytires.repository.ServiceProviderRepository;
import com.github.vikusku.happytires.util.ModelMapperUtil;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ServiceProviderService {

    private final ServiceProviderRepository spRepository;
    private final ModelMapper modelMapper;
    private final ModelMapperUtil modelMapperUtil;

    @Autowired
    public ServiceProviderService(ServiceProviderRepository spRepository, ModelMapper modelMapper, ModelMapperUtil modelMapperUtil) {
        this.spRepository = spRepository;
        this.modelMapper = modelMapper;
        this.modelMapperUtil = modelMapperUtil;
    }

    public Optional<ServiceProviderDto> create(final ServiceProviderDto serviceProviderDto) {
        final ServiceProvider sp = modelMapper.map(serviceProviderDto, ServiceProvider.class);
        return Optional.ofNullable(modelMapper.map(spRepository.save(sp), ServiceProviderDto.class));
    }

    public Optional<ServiceProviderDto> updateContactInfo(final long id, final ServiceProviderDto updatedSpDto) {
        return spRepository.findById(id).map(existingSp -> {
            existingSp.setName(updatedSpDto.getName());
            existingSp.setEmail(updatedSpDto.getEmail());
            existingSp.setPhoneNumber(updatedSpDto.getPhoneNumber());

            return modelMapper.map(spRepository.save(existingSp), ServiceProviderDto.class);
        });
    }

    // TODO add pagination
    public List<ServiceProviderDto> getAll() {
        return modelMapperUtil.mapList(spRepository.findAll(), ServiceProviderDto.class);
    }

    public Optional<ServiceProviderDto> get(long id) {
        return spRepository.findById(id).map(sp -> modelMapper.map(sp, ServiceProviderDto.class));
    }
}
