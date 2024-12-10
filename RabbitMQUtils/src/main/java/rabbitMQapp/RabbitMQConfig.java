package rabbitMQapp;

import com.rabbitmq.client.ConnectionFactory;

public class RabbitMQConfig {
    public static final String EXCHANGE_NAME = "photo-exchange";
    public static final String QUEUE_NAME = "photo-task-queue";
    public static final String ROUTING_KEY = "photo.task";

    public static ConnectionFactory getConnectionFactory() {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("10.204.0.26");
        factory.setPort(5672);
        factory.setUsername("guest");
        factory.setPassword("guest");
        return factory;
    }
}

