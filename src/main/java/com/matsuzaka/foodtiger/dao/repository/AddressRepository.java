package com.matsuzaka.foodtiger.dao.repository;

import com.matsuzaka.foodtiger.dao.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
    List<Address> findByUserId(Long userId);
    List<Address> findByCityAndDistrictAndStreet(String city, String district, String street);
}
