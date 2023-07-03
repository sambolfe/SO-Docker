package Model;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Seat {
    private int id;
    private String name;
    public boolean reserved;
    //A data e hora são geradas automáticamente na reserva de uma poltrona.
    private final static SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    private static Date dateHour;
    private String time;

    public Seat(int id) {
        this.id = id;
        this.reserved = false;
    }
    public int getId() {
        return this.id;
    }
    public String getNome() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isReserved() {
        return this.reserved;
    }

    public void setReserved(boolean reserved) {
        this.reserved = reserved;
    }
    public static String getDateHour() {
        dateHour = new Date();
        return formatter.format(dateHour);
    }
    public void setTime(String time) {
        this.time = time;
    }
    public String getTime() {
        return this.time;
    }

}
