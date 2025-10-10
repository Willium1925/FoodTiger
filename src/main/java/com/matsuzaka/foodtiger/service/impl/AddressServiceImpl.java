package com.matsuzaka.foodtiger.service.impl;

import com.matsuzaka.foodtiger.dao.entity.Address;
import com.matsuzaka.foodtiger.dao.repository.AddressRepository;
import com.matsuzaka.foodtiger.service.AddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AddressServiceImpl implements AddressService {

    @Autowired
    private AddressRepository addressRepository;

    @Override
    public List<Address> findAllAddresses() {
        return addressRepository.findAll();
    }

    @Override
    public Optional<Address> findAddressById(Long id) {
        return addressRepository.findById(id);
    }

    @Override
    public Address saveAddress(Address address) {
        return addressRepository.save(address);
    }

    @Override
    public void deleteAddress(Long id) {
        addressRepository.deleteById(id);
    }

    @Override
    public List<Address> findAddressesByUserId(Long userId) {
        return addressRepository.findByUserId(userId);
    }
}
