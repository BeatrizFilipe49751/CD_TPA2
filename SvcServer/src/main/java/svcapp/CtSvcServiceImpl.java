package svcapp;

import CtSvcstubs.*;
import io.grpc.stub.StreamObserver;
import rabbitMQapp.PhotoTask;
import rabbitMQapp.RabbitMQPublisher;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

public class CtSvcServiceImpl extends CtSvcServiceGrpc.CtSvcServiceImplBase {

    private static final String GLUSTER_SHARED_PATH = System.getenv("GLUSTER_SHARED_PATH");

    @Override
    public StreamObserver<PhotoChunk> uploadPhoto(StreamObserver<ImageInfo> responseObserver) {
        return new StreamObserver<>() {
            private String fileId;
            private String fileExtension;
            private FileOutputStream fileOutputStream;
            private ArrayList<String> words;

            @Override
            public void onNext(PhotoChunk photoChunk) {
                System.out.println("Received chunk of size: " + photoChunk.getData().size() + " bytes");
                try {
                    // Initialize file ID, extension, and output stream
                    if (fileId == null) {
                        fileId = UUID.randomUUID().toString(); // Unique ID for the photo
                        fileExtension = photoChunk.getFormat();

                        if (!fileExtension.equalsIgnoreCase(".jpg") && !fileExtension.equalsIgnoreCase(".png")) {
                            throw new IllegalArgumentException("Unsupported file format: " + fileExtension);
                        }

                        File file = new File(GLUSTER_SHARED_PATH, fileId + fileExtension);
                        fileOutputStream = new FileOutputStream(file);

                        // Extract the words for marking from the first chunk
                        words = new ArrayList<>(photoChunk.getWordsList());
                    }

                    // Write the chunk data to the file
                    fileOutputStream.write(photoChunk.getData().toByteArray());
                } catch (IOException e) {
                    responseObserver.onError(e);
                }
            }

            @Override
            public void onError(Throwable throwable) {
                throwable.printStackTrace();
                closeResources();
            }

            @Override
            public void onCompleted() {
                try {
                    // Close the file output stream
                    closeResources();

                    // Publish the task to RabbitMQ
                    PhotoTask task = new PhotoTask(fileId + fileExtension, words);
                    RabbitMQPublisher.publishTask(task);

                    // Send response with image ID
                    ImageInfo imageInfo = ImageInfo.newBuilder().setId(fileId).build();
                    responseObserver.onNext(imageInfo);
                    responseObserver.onCompleted();

                    System.out.println("Upload completed for photo: " + fileId);
                } catch (Exception e) {
                    responseObserver.onError(e);
                }
            }

            private void closeResources() {
                try {
                    if (fileOutputStream != null) {
                        fileOutputStream.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
    }

    @Override
    public void downloadPhoto(ImageInfo request, StreamObserver<PhotoChunk> responseObserver) {
        // Handle photo download
    }

}