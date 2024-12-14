package rabbitMQapp;

import com.rabbitmq.client.ConnectionFactory;

public class RabbitMQConfig {
    public final String EXCHANGE_NAME = "photo-exchange";
    public String QUEUE_NAME;
    public final String ROUTING_KEY = "photo.task";
    public String IP;
    public Integer PORT;

    public RabbitMQConfig(String queueName, String ip, Integer port) {
        QUEUE_NAME = queueName;
        IP = ip;
        PORT = port;
    }

    public ConnectionFactory getConnectionFactory() {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(IP);
        factory.setPort(PORT);
        factory.setUsername("guest");
        factory.setPassword("guest");
        return factory;
    }
}

