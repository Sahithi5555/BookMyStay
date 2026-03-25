/**
 * BookMyStay Application
 * @author Sahithi
 * @version 4.0
 */

import java.util.HashMap;

abstract class Room {
    String type;
    int beds;
    double price;

    Room(String type, int beds, double price) {
        this.type = type;
        this.beds = beds;
        this.price = price;
    }

    void displayRoomDetails() {
        System.out.println("Room Type: " + type);
        System.out.println("Beds: " + beds);
        System.out.println("Price: " + price);
    }
}

class SingleRoom extends Room {
    SingleRoom() {
        super("Single Room", 1, 1000);
    }
}

class DoubleRoom extends Room {
    DoubleRoom() {
        super("Double Room", 2, 2000);
    }
}

class SuiteRoom extends Room {
    SuiteRoom() {
        super("Suite Room", 3, 5000);
    }
}

// ---------------- UC3 ----------------
class RoomInventory {

    private HashMap<String, Integer> availability;

    RoomInventory() {
        availability = new HashMap<>();

        availability.put("Single Room", 5);
        availability.put("Double Room", 3);
        availability.put("Suite Room", 2);
    }

    int getAvailability(String roomType) {
        return availability.get(roomType);
    }

    void updateAvailability(String roomType, int count) {
        availability.put(roomType, count);
    }

    HashMap<String, Integer> getAllRooms() {
        return availability;
    }
}

// ---------------- UC4 ----------------
class SearchService {

    void searchAvailableRooms(RoomInventory inventory) {

        System.out.println("\n--- Available Rooms ---\n");

        for (String roomType : inventory.getAllRooms().keySet()) {

            int available = inventory.getAvailability(roomType);

            if (available > 0) {

                Room room = null;

                if (roomType.equals("Single Room")) {
                    room = new SingleRoom();
                } else if (roomType.equals("Double Room")) {
                    room = new DoubleRoom();
                } else if (roomType.equals("Suite Room")) {
                    room = new SuiteRoom();
                }

                room.displayRoomDetails();
                System.out.println("Available: " + available);
                System.out.println();
            }
        }
    }
}

public class BookMyStayApp {

    public static void main(String[] args) {

        // ---------------- UC1 ----------------
        System.out.println("Welcome to BookMyStay!");
        System.out.println("Hotel Booking System v1.0");


        // ---------------- UC2 ----------------
        Room single = new SingleRoom();
        Room doub = new DoubleRoom();
        Room suite = new SuiteRoom();


        // ---------------- UC3 ----------------
        RoomInventory inventory = new RoomInventory();


        // ---------------- UC4 ----------------
        SearchService searchService = new SearchService();
        searchService.searchAvailableRooms(inventory);
    }
}