/**
 * BookMyStay Application
 * @author Sahithi
 * @version 3.0
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
// Inventory class using HashMap

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

    void displayInventory() {
        System.out.println("Current Inventory:");

        for (String key : availability.keySet()) {
            System.out.println(key + " -> " + availability.get(key));
        }
    }
}

public class BookMyStayApp {

    public static void main(String[] args) {

        // ---------------- UC1 ----------------
        System.out.println("Welcome to BookMyStay!");
        System.out.println("Hotel Booking System v1.0");


        // ---------------- UC2 ----------------
        System.out.println("\nRoom Details:\n");

        Room single = new SingleRoom();
        Room doub = new DoubleRoom();
        Room suite = new SuiteRoom();

        single.displayRoomDetails();
        System.out.println();

        doub.displayRoomDetails();
        System.out.println();

        suite.displayRoomDetails();


        // ---------------- UC3 ----------------
        System.out.println("\n--- Inventory Management ---\n");

        RoomInventory inventory = new RoomInventory();

        inventory.displayInventory();

        System.out.println("\nChecking availability of Single Room:");
        System.out.println(inventory.getAvailability("Single Room"));

        System.out.println("\nUpdating Single Room availability...");
        inventory.updateAvailability("Single Room", 4);

        inventory.displayInventory();
    }
}