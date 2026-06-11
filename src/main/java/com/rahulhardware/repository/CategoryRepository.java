package com.rahulhardware.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rahulhardware.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, String> {

    List<Category> findByActiveTrueOrderByDisplayOrderAsc();
}