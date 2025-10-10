package com.matsuzaka.foodtiger.controller;

import com.matsuzaka.foodtiger.dao.entity.Address;
import com.matsuzaka.foodtiger.service.AddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/addresses")
public class AddressController {

    @Autowired
    private AddressService addressService;

    @GetMapping
    public ResponseEntity<List<Address>> getAllAddresses() {
        List<Address> addresses = addressService.findAllAddresses();
        return new ResponseEntity<>(addresses, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Address> getAddressById(@PathVariable Long id) {
        return addressService.findAddressById(id)
                .map(address -> new ResponseEntity<>(address, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping
    public ResponseEntity<Address> createAddress(@RequestBody Address address) {
        Address savedAddress = addressService.saveAddress(address);
        return new ResponseEntity<>(savedAddress, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Address> updateAddress(@PathVariable Long id, @RequestBody Address address) {
        return addressService.findAddressById(id)
                .map(existingAddress -> {
                    existingAddress.setUser(address.getUser());
                    existingAddress.setZipCode(address.getZipCode());
                    existingAddress.setCity(address.getCity());
                    existingAddress.setDistrict(address.getDistrict());
                    existingAddress.setStreet(address.getStreet());
                    existingAddress.setExtraDetails(address.getExtraDetails());
                    Address updatedAddress = addressService.saveAddress(existingAddress);
                    return new ResponseEntity<>(updatedAddress, HttpStatus.OK);
                })
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAddress(@PathVariable Long id) {
        if (addressService.findAddressById(id).isPresent()) {
            addressService.deleteAddress(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Address>> getAddressesByUserId(@PathVariable Long userId) {
        List<Address> addresses = addressService.findAddressesByUserId(userId);
        if (addresses.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(addresses, HttpStatus.OK);
    }
}
