package com.rahulhardware.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_addresses")
public class UserAddress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="user_mobile", nullable = false)
    private String userMobile;

    private String firstName;
    private String lastName;
    private String email;
    private String mobile;
    private String state;
    private String city;
    private String pinCode;

    @Column(length = 1000)
    private String fullAddress;

    public Long getId() { return id; }
    public String getUserMobile() { return userMobile; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }
    public String getMobile() { return mobile; }
    public String getState() { return state; }
    public String getCity() { return city; }
    public String getPinCode() { return pinCode; }
    public String getFullAddress() { return fullAddress; }

    public void setId(Long id) { this.id = id; }
    public void setUserMobile(String userMobile) { this.userMobile = userMobile; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setEmail(String email) { this.email = email; }
    public void setMobile(String mobile) { this.mobile = mobile; }
    public void setState(String state) { this.state = state; }
    public void setCity(String city) { this.city = city; }
    public void setPinCode(String pinCode) { this.pinCode = pinCode; }
    public void setFullAddress(String fullAddress) { this.fullAddress = fullAddress; }
}