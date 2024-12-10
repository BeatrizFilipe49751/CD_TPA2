package rabbitMQapp;

import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class RabbitMQPublisher {

    public static void publishTask(PhotoTask task) throws Exception {
        ConnectionFactory factory = RabbitMQConfig.getConnectionFactory();

        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {

            // Declare exchange and queue
            channel.exchangeDeclare(RabbitMQConfig.EXCHANGE_NAME, "direct", true);
            channel.queueDeclare(RabbitMQConfig.QUEUE_NAME, true, false, false, null);
            channel.queueBind(RabbitMQConfig.QUEUE_NAME, RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.ROUTING_KEY);

            // Serialize task to JSON
            Gson gson = new Gson();
            String message = gson.toJson(task);

            // Publish message
            channel.basicPublish(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.ROUTING_KEY, null, message.getBytes());
            System.out.println("Published task: " + task);
        }
    }
}
