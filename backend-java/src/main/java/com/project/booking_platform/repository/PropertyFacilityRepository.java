package com.project.booking_platform.repository;

import com.project.booking_platform.model.PropertyFacility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PropertyFacilityRepository extends JpaRepository<PropertyFacility, Long> {
}
