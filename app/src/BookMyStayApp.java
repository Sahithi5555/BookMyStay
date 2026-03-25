/**
 * BookMyStay Application
 * @version 11.0
 */

import java.util.*;

// ---------------- EXCEPTION ----------------
class InvalidBookingException extends Exception {
    InvalidBookingException(String msg) { super(msg); }
}

// ---------------- VALIDATOR ----------------
class BookingValidator {
    void validate(Reservation r, RoomInventory inv) throws InvalidBookingException {

        if (r.roomType == null || r.roomType.isEmpty())
            throw new InvalidBookingException("Invalid Room Type");

        if (!inv.getAllRooms().containsKey(r.roomType))
            throw new InvalidBookingException("Room Type Not Found");

        if (inv.getAvailability(r.roomType) <= 0)
            throw new InvalidBookingException("No Rooms Available");
    }
}

// ---------------- ROOM ----------------
abstract class Room {
    String type;
    int beds;
    double price;

    Room(String t, int b, double p) {
        type = t; beds = b; price = p;
    }

    void display() {
        System.out.println(type + " | Beds: " + beds + " | ₹" + price);
    }
}

class SingleRoom extends Room { SingleRoom() { super("Single Room",1,1000);} }
class DoubleRoom extends Room { DoubleRoom() { super("Double Room",2,2000);} }
class SuiteRoom extends Room { SuiteRoom() { super("Suite Room",3,5000);} }

// ---------------- INVENTORY ----------------
class RoomInventory {

    private HashMap<String,Integer> map = new HashMap<>();

    RoomInventory() {
        map.put("Single Room",5);
        map.put("Double Room",3);
        map.put("Suite Room",2);
    }

    synchronized void reduce(String type) throws InvalidBookingException {
        if (map.get(type) <= 0)
            throw new InvalidBookingException("No rooms left");
        map.put(type, map.get(type)-1);
    }

    synchronized void increase(String type) {
        map.put(type, map.get(type)+1);
    }

    int getAvailability(String t) { return map.get(t); }
    HashMap<String,Integer> getAllRooms(){ return map; }
}

// ---------------- SEARCH ----------------
class SearchService {
    void search(RoomInventory inv) {
        System.out.println("\nAvailable Rooms:");
        for(String t : inv.getAllRooms().keySet()) {
            if(inv.getAvailability(t) > 0)
                System.out.println(t + " → " + inv.getAvailability(t));
        }
    }
}

// ---------------- RESERVATION ----------------
class Reservation {
    String id;
    String guest;
    String roomType;

    Reservation(String g,String t){
        guest=g;
        roomType=t;
        id = UUID.randomUUID().toString().substring(0,5);
    }
}

// ---------------- QUEUE ----------------
class BookingQueue {
    Queue<Reservation> q = new LinkedList<>();

    synchronized void add(Reservation r){ q.add(r); }

    synchronized Reservation get(){
        return q.poll();
    }
}

// ---------------- HISTORY ----------------
class BookingHistory {
    List<Reservation> list = new ArrayList<>();

    void add(Reservation r){ list.add(r); }
    void remove(Reservation r){ list.remove(r); }
    List<Reservation> getAll(){ return list; }
}

// ---------------- BOOKING SERVICE ----------------
class BookingService {

    HashMap<String,String> map = new HashMap<>();
    BookingValidator validator = new BookingValidator();
    BookingHistory history;

    BookingService(BookingHistory h){ history=h; }

    synchronized void processOne(Reservation r, RoomInventory inv){

        try {
            validator.validate(r, inv);

            String roomId = r.roomType.substring(0,2).toUpperCase()
                    + "_" + UUID.randomUUID().toString().substring(0,4);

            inv.reduce(r.roomType);
            map.put(r.id, roomId);
            history.add(r);

            System.out.println(Thread.currentThread().getName()
                    + " → Booked " + r.guest + " | " + roomId);

        } catch(Exception e){
            System.out.println("Failed: " + r.guest + " → " + e.getMessage());
        }
    }
}

// ---------------- THREAD ----------------
class BookingThread extends Thread {

    BookingQueue queue;
    BookingService service;
    RoomInventory inventory;

    BookingThread(BookingQueue q, BookingService s, RoomInventory i){
        queue=q; service=s; inventory=i;
    }

    public void run(){
        while(true){
            Reservation r;
            synchronized(queue){
                r = queue.get();
            }
            if(r == null) break;

            service.processOne(r, inventory);
        }
    }
}

// ---------------- ADDONS ----------------
class AddOnService {
    String name; double cost;
    AddOnService(String n,double c){name=n;cost=c;}
}

class AddOnManager {
    HashMap<String,List<AddOnService>> map = new HashMap<>();

    void add(String id, AddOnService s){
        map.putIfAbsent(id,new ArrayList<>());
        map.get(id).add(s);
    }
}

// ---------------- CANCELLATION ----------------
class CancellationService {
    Stack<String> stack = new Stack<>();

    void cancel(String id, BookingService s, BookingHistory h, RoomInventory inv){

        if(!s.map.containsKey(id)){
            System.out.println("Invalid cancel ID");
            return;
        }

        String roomId = s.map.get(id);
        stack.push(roomId);

        Reservation remove=null;

        for(Reservation r:h.getAll()){
            if(r.id.equals(id)){
                remove=r;
                inv.increase(r.roomType);
                break;
            }
        }

        if(remove!=null){
            h.remove(remove);
            s.map.remove(id);
            System.out.println("Cancelled → "+id);
        }
    }
}

// ---------------- MAIN ----------------
public class BookMyStayApp {

    public static void main(String[] args) {

        System.out.println("Welcome to BookMyStay v11");

        RoomInventory inv = new RoomInventory();

        new SearchService().search(inv);

        BookingQueue queue = new BookingQueue();

        queue.add(new Reservation("A","Single Room"));
        queue.add(new Reservation("B","Double Room"));
        queue.add(new Reservation("C","Suite Room"));
        queue.add(new Reservation("D","Single Room"));

        BookingHistory history = new BookingHistory();

        BookingService service = new BookingService(history);

        // MULTI THREADING
        Thread t1 = new BookingThread(queue, service, inv);
        Thread t2 = new BookingThread(queue, service, inv);

        t1.start();
        t2.start();

        try{
            t1.join();
            t2.join();
        }catch(Exception e){}

        System.out.println("\nAll bookings processed safely.");
    }
}