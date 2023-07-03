package Server;

import Model.Seat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Scanner;

public class ServerConnection implements Runnable {
    private Socket socket;
    private String resource;
    public ServerConnection(Socket socket) {
        this.socket = socket;
    }
    @Override
    public void run() {
        try{
            InputStream in = this.socket.getInputStream();
            OutputStream out = this.socket.getOutputStream();

            Scanner scan = new Scanner(in); // recebe os dados no socket.

            // verifica se o scanner tem uma próxima palavra para ler, se não encerra leitura.
            if(!scan.hasNext()) {
                scan.close();
                return;
            }

            String method = scan.next();
            String path = scan.next();
            System.out.println(method + " " + path + "\n"); // request feita pelo cliente.

            String[] url = path.split("\\?"); // separa o caminho dos parâmetros
            String resource = url[0]; // rota acessada!

            Params params = new Params(); // classe para interpretar as queries

            String[] q = url.length > 1 ? url[1].split("&") : null;
            Map<String, String> query = params.parseQuery(q);

            byte[] contentBytes = null;

            Headers headers = new Headers();
            String header = headers.getPatternHeader(); // pega o cabeçalho padrão de response

            if(resource.startsWith("/css/")) { // request do css
                contentBytes = this.getBytes(resource);
                if(contentBytes != null) {
                    header = header.replace("text/html","text/css");
                }
                System.out.println(header);
            }

            if(resource.equals("/")) { // request da página home
                contentBytes = this.getBytes("index.html");
                String html = new String(contentBytes, StandardCharsets.UTF_8);
                String elements = "";

                for(Seat seat : Server.seats.values()) { // gera as poltronas com base no estado delas (reservadas ou vazias)
                    String element = "<a ";
                    element += "class=\"seats\" ";
                    if(!seat.isReserved()) {
                        element += "href=\"/reserve?id=" + seat.getId() + "\"";
                        element += ">Poltrona " + seat.getId();
                        element += "</a>";
                    } else {
                        element += " style=\"background-color:#3E1515;color:#F2D8D8;\">Reservado</br>";
                        element += seat.getNome() + "</br>";
                        element += seat.getTime();
                        element += "</a>";
                    }
                    elements += element + "\n";
                }
                html = html.replace("<seats/>", elements);
                contentBytes = html.getBytes();
                System.out.println(header);
            }

            if(resource.equals("/reserve")) {  // request para a reserva da poltrona
                contentBytes = this.getBytes("reserve.html");
                String html = new String(contentBytes, StandardCharsets.UTF_8);
                html  = html.replace("{{id}}", query.get("id")); // pega o id vindo da query da poltrona selecionada em index.html
                contentBytes = html.getBytes();
                System.out.println(header);
            }

            if(resource.equals("/confirm")) { // rota para quando o usuário confirma os dados em reserve
                header = headers.getLocationHeader(); // redireciona para página principal index.html
                System.out.println(header);
                contentBytes = "<p>Redirecting...</p>".getBytes();

                Server.mutex.acquire(); // bloqueia a entrada na região crítica para o acesso de múltiplas threads.

                int id = Integer.parseInt(query.get("id")); // pega o id da poltrona que foi escolhida para ser reservada
                Seat seat = Server.seats.get(id);

                if(!seat.isReserved()) { // verifica se a poltrona não foi reservada
                    String name = query.get("name"); // pega o nome que o usuário digitou da query em /reserve
                    seat.setName(name); // seta o nome na reserva do ingresso
                    seat.setReserved(true); // seta que a poltrona foi reservada
                    seat.setTime(Seat.getDateHour()); // pega a data e a hora que foi feita a reserva
                    Server.log.log(socket, seat); // registra no log do server a reserva
                    System.out.println("Nova venda de ingresso registrada: " + seat.getId() + " " + seat.getNome());
                }
                Server.mutex.release(); // libera acesso a região crítica
            }

            if(contentBytes == null) { // caso o servidor não ache a rota a ser acessada retorna um 404 (resource = nulo)
                contentBytes = this.getBytes("notFound.html");
                header = headers.getNotFoundHeader();
                System.out.println(header);
            }

            out.write(header.getBytes());
            out.write(contentBytes);

            in.close(); // encerra leituras de request
            out.close(); // encerra escritas de response
            scan.close(); // encerra o scanner
            this.socket.close(); // encerra conexão com o cliente

        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    private byte[] getBytes(String resource) throws IOException {
        try{
            if(resource.startsWith("/")) {
                resource = resource.substring(1); // remove a "/" e faz uma nova string para o recurso
            }

            InputStream is = this.getClass().getClassLoader().getResourceAsStream(resource); // localiza e carrega os bytes da substring(resourcec)

            if(is != null) { //  se is não for nulo, retorna os bytes vindos do input
                return is.readAllBytes();
            }
        }catch (IOException e) {
            e.printStackTrace();
        }
        return null; // se nenhum recurso foi encontrado retorna nulo
    }
}
