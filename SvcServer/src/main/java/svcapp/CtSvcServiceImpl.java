package svcapp;

import CtSvcstubs.*;
import com.google.protobuf.ByteString;
import io.grpc.stub.StreamObserver;
import rabbitMQapp.PhotoTask;
import rabbitMQapp.RabbitMQConfig;
import rabbitMQapp.RabbitMQPublisher;
import spreadapp.SpreadUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

// CtSvcServiceImpl class implements the gRPC service for uploading and downloading photos, and disconnection warning
public class CtSvcServiceImpl extends CtSvcServiceGrpc.CtSvcServiceImplBase {
    private static final String GLUSTER_SHARED_PATH = System.getenv("GLUSTER_SHARED_PATH");
    private final SpreadUtils spreadUtils;
    private final RabbitMQPublisher rabbitMQPublisher;

    public CtSvcServiceImpl(SpreadUtils spreadUtils, String ipRabbitMQ, int portRabbitMQ, String workQueue) {
        this.spreadUtils = spreadUtils;
        this.rabbitMQPublisher = new RabbitMQPublisher(new RabbitMQConfig(workQueue, ipRabbitMQ, portRabbitMQ));
    }

    // Upload photo implementation using StreamObserver to handle multiple chunks of the photo
    @Override
    public StreamObserver<PhotoChunk> uploadPhoto(StreamObserver<ImageInfo> responseObserver) {
        return new StreamObserver<>() {
            private String fileId; // Unique ID for the photo file
            private String fileExtension; // Extension of the file (.jpg, .png)
            private FileOutputStream fileOutputStream; // Output stream to save file
            private ArrayList<String> words; // Words to mark in the photo

            // Handle the incoming chunks of the photo
            @Override
            public void onNext(PhotoChunk photoChunk) {
                System.out.println("Received chunk of size: " + photoChunk.getData().size() + " bytes");
                try {
                    if (fileId == null) {
                        // Initialize file ID and extension for the first chunk
                        fileId = UUID.randomUUID().toString();
                        fileExtension = photoChunk.getFormat();

                        if (!fileExtension.equalsIgnoreCase(".jpg") && !fileExtension.equalsIgnoreCase(".png")) {
                            throw new IllegalArgumentException("Unsupported file format: " + fileExtension);
                        }

                        // Create file in the shared path
                        File file = new File(GLUSTER_SHARED_PATH, fileId + fileExtension);
                        fileOutputStream = new FileOutputStream(file);

                        // Extract words from the first chunk
                        words = new ArrayList<>(photoChunk.getWordsList());
                    }

                    // Write the chunk data to the file
                    fileOutputStream.write(photoChunk.getData().toByteArray());
                } catch (Exception e) {
                    responseObserver.onError(e);
                }
            }

            // Handle errors during the upload
            @Override
            public void onError(Throwable throwable) {
                System.out.println("Error occurred during upload: " + Arrays.toString(throwable.getStackTrace()));
                closeResources();
            }

            // Finalize the upload process
            @Override
            public void onCompleted() {
                try {
                    // Close file output stream and publish the task to RabbitMQ
                    closeResources();
                    PhotoTask task = new PhotoTask(fileId + fileExtension, words);
                    rabbitMQPublisher.publishTask(task);

                    // Send response with the file ID
                    ImageInfo imageInfo = ImageInfo.newBuilder().setId(fileId).build();
                    responseObserver.onNext(imageInfo);
                    responseObserver.onCompleted();
                    System.out.println("Upload completed for photo: " + fileId);
                } catch (Exception e) {
                    responseObserver.onError(e);
                }
            }

            // Close resources like file output stream
            private void closeResources() {
                try {
                    if (fileOutputStream != null) {
                        fileOutputStream.close();
                    }
                } catch (IOException e) {
                    System.out.println("Error closing file on closeResources: " + e.getMessage());
                }
            }
        };
    }

    // Download photo implementation to stream the photo in chunks
    @Override
    public void downloadPhoto(ImageInfo request, StreamObserver<PhotoChunkData> responseObserver) {
        String imageId = request.getId() + "-marks"; // File name with "marks" suffix
        File sharedPath = new File(GLUSTER_SHARED_PATH);

        // Search for the file in the shared directory
        File[] files = sharedPath.listFiles((dir, name) -> name.startsWith(imageId));
        if (files == null || files.length == 0) {
            responseObserver.onError(new FileNotFoundException("File not found for ID: " + imageId));
            return;
        }

        // Use the first matching file for the download
        File file = files[0];

        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            byte[] buffer = new byte[1024 * 1024]; // 1 MB buffer
            int bytesRead;
            boolean firstChunk = true;

            // Stream the file in chunks to the client
            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                PhotoChunkData.Builder chunkData = PhotoChunkData.newBuilder()
                        .setData(ByteString.copyFrom(buffer, 0, bytesRead));

                // Get name of the file from the first chunk
                if (firstChunk) {
                    chunkData.setFullName(file.getName());
                    firstChunk = false;
                }

                PhotoChunkData photoChunkData = chunkData.build();
                responseObserver.onNext(photoChunkData);
            }
            responseObserver.onCompleted();

            System.out.println("Download completed for photo: " + file.getName());
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    // Disconnect implementation to send a message to the Spread group to remove the load
    @Override
    public void disconnect(Nothing request, StreamObserver<Nothing> responseObserver) {
        try {
            if (spreadUtils == null) {
                throw new IllegalStateException("SpreadUtils is not initialized.");
            }
            spreadUtils.sendMessage("REMOVE_LOAD");
            System.out.println("REMOVE_LOAD message sent");
        } catch (Exception e) {
            System.err.println("Error sending REMOVE_LOAD message: " + e.getMessage());
        }
        responseObserver.onNext(Nothing.newBuilder().build());
        responseObserver.onCompleted();
    }
}