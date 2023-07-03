package Server;

import Model.Seat;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.logging.Logger;

public class Server {
    public final static int port = 8081;
    public static ServerSocket serverSocket;
    public static Map<Integer, Seat> seats = new HashMap<>();
    public static Semaphore mutex  = new Semaphore(1); // semáforo para lidar com o acesso a região crítica
    public static Logs log; // arquivo de log do servidor

    static {
        try {
            log = new Logs();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args)throws Exception {
        getSeats(); // inicia as poltronas
        try{
            serverSocket = new ServerSocket(port); // inicia o servidor

            System.out.println("Server is running on port 8081...\n");
            System.out.println("Page access: " + "http://localhost:8081 \n");

            while(true) {
                Socket socket = serverSocket.accept(); // aceita conexão
                ServerConnection connection = new ServerConnection(socket); // gerencia a conexão
                Thread thread = new Thread(connection);
                thread.start();
            }
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void getSeats() { // função para setar a quantidade das poltronas do teatro
        for(int id = 1; id <= 10; id++) {
            seats.put(id, new Seat(id));
        }
    }

}
