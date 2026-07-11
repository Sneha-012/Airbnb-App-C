package com.sneha.airbnbAppC.service;

import com.sneha.airbnbAppC.entity.Room;

public interface InventoryService {

    void initializeRoomForAYear(Room room);

    void deleteFutureInventories(Room room);

    void deleteAllInventories(Room room);
}
