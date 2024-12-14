package workerapp;

import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import rabbitMQapp.PhotoTask;
import rabbitMQapp.RabbitMQConfig;
import spreadapp.SpreadUtils;
import java.io.File;

// Worker class that listens for tasks from a RabbitMQ queue, processes images, and communicates task status through Spread
public class Worker {
    private static final String GLUSTER_SHARED_PATH = System.getenv("GLUSTER_SHARED_PATH");

    public static void main(String[] args) {
        if (args.length < 6) {
            System.err.println("Usage: java -jar Worker-1.0-jar-with-dependencies.jar <SpreadGroup> <SpreadAddress> <SpreadPort> <ipRabbitMQ> <portRabbitMQ> <workQueue>");
            System.exit(1);
        }
        String groupName = args[0];
        String spreadAddress = args[1];
        int spreadPort = Integer.parseInt(args[2]);
        String ipRabbitMQ = args[3];
        int portRabbitMQ = Integer.parseInt(args[4]);
        String workQueue = args[5];

        // Initialize utilities for Spread and RabbitMQ
        SpreadUtils spreadUtils = new SpreadUtils(groupName);
        RabbitMQConfig rabbitMQConfig = new RabbitMQConfig(workQueue, ipRabbitMQ, portRabbitMQ);
        ConnectionFactory factory = rabbitMQConfig.getConnectionFactory();

        try (Connection connection = factory.newConnection()) {
            Channel channel = connection.createChannel();

            // Declare queue for receiving tasks from RabbitMQ
            channel.queueDeclare(rabbitMQConfig.QUEUE_NAME, true, false, false, null);

            System.out.println("Worker waiting for tasks...");

            // Callback for processing tasks from the RabbitMQ queue
            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody());
                Gson gson = new Gson();
                // Deserialize the task from the message
                PhotoTask task = gson.fromJson(message, PhotoTask.class);

                System.out.println("Received task: " + task);

                // Locate the image file in the shared storage (Gluster)
                String fileName = task.getFileName();
                File inputFile = new File(GLUSTER_SHARED_PATH, fileName);
                File outputFile = new File(GLUSTER_SHARED_PATH, fileName.replace(".", "-marks."));
                String extension = fileName.substring(fileName.lastIndexOf('.') + 1);

                // Process the image by annotating it with keywords
                MarkApp.annotateImage(inputFile, outputFile, extension, task.getWords());

                System.out.println("Processed photo: " + outputFile.getPath());

                // Send work completed message to Spread group
                String workCompletedMessage = "Task completed for file: " + fileName + " processed with words: " + task.getWords();
                spreadUtils.start(spreadAddress, spreadPort, null);
                spreadUtils.sendMessage(workCompletedMessage);

                // Acknowledge a message
                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
            };

            channel.basicConsume(rabbitMQConfig.QUEUE_NAME, false, deliverCallback, consumerTag -> {});

            // Keep the worker running
            Thread.sleep(Long.MAX_VALUE);

        } catch (Exception e) {
            System.out.println("An error occurred: " + e.getMessage());
        }
    }
}