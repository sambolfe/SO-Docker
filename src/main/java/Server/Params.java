package Server;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class Params {
    private Map<String, String> query;

    public Map<String, String> parseQuery(String[] q) throws IOException {
            if(q == null) { // se não houver parâmentros na  query, retorna null
                return null;
            }

            Map<String, String> queries = new HashMap<>(); // chave : valor
            for(String s : q) {  // for para separar o nome do valor [id,1];
                String[] pair = s.split("=");
                if(pair.length == 1) { // se houver apenas nome e não valor(valor == null)
                    queries.put(pair[0], null);
                } else { // decodifica o nome e o valor (retira "%" da url)
                    queries.put(pair[0], URLDecoder.decode(pair[1], StandardCharsets.UTF_8));
                }
            }
            return queries;
    }
}
