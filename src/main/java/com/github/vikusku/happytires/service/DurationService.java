package com.github.vikusku.happytires.service;

import com.github.vikusku.happytires.model.ServiceType;
import org.springframework.stereotype.Service;

@Service
public class DurationService {

    public int getServiceDuration(ServiceType serviceType) {
        switch (serviceType) {
            case TIRES_CHANGE:
            case TIRE_CHANGE_PLUS_STORAGE:
                return 30;
            default:
                return 60;
        }
    }
}
