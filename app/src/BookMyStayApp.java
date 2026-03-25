/**
 * BookMyStay Application
 * @author Sahithi
 * @version 10.0
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

        if (inventory.getAvailability(r.roomType) <= 0) {
            throw new InvalidBookingException("No rooms available for " + r.roomType);
        }
    }
}

// ---------------- ROOM ----------------
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
        System.out.println("Room Type: " + type + " | Beds: " + beds + " | Price: ₹" + price);
    }
}

class SingleRoom extends Room {
    SingleRoom() { super("Single Room", 1, 1000); }
}
class DoubleRoom extends Room {
    DoubleRoom() { super("Double Room", 2, 2000); }
}
class SuiteRoom extends Room {
    SuiteRoom() { super("Suite Room", 3, 5000); }
}

// ---------------- INVENTORY ----------------
class RoomInventory {

    private HashMap<String, Integer> availability = new HashMap<>();

    RoomInventory() {
        availability.put("Single Room", 5);
        availability.put("Double Room", 3);
        availability.put("Suite Room", 2);
    }

    int getAvailability(String type) {
        return availability.get(type);
    }

    void reduceRoom(String type) throws InvalidBookingException {
        if (availability.get(type) <= 0)
            throw new InvalidBookingException("No rooms left");
        availability.put(type, availability.get(type) - 1);
    }

    void increaseRoom(String type) {
        availability.put(type, availability.get(type) + 1);
    }

    HashMap<String, Integer> getAllRooms() {
        return availability;
    }
}

// ---------------- SEARCH (UC4) ----------------
class SearchService {

    void search(RoomInventory inventory) {

        System.out.println("\n--- Available Rooms ---");

        for (String type : inventory.getAllRooms().keySet()) {

            if (inventory.getAvailability(type) > 0) {

                Room r = null;

                if (type.equals("Single Room")) r = new SingleRoom();
                else if (type.equals("Double Room")) r = new DoubleRoom();
                else if (type.equals("Suite Room")) r = new SuiteRoom();

                r.displayRoomDetails();
                System.out.println("Available: " + inventory.getAvailability(type));
                System.out.println();
            }
        }
    }
}

// ---------------- RESERVATION ----------------
class Reservation {
    String id;
    String guestName;
    String roomType;

    Reservation(String guest, String type) {
        guestName = guest;
        roomType = type;
        id = UUID.randomUUID().toString().substring(0, 5);
    }

    void display() {
        System.out.println("ID: " + id + " | Guest: " + guestName + " | Room: " + roomType);
    }
}

// ---------------- QUEUE (UC5) ----------------
class BookingQueue {
    private Queue<Reservation> queue = new LinkedList<>();

    void addRequest(Reservation r) {
        queue.add(r);
    }

    Queue<Reservation> getQueue() {
        return queue;
    }
}

// ---------------- HISTORY (UC8) ----------------
class BookingHistory {
    private List<Reservation> history = new ArrayList<>();

    void add(Reservation r) { history.add(r); }

    void remove(Reservation r) { history.remove(r); }

    List<Reservation> getAll() { return history; }
}

// ---------------- ADD-ONS (UC7) ----------------
class AddOnService {
    String name;
    double cost;

    AddOnService(String n, double c) {
        name = n;
        cost = c;
    }
}

class AddOnServiceManager {

    private HashMap<String, List<AddOnService>> map = new HashMap<>();

    void addService(String id, AddOnService s) {
        map.putIfAbsent(id, new ArrayList<>());
        map.get(id).add(s);
    }

    void show(String id) {
        if (!map.containsKey(id)) return;

        double total = 0;
        for (AddOnService s : map.get(id)) {
            System.out.println(s.name + " ₹" + s.cost);
            total += s.cost;
        }
        System.out.println("Total: ₹" + total);
    }
}

// ---------------- BOOKING (UC6 + UC9) ----------------
class BookingService {

    private HashMap<String, String> reservationMap = new HashMap<>();
    private BookingValidator validator = new BookingValidator();
    private BookingHistory history;

    BookingService(BookingHistory h) {
        history = h;
    }

    HashMap<String, String> getMap() {
        return reservationMap;
    }

    void process(BookingQueue q, RoomInventory inv) {

        while (!q.getQueue().isEmpty()) {

            Reservation r = q.getQueue().poll();

            try {
                validator.validate(r, inv);

                String roomId = r.roomType.substring(0, 2).toUpperCase()
                        + "_" + UUID.randomUUID().toString().substring(0, 4);

                reservationMap.put(r.id, roomId);
                inv.reduceRoom(r.roomType);
                history.add(r);

                System.out.println("Booked → " + r.guestName + " | " + roomId);

            } catch (Exception e) {
                System.out.println("Failed → " + r.guestName + " : " + e.getMessage());
            }
        }
    }
}

// ---------------- CANCELLATION (UC10) ----------------
class CancellationService {

    private Stack<String> stack = new Stack<>();

    void cancel(String id, BookingService service, BookingHistory history, RoomInventory inv) {

        if (!service.getMap().containsKey(id)) {
            System.out.println("Invalid Cancellation ID");
            return;
        }

        String roomId = service.getMap().get(id);
        stack.push(roomId);

        Reservation remove = null;

        for (Reservation r : history.getAll()) {
            if (r.id.equals(id)) {
                remove = r;
                inv.increaseRoom(r.roomType);
                break;
            }
        }

        if (remove != null) {
            history.remove(remove);
            service.getMap().remove(id);

            System.out.println("Cancelled → " + id + " | Rollback: " + stack.peek());
        }
    }
}

// ---------------- REPORT ----------------
class BookingReportService {
    void show(BookingHistory history) {
        System.out.println("\n--- Booking History ---");
        for (Reservation r : history.getAll()) r.display();
    }
}

// ---------------- MAIN ----------------
public class BookMyStayApp {

    public static void main(String[] args) {

        System.out.println("Welcome to BookMyStay!");

        RoomInventory inventory = new RoomInventory();

        // UC4
        new SearchService().search(inventory);

        // UC5
        BookingQueue queue = new BookingQueue();

        Reservation r1 = new Reservation("Alice", "Single Room");
        Reservation r2 = new Reservation("Bob", "Double Room");
        Reservation r3 = new Reservation("Eve", "Invalid Room"); // UC9 test

        queue.addRequest(r1);
        queue.addRequest(r2);
        queue.addRequest(r3);

        // UC8
        BookingHistory history = new BookingHistory();

        // UC6 + UC9
        BookingService service = new BookingService(history);
        service.process(queue, inventory);

        // UC7
        AddOnServiceManager manager = new AddOnServiceManager();
        manager.addService(r1.id, new AddOnService("Breakfast", 200));

        // UC8 report
        new BookingReportService().show(history);

        // UC10
        new CancellationService().cancel(r1.id, service, history, inventory);
    }
}