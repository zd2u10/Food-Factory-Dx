package com.foodfactory.dx.service;

import com.foodfactory.dx.domain.Carrier;
import com.foodfactory.dx.mapper.CarrierMapper;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class CarrierService {

    private final CarrierMapper carrierMapper;

    public CarrierService(CarrierMapper carrierMapper) {
        this.carrierMapper = carrierMapper;
    }

    public Carrier createCarrier(Carrier carrier) {
        carrierMapper.insert(carrier);
        return carrier;
    }

    public List<Carrier> listCarriers() {
        return carrierMapper.findAll();
    }
}
