package Server;

public class Headers {

    private final String patternHeader = """
            HTTP/1.1 200 OK
            Content-Type: text/html; charset=UTF-8
            
            """;
    private final String locationHeader = """
            HTTP/1.1 302 Found
            Contene-Type: text/html; charset=UTF-8
            Location: /
            
            """;
    private final String notFoundHeader = """
            HTTP/1.1 404 NOT FOUND
            Content-Type: text/html; charset=UTF-8
            
            """;

    public String getPatternHeader() { return patternHeader; }

    public String getLocationHeader() {
        return locationHeader;
    }

    public String getNotFoundHeader() {
        return notFoundHeader;
    }

}
