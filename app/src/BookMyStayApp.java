import java.util.*;
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

    void reduceRoom(String roomType) {
        availability.put(roomType, availability.get(roomType) - 1);
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

// ---------------- UC5 ----------------
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

// ---------------- UC8 ----------------
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

// ---------------- UC6 ----------------
class BookingService {

    private HashMap<String, Set<String>> allocatedRooms;
    private BookingHistory history;

    BookingService(BookingHistory history) {
        this.history = history;
        allocatedRooms = new HashMap<>();
    }

    void processBookings(BookingQueue bookingQueue, RoomInventory inventory) {

        System.out.println("\n--- Processing Bookings ---");

        Queue<Reservation> queue = bookingQueue.getQueue();

        while (!queue.isEmpty()) {

            Reservation r = queue.poll();
            String roomType = r.roomType;

            int available = inventory.getAvailability(roomType);

            if (available > 0) {

                String roomId = roomType.substring(0, 2).toUpperCase()
                        + "_" + UUID.randomUUID().toString().substring(0, 5);

                allocatedRooms.putIfAbsent(roomType, new HashSet<>());

                if (!allocatedRooms.get(roomType).contains(roomId)) {

                    allocatedRooms.get(roomType).add(roomId);
                    inventory.reduceRoom(roomType);

                    System.out.println("Booking Confirmed for " + r.guestName +
                            " | Room ID: " + roomId);

                    // ✅ UC8 → store history
                    history.addReservation(r);
                }

            } else {
                System.out.println("Booking Failed for " + r.guestName);
            }
        }
    }
}

// ---------------- UC7 ----------------
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

    void displayServices(String reservationId) {

        List<AddOnService> services = serviceMap.get(reservationId);

        if (services == null) {
            System.out.println("No services.");
            return;
        }

        double total = 0;

        for (AddOnService s : services) {
            System.out.println(s.name + " ₹" + s.cost);
            total += s.cost;
        }

        System.out.println("Total: ₹" + total);
    }
}

// ---------------- UC8 REPORT ----------------
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

        Reservation r1 = new Reservation("Alice", "Single Room");
        Reservation r2 = new Reservation("Bob", "Double Room");

        queue.addRequest(r1);
        queue.addRequest(r2);

        // UC8
        BookingHistory history = new BookingHistory();

        BookingService service = new BookingService(history);
        service.processBookings(queue, inventory);

        // UC7
        AddOnServiceManager manager = new AddOnServiceManager();
        manager.addService(r1.reservationId, new AddOnService("Breakfast", 200));

        // UC8 Report
        BookingReportService report = new BookingReportService();
        report.showAllBookings(history);
    }
}