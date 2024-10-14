package com.project.booking_platform.service.database;

import com.project.booking_platform.dto.room.RoomDTO;
import com.project.booking_platform.model.Bedroom;
import com.project.booking_platform.model.Property;
import com.project.booking_platform.model.Room;
import com.project.booking_platform.repository.RoomRepository;
import com.project.booking_platform.service.fileupload.FileStorageService;
import com.project.booking_platform.utils.enums.Status;
import com.project.booking_platform.utils.generate.RandomID;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class RoomService {
    private final RoomRepository roomRepository;
    private final FileStorageService fileService;
    private ModelMapper mapper;

    public RoomService(RoomRepository roomRepository, FileStorageService fileService, ModelMapper mapper) {
        this.roomRepository = roomRepository;
        this.fileService = fileService;
        this.mapper = mapper;
    }

    public Room getRoom(String id) {
        return roomRepository.findById(id).orElse(null);
    }

    public Room saveRoom(Room room) {
        return roomRepository.save(room);
    }

    public void deleteRoom(String id) {
        roomRepository.deleteById(id);
    }

    public List<RoomDTO> getRoomsByPropertyId(String propertyId, LocalDate from, LocalDate to) {
        return roomRepository.findRoomsByPropertyId(propertyId).stream()
                .filter(room -> room.getReservations().stream().allMatch(r -> r.getCheckOutDate().isBefore(from) || r.getCheckInDate().isAfter(to)))
                .map(room -> mapper.map(room, RoomDTO.class))
                .toList();
    }

    public List<RoomDTO> getRoomsByPropertyId(String propertyId) {
        return roomRepository.findRoomsByPropertyId(propertyId).stream()
                .map(room -> mapper.map(room, RoomDTO.class))
                .toList();
    }

    public List<Room> saveRooms(Property p, List<Room> rooms, List<MultipartFile> roomsPictures) {
        AtomicInteger index = new AtomicInteger(0);
        var newRooms = rooms.stream().map(r -> {
            r.setId(RandomID.generateRandomID());
           r.setCreatedAt(Date.from(Instant.now()));
           r.setLastModifiedDate(Date.from(Instant.now()));
           r.setStatus(Status.ACTIVE);
           r.setProperty(p);
           r.setDescription("No description");
           r.setPicture(fileService.storeRoomPictures(roomsPictures.get(index.getAndIncrement()), r.getId()));
           return r;
        }).toList();
        return roomRepository.saveAll(newRooms);
    }
}
