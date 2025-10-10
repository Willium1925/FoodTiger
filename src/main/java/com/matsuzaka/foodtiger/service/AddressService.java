package com.matsuzaka.foodtiger.service;

import com.matsuzaka.foodtiger.dao.entity.Address;

import java.util.List;
import java.util.Optional;

public interface AddressService {
    List<Address> findAllAddresses();
    Optional<Address> findAddressById(Long id);
    Address saveAddress(Address address);
    void deleteAddress(Long id);
    List<Address> findAddressesByUserId(Long userId);
}
