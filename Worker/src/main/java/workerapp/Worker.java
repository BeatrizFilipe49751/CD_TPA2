package workerapp;

import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import rabbitMQapp.PhotoTask;
import rabbitMQapp.RabbitMQConfig;
import java.io.File;

public class Worker {

    private static final String GLUSTER_SHARED_PATH = System.getenv("GLUSTER_SHARED_PATH");

    public static void main(String[] args) throws Exception {
        ConnectionFactory factory = RabbitMQConfig.getConnectionFactory();
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        // Declare queue
        channel.queueDeclare(RabbitMQConfig.QUEUE_NAME, true, false, false, null);

        System.out.println("Worker waiting for tasks...");

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody());
            Gson gson = new Gson();
            PhotoTask task = gson.fromJson(message, PhotoTask.class);

            System.out.println("Received task: " + task);

            String fileName = task.getFileName();
            // Locate the file in the shared Gluster storage
            File inputFile = new File(GLUSTER_SHARED_PATH, fileName);
            File outputFile = new File(GLUSTER_SHARED_PATH, fileName.replace(".", "-marks."));
            System.out.println("Output file path: " + outputFile.getAbsolutePath());

            String extension = fileName.substring(fileName.lastIndexOf('.') + 1);

            // marking
            MarkApp.annotateImage(inputFile, outputFile, extension, task.getWords());

            System.out.println("Processed photo: " + outputFile.getPath());

            // Acknowledge message
            channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
        };

        channel.basicConsume(RabbitMQConfig.QUEUE_NAME, false, deliverCallback, consumerTag -> {});
    }

}