package Server;

import Model.Seat;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.Semaphore;

public class Logs {

    private File file = new File("logs/log.txt"); // Arquivo para armazenar os registros de log na pasta "logs"
    private Socket socket;
    private Seat seat;
    private String logStr = ""; // String para armazenar os detalhes do log
    private Semaphore empty = new Semaphore(10000); // controla a disponibilidade de espaços vazios
    private Semaphore full = new Semaphore(0); //  controla o preenchimento de espaços vazios
    private Semaphore mutex = new Semaphore(1); //  faz a exclusão mútua caso duas threads tentem acessar a região crítica ao mesmo tempo

    public Logs() throws IOException {
        // Verifica se a pasta "logs" existe ou cria uma nova
        File logsFolder = new File("logs");
        if (!logsFolder.exists()) {
            if (logsFolder.mkdir()) {
                System.out.println("Logs folder created.");
            } else {
                System.out.println("Failed to create logs folder.");
            }
        }

        // Verifica se o arquivo de log já existe ou cria um novo na pasta "logs"
        if (this.file.createNewFile()) {
            System.out.println("Log archive has been created: " + this.file.getName());
        }
    }

    public void log(Socket socket, Seat seat){
        this.socket = socket;
        this.seat = seat;

        // Cria e inicia threads produtor e consumidor
        Thread producer = new Thread(new Producer());
        Thread consumer = new Thread(new Consumer());

        producer.start();
        consumer.start();
    }

    public class Producer implements Runnable {
        public void run()  {
            try{
                mutex.acquire(); // garante a exclusão mútua (consumidor não consome)

                String ip = socket.getInetAddress().toString(); // Obtém o endereço IP do socket conectado

                logStr = "INGRESSO DE " + seat.getNome() + " \n"; // nome do comprador do ingresso
                logStr += "Poltrona: " + seat.getId() + " \n"; // poltrona escolhida (id)
                logStr += "Data e Hora: " + seat.getTime() + " \n"; // data e hora da reserva da poltrona
                logStr += "Ip: " + ip + " \n"; // ip do socket conectado

                empty.acquire(logStr.length()); // pega a permissão do semáforo empty, com base no tamanho da string do log(buffer)
                full.release(); // Libera uma permissão para o semáforo full
                mutex.release(); // Libera o mutex para permitir que o consumidor execute
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public class Consumer implements Runnable {
        @Override
        public void run() {
            try{
                mutex.acquire(); // garante a exclusão mútua (produtor não produz)
                full.acquire(); // Aguarda até que haja a permissão do semáforo full
                empty.release(logStr.length()); // Libera uma permissão para o semáforo empty, com base no tamanho da string do log

                FileWriter writer = new FileWriter(file, true); // Cria um escritor de arquivo para adicionar o log ao arquivo de log existente
                writer.write(logStr); // Faz o registro no log
                writer.close(); // Encerra escritura no log
                mutex.release(); // Libera o mutex para permitir que o produtor execute
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
