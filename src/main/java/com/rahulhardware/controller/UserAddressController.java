package com.rahulhardware.controller;

import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rahulhardware.entity.UserAddress;
import com.rahulhardware.repository.UserAddressRepository;

@RestController
@RequestMapping("/api/addresses")
@CrossOrigin(origins = "*")
public class UserAddressController {

    private final UserAddressRepository addressRepository;

    public UserAddressController(UserAddressRepository addressRepository) {
        this.addressRepository = addressRepository;
    }

    @GetMapping
    public List<UserAddress> getAddresses(
            @RequestHeader("X-User-Mobile") String userMobile) {
        return addressRepository.findByUserMobileOrderByIdDesc(
                getUserMobile(userMobile));
    }

    @PostMapping
    public UserAddress createAddress(
            @RequestHeader("X-User-Mobile") String userMobile,
            @RequestBody UserAddress address) {
        address.setId(null);
        address.setUserMobile(getUserMobile(userMobile));

        return addressRepository.save(address);
    }

    @PutMapping("/{id}")
    public UserAddress updateAddress(
            @RequestHeader("X-User-Mobile") String userMobile,
            @PathVariable Long id,
            @RequestBody UserAddress payload) {
        String loggedInMobile = getUserMobile(userMobile);

        UserAddress existing = addressRepository
                .findByIdAndUserMobile(id, loggedInMobile)
                .orElseThrow(() -> new RuntimeException("Address not found"));

        existing.setFirstName(payload.getFirstName());
        existing.setLastName(payload.getLastName());
        existing.setEmail(payload.getEmail());
        existing.setMobile(payload.getMobile());
        existing.setState(payload.getState());
        existing.setCity(payload.getCity());
        existing.setPinCode(payload.getPinCode());
        existing.setFullAddress(payload.getFullAddress());

        return addressRepository.save(existing);
    }

    @DeleteMapping("/{id}")
    public void deleteAddress(
            @RequestHeader("X-User-Mobile") String userMobile,
            @PathVariable Long id) {
        String loggedInMobile = getUserMobile(userMobile);

        UserAddress address = addressRepository
                .findByIdAndUserMobile(id, loggedInMobile)
                .orElseThrow(() -> new RuntimeException("Address not found"));

        addressRepository.delete(address);
    }

    private String getUserMobile(String userMobile) {
        if (userMobile == null || userMobile.trim().isEmpty()) {
            throw new RuntimeException("User mobile is required");
        }

        String mobile = userMobile.trim();

        // remove +91 if exists
        if (mobile.startsWith("+91")) {
            mobile = mobile.substring(3);
        }

        // remove 91 if 12 digits
        if (mobile.startsWith("91") && mobile.length() == 12) {
            mobile = mobile.substring(2);
        }

        return mobile;
    }
}