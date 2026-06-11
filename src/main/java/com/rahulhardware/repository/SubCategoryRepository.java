package com.rahulhardware.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rahulhardware.entity.SubCategory;

public interface SubCategoryRepository extends JpaRepository<SubCategory, String> {

    List<SubCategory> findByCategoryIdAndActiveTrueOrderByDisplayOrderAsc(String categoryId);
}