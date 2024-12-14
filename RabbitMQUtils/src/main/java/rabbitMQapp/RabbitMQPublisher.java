package rabbitMQapp;

import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class RabbitMQPublisher {
    private final RabbitMQConfig CONFIG;
    public RabbitMQPublisher(RabbitMQConfig config) {
        CONFIG = config;
    }

    public void publishTask(PhotoTask task) throws Exception {
        ConnectionFactory factory = CONFIG.getConnectionFactory();

        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {

            // Declare exchange and queue
            channel.exchangeDeclare(CONFIG.EXCHANGE_NAME, "direct", true);
            channel.queueDeclare(CONFIG.QUEUE_NAME, true, false, false, null);
            channel.queueBind(CONFIG.QUEUE_NAME, CONFIG.EXCHANGE_NAME, CONFIG.ROUTING_KEY);

            // Serialize task to JSON
            Gson gson = new Gson();
            String message = gson.toJson(task);

            // Publish message
            channel.basicPublish(CONFIG.EXCHANGE_NAME, CONFIG.ROUTING_KEY, null, message.getBytes());
            System.out.println("Published task: " + task);
        }
    }
}
