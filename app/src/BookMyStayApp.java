/**
 * BookMyStay Application
 * @author Sahithi
 * @version 9.0
 */

import java.util.*;

// ---------------- EXCEPTION ----------------
class InvalidBookingException extends Exception {
    InvalidBookingException(String message) {
        super(message);
    }
}

// ---------------- VALIDATOR ----------------
class BookingValidator {

    void validate(Reservation r, RoomInventory inventory) throws InvalidBookingException {

        if (r.roomType == null || r.roomType.isEmpty()) {
            throw new InvalidBookingException("Room type cannot be empty");
        }

        if (!inventory.getAllRooms().containsKey(r.roomType)) {
            throw new InvalidBookingException("Invalid room type: " + r.roomType);
        }

        int available = inventory.getAvailability(r.roomType);

        if (available <= 0) {
            throw new InvalidBookingException("No rooms available for " + r.roomType);
        }
    }
}

// ---------------- ROOM CLASSES ----------------
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

// ---------------- INVENTORY ----------------
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

    void reduceRoom(String roomType) throws InvalidBookingException {

        int current = availability.get(roomType);

        if (current <= 0) {
            throw new InvalidBookingException("Cannot reduce. No rooms left for " + roomType);
        }

        availability.put(roomType, current - 1);
    }

    HashMap<String, Integer> getAllRooms() {
        return availability;
    }
}

// ---------------- SEARCH ----------------
class SearchService {

    void searchAvailableRooms(RoomInventory inventory) {

        System.out.println("\n--- Available Rooms ---\n");

        for (String roomType : inventory.getAllRooms().keySet()) {

            int available = inventory.getAvailability(roomType);

            if (available > 0) {

                Room room = null;

                if (roomType.equals("Single Room")) room = new SingleRoom();
                else if (roomType.equals("Double Room")) room = new DoubleRoom();
                else if (roomType.equals("Suite Room")) room = new SuiteRoom();

                room.displayRoomDetails();
                System.out.println("Available: " + available);
                System.out.println();
            }
        }
    }
}

// ---------------- RESERVATION ----------------
class Reservation {
    String reservationId;
    String guestName;
    String roomType;

    Reservation(String guestName, String roomType) {
        this.guestName = guestName;
        this.roomType = roomType;
        this.reservationId = UUID.randomUUID().toString().substring(0, 5);
    }

    void display() {
        System.out.println("ID: " + reservationId + " | Guest: " + guestName + " | Room: " + roomType);
    }
}

// ---------------- QUEUE ----------------
class BookingQueue {

    private Queue<Reservation> queue;

    BookingQueue() {
        queue = new LinkedList<>();
    }

    void addRequest(Reservation r) {
        queue.add(r);
    }

    Queue<Reservation> getQueue() {
        return queue;
    }
}

// ---------------- HISTORY ----------------
class BookingHistory {

    private List<Reservation> history;

    BookingHistory() {
        history = new ArrayList<>();
    }

    void addReservation(Reservation r) {
        history.add(r);
    }

    List<Reservation> getAllReservations() {
        return history;
    }
}

// ---------------- BOOKING SERVICE ----------------
class BookingService {

    private HashMap<String, Set<String>> allocatedRooms;
    private BookingHistory history;
    private BookingValidator validator;

    BookingService(BookingHistory history) {
        this.history = history;
        this.validator = new BookingValidator();
        allocatedRooms = new HashMap<>();
    }

    void processBookings(BookingQueue bookingQueue, RoomInventory inventory) {

        System.out.println("\n--- Processing Bookings ---");

        Queue<Reservation> queue = bookingQueue.getQueue();

        while (!queue.isEmpty()) {

            Reservation r = queue.poll();

            try {
                // VALIDATION (UC9)
                validator.validate(r, inventory);

                String roomType = r.roomType;

                String roomId = roomType.substring(0, 2).toUpperCase()
                        + "_" + UUID.randomUUID().toString().substring(0, 5);

                allocatedRooms.putIfAbsent(roomType, new HashSet<>());
                allocatedRooms.get(roomType).add(roomId);

                inventory.reduceRoom(roomType);

                System.out.println("Booking Confirmed for " + r.guestName +
                        " | Room ID: " + roomId);

                history.addReservation(r);

            } catch (InvalidBookingException e) {

                System.out.println("Booking Failed for " + r.guestName +
                        " → " + e.getMessage());
            }
        }
    }
}

// ---------------- ADD-ONS ----------------
class AddOnService {
    String name;
    double cost;

    AddOnService(String name, double cost) {
        this.name = name;
        this.cost = cost;
    }
}

class AddOnServiceManager {

    private HashMap<String, List<AddOnService>> serviceMap;

    AddOnServiceManager() {
        serviceMap = new HashMap<>();
    }

    void addService(String reservationId, AddOnService service) {
        serviceMap.putIfAbsent(reservationId, new ArrayList<>());
        serviceMap.get(reservationId).add(service);
    }
}

// ---------------- REPORT ----------------
class BookingReportService {

    void showAllBookings(BookingHistory history) {

        System.out.println("\n--- Booking History ---");

        for (Reservation r : history.getAllReservations()) {
            r.display();
        }
    }
}

// ---------------- MAIN ----------------
public class BookMyStayApp {

    public static void main(String[] args) {

        System.out.println("Welcome to BookMyStay!");

        RoomInventory inventory = new RoomInventory();

        SearchService searchService = new SearchService();
        searchService.searchAvailableRooms(inventory);

        BookingQueue queue = new BookingQueue();

        // VALID + INVALID cases (UC9)
        queue.addRequest(new Reservation("Alice", "Single Room"));
        queue.addRequest(new Reservation("Bob", "Double Room"));
        queue.addRequest(new Reservation("Charlie", "Invalid Room")); // invalid

        BookingHistory history = new BookingHistory();

        BookingService service = new BookingService(history);
        service.processBookings(queue, inventory);

        BookingReportService report = new BookingReportService();
        report.showAllBookings(history);
    }
}