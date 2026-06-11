package com.rahulhardware.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rahulhardware.entity.UserAddress;

public interface UserAddressRepository extends JpaRepository<UserAddress, Long> {

    List<UserAddress> findByUserMobileOrderByIdDesc(String userMobile);

    Optional<UserAddress> findByIdAndUserMobile(Long id, String userMobile);
}