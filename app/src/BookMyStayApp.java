/**
 * BookMyStay Application
 * @version 12.0
 */

import java.util.*;
import java.io.*;

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
}

class SingleRoom extends Room { SingleRoom() { super("Single Room",1,1000);} }
class DoubleRoom extends Room { DoubleRoom() { super("Double Room",2,2000);} }
class SuiteRoom extends Room { SuiteRoom() { super("Suite Room",3,5000);} }

// ---------------- INVENTORY ----------------
class RoomInventory implements Serializable {

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

// ---------------- RESERVATION ----------------
class Reservation implements Serializable {
    String id;
    String guest;
    String roomType;

    Reservation(String g,String t){
        guest=g;
        roomType=t;
        id = UUID.randomUUID().toString().substring(0,5);
    }
}

// ---------------- HISTORY ----------------
class BookingHistory implements Serializable {
    List<Reservation> list = new ArrayList<>();

    void add(Reservation r){ list.add(r); }
    List<Reservation> getAll(){ return list; }
}

// ---------------- QUEUE ----------------
class BookingQueue {
    Queue<Reservation> q = new LinkedList<>();

    synchronized void add(Reservation r){ q.add(r); }
    synchronized Reservation get(){ return q.poll(); }
}

// ---------------- BOOKING ----------------
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

            System.out.println("Booked → " + r.guest + " | " + roomId);

        } catch(Exception e){
            System.out.println("Failed → " + r.guest + " : " + e.getMessage());
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

// ---------------- PERSISTENCE (UC12) ----------------
class PersistenceService {

    void save(RoomInventory inv, BookingHistory history) {

        try {
            FileOutputStream file = new FileOutputStream("data.ser");
            ObjectOutputStream out = new ObjectOutputStream(file);

            out.writeObject(inv);
            out.writeObject(history);

            out.close();
            file.close();

            System.out.println("Data saved successfully.");

        } catch (Exception e) {
            System.out.println("Save failed.");
        }
    }

    Object[] load() {

        try {
            FileInputStream file = new FileInputStream("data.ser");
            ObjectInputStream in = new ObjectInputStream(file);

            RoomInventory inv = (RoomInventory) in.readObject();
            BookingHistory history = (BookingHistory) in.readObject();

            in.close();
            file.close();

            System.out.println("Data loaded successfully.");
            return new Object[]{inv, history};

        } catch (Exception e) {
            System.out.println("No previous data found. Starting fresh.");
            return null;
        }
    }
}

// ---------------- MAIN ----------------
public class BookMyStayApp {

    public static void main(String[] args) {

        System.out.println("Welcome to BookMyStay v12");

        PersistenceService ps = new PersistenceService();

        RoomInventory inventory;
        BookingHistory history;

        Object[] data = ps.load();

        if (data != null) {
            inventory = (RoomInventory) data[0];
            history = (BookingHistory) data[1];
        } else {
            inventory = new RoomInventory();
            history = new BookingHistory();
        }

        BookingQueue queue = new BookingQueue();

        queue.add(new Reservation("Alice","Single Room"));
        queue.add(new Reservation("Bob","Double Room"));

        BookingService service = new BookingService(history);

        Thread t1 = new BookingThread(queue, service, inventory);
        Thread t2 = new BookingThread(queue, service, inventory);

        t1.start();
        t2.start();

        try{
            t1.join();
            t2.join();
        }catch(Exception e){}

        ps.save(inventory, history);

        System.out.println("System state persisted.");
    }
}