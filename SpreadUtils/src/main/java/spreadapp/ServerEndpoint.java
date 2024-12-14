package spreadapp;

// ServerEndpoint: Represents a server's endpoint with IP and port
public class ServerEndpoint {
    private final String ip;
    private final int port;

    public ServerEndpoint(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    @Override
    public String toString() {
        return "ServerEndPoint{" +
                "ip='" + ip + '\'' +
                ", port=" + port + '\'' +
                '}';
    }
}
