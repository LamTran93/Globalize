package com.project.booking_platform.service.database;

import com.project.booking_platform.dto.property.OwnerPropertyDTO;
import com.project.booking_platform.model.*;
import com.project.booking_platform.repository.*;
import com.project.booking_platform.service.fileupload.FileStorageService;
import com.project.booking_platform.utils.enums.Status;
import com.project.booking_platform.utils.generate.RandomID;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import javax.swing.text.html.Option;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class OwnerService {
    private final OwnerRepository ownerRepository;
    private final ModelMapper modelMapper;
    private final FileStorageService fileService;
    private final PropertyRepository propertyRepository;
    private final FacilityRepository facilityRepository;
    private final PropertyFacilityRepository propertyFacilityRepository;
    private final PropertyCommonRuleRepository propertyCommonRuleRepository;

    public OwnerService(OwnerRepository ownerRepository, ModelMapper modelMapper, FileStorageService fileService, PropertyRepository propertyRepository, FacilityRepository facilityRepository, PropertyFacilityRepository propertyFacilityRepository, PropertyCommonRuleRepository propertyCommonRuleRepository) {
        this.ownerRepository = ownerRepository;
        this.modelMapper = modelMapper;
        this.fileService = fileService;
        this.propertyRepository = propertyRepository;
        this.facilityRepository = facilityRepository;
        this.propertyFacilityRepository = propertyFacilityRepository;
        this.propertyCommonRuleRepository = propertyCommonRuleRepository;
    }

    public Optional<Owner> login(String userName, String password) {
        return ownerRepository.findByUserNameAndPassword(userName, password);
    }

    public List<OwnerPropertyDTO> getProperties(String userName) {
        return ownerRepository.findPropertiesByUserName(userName).stream()
                .map(property -> {
                    String link = "api/files/property/" + property.getPicture() + "/feature/";
                    String featuredFolder = "property/" + property.getPicture() + "/feature";
                    property.setPicture(link + fileService.getFileNames(featuredFolder).stream().findFirst().orElse(null));
                    return modelMapper.map(property, OwnerPropertyDTO.class);
                })
                .toList();
    }

    public Optional<Owner> getOwnerById(String id) {
        return ownerRepository.findById(id);
    }

    public Owner save(Owner owner) {
        owner.setId(RandomID.generateRandomID());
        owner.setStatus(Status.UNVERIFIED);
        owner.setCreatedAt(Date.from(Instant.now()));
        owner.setLastModifiedDate(Date.from(Instant.now()));
        return ownerRepository.save(owner);
    }

    public Owner update(Owner owner) {
        owner.setLastModifiedDate(Date.from(Instant.now()));
        return ownerRepository.save(owner);
    }

    public void removeProperty(String propertyId) {
        propertyRepository.deleteById(propertyId);
    }

    public boolean checkOwner(String username, String email) {
        return ownerRepository.existsByUserNameOrEmail(username, email);
    }

    public Owner getOwner(String userName) {
        return ownerRepository.findByUserName(userName).orElse(null);
    }

    public List<Facility> saveFacilities(List<Facility> facilities) {
        return facilities.stream().map(facility -> {
            if (facilityRepository.getFirstByName(facility.getName()).isEmpty()) {
                facility.setFee(0.0f);
                facility.setStatus(Status.ACTIVE);
                facility.setDescription("No description");
                facility.setCreatedAt(Date.from(Instant.now()));
                facility.setLastModifiedDate(Date.from(Instant.now()));
                return facilityRepository.save(facility);
            } else {
                return facilityRepository.getFirstByName(facility.getName()).get();
            }
        }).toList();
    }

    public Property addFacilitiesToProperty(Property property, List<Facility> facilities) {
        List<PropertyFacility> propertyFacilities = new ArrayList<>();
        facilities.forEach(facility -> {
            PropertyFacility propertyFacility = new PropertyFacility();
            propertyFacility.setFacility(facility);
            propertyFacility.setProperty(property);
            propertyFacility.setStatus(Status.ACTIVE);
            propertyFacility.setCreatedAt(Date.from(Instant.now()));
            propertyFacility.setLastModifiedDate(Date.from(Instant.now()));
            propertyFacilityRepository.save(propertyFacility);
            propertyFacilities.add(propertyFacility);
        });
        property.setPropertyFacilities(propertyFacilities);
        return propertyRepository.save(property);
    }

    public void savePropertyCommonRule(PropertyCommonRule propertyCommonRule, Property property) {
        propertyCommonRule.setId(RandomID.generateRandomID());
        propertyCommonRule.setStatus(Status.ACTIVE);
        propertyCommonRule.setCreatedAt(Date.from(Instant.now()));
        propertyCommonRule.setLastModifiedDate(Date.from(Instant.now()));
        propertyCommonRule.setProperty(property);
        propertyCommonRuleRepository.save(propertyCommonRule);
    }
}
