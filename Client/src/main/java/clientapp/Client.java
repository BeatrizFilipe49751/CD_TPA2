package clientapp;

import CtSvcstubs.ImageInfo;
import CtSvcstubs.Nothing;
import CtSvcstubs.PhotoChunk;
import CtSvcstubs.PhotoChunkData;
import CtSvcstubs.CtSvcServiceGrpc;
import com.google.protobuf.ByteString;
import ctregstubs.Empty;
import ctregstubs.ServerEndPoint;
import ctregstubs.CtregServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

public class Client {

    // Register Server IP and Port
    private static final String svcIP = "34.175.254.22";
    private static final int svcPort = 8500;

    private static ManagedChannel channel;

    public static void main(String[] args) {
        try {
            // Retrieve the Server endpoint from Register
            ServerEndPoint serverEndPoint = getServerEndPoint();
            String serverIp = serverEndPoint.getIp();
            int serverPort = serverEndPoint.getPort();

            while (true) {
                switch (Menu()) {
                    case 1:  // Upload a photo
                        uploadPhoto(serverIp, serverPort);
                        break;
                    case 2:  // Download a photo
                        downloadPhoto(serverIp, serverPort);
                        break;
                    case 0:
                        notifyServerOnShutdown(serverIp, serverPort);
                        System.exit(0);
                    default:
                        break;
                }
            }
        } catch (Exception ex) {
            System.out.println("An error occurred" + ex.getMessage());
        }
    }

    private static int Menu() {
        int op;
        Scanner scan = new Scanner(System.in);
        do {
            System.out.println();
            System.out.println("    MENU");
            System.out.println("1 - Upload Photo");
            System.out.println("2 - Download Photo");
            System.out.println("0 - Exit");
            System.out.println();
            System.out.println("Choose an Option?");
            op = scan.nextInt();
        } while (!(op == 1 || op == 2 || op == 0));
        return op;
    }

    // Function to retrieve the Server EndPoint from the Register Server
    private static ServerEndPoint getServerEndPoint() {
        ServerEndPoint serverEndPoint = null;
        try {
            // Connect to RegisterServer
            System.out.println("Connecting to RegisterServer at " + svcIP + ":" + svcPort);
            channel = ManagedChannelBuilder.forAddress(svcIP, svcPort)
                    .usePlaintext()
                    .build();

            CtregServiceGrpc.CtregServiceBlockingStub serverBlockingStub = CtregServiceGrpc.newBlockingStub(channel);

            // Request Server EndPoint
            System.out.println("Requesting Server EndPoint from RegisterServer...");
            serverEndPoint = serverBlockingStub.getIp(Empty.newBuilder().build());
            System.out.println("Server EndPoint received: " + serverEndPoint.getIp() + ":" + serverEndPoint.getPort());
        } catch (Exception e) {
            System.out.println("An error occurred: " + e.getMessage());
        } finally {
            if (channel != null) {
                channel.shutdown();
            }
        }
        return serverEndPoint;
    }

    // Function to upload a photo
    private static void uploadPhoto(String serverIp, int serverPort) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Enter the file path to upload:");
        String filePath = scanner.nextLine();

        String fileExtension = filePath.substring(filePath.lastIndexOf('.'));

        System.out.println("Enter words to mark on the photo (comma-separated):");
        String wordsInput = scanner.nextLine();
        String[] words = wordsInput.split(",");

        try {
            // Connect to Server
            channel = ManagedChannelBuilder.forAddress(serverIp, serverPort)
                    .usePlaintext()
                    .build();

            CtSvcServiceGrpc.CtSvcServiceStub stub = CtSvcServiceGrpc.newStub(channel);

            StreamObserver<PhotoChunk> requestObserver = stub.uploadPhoto(new StreamObserver<ImageInfo>() {
                @Override
                public void onNext(ImageInfo imageInfo) {
                    System.out.println("Photo uploaded successfully. Image ID: " + imageInfo.getId());
                }

                @Override
                public void onError(Throwable t) {
                    System.err.println("Upload failed: " + t.getMessage());
                }

                @Override
                public void onCompleted() {
                    System.out.println("Upload process completed.");
                }
            });

            // Read a file in chunks and send
            try (FileInputStream fileInputStream = new FileInputStream(filePath)) {
                byte[] buffer = new byte[1024 * 1024];
                int bytesRead;

                boolean firstChunk = true;
                while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                    PhotoChunk.Builder chunkBuilder = PhotoChunk.newBuilder()
                            .setData(ByteString.copyFrom(buffer, 0, bytesRead));

                    // Add words only in the first chunk
                    if (firstChunk) {
                        chunkBuilder.addAllWords(List.of(words));
                        chunkBuilder.setFormat(fileExtension);
                        firstChunk = false;
                    }

                    requestObserver.onNext(chunkBuilder.build());
                }
            }

            requestObserver.onCompleted();
        } catch (Exception e) {
            System.out.println("An error occurred: " + e.getMessage());
        } finally {
            if (channel != null) {
                channel.shutdown();
            }
        }
    }

    // Function to download a photo
    private static void downloadPhoto(String serverIp, int serverPort) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Enter the Image ID to download:");
        String imageId = scanner.nextLine();

        System.out.println("Enter the folder path to save the photo:");
        String folderPath = scanner.nextLine();

        // Ensure the folder path ends with a separator
        if (!folderPath.endsWith(File.separator)) {
            folderPath += File.separator;
        }

        final String finalFolderPath = folderPath;

        try {
            // Connect to Server
            channel = ManagedChannelBuilder.forAddress(serverIp, serverPort)
                    .usePlaintext()
                    .build();

            CtSvcServiceGrpc.CtSvcServiceStub stub = CtSvcServiceGrpc.newStub(channel);

            StreamObserver<PhotoChunkData> responseObserver = new StreamObserver<>() {
                private FileOutputStream fileOutputStream;
                private String savePath;

                @Override
                public void onNext(PhotoChunkData photoChunk) {
                    try {
                        if (savePath == null) {
                            // Use the file name from the first chunk
                            String fileName = photoChunk.getFullName();
                            if (fileName.isEmpty()) {
                                throw new IllegalArgumentException("File name is missing in the first chunk.");
                            }
                            savePath = finalFolderPath + fileName;
                            fileOutputStream = new FileOutputStream(savePath);
                            System.out.println("Saving photo to: " + savePath);
                        }

                        fileOutputStream.write(photoChunk.getData().toByteArray());
                    } catch (IOException e) {
                        System.err.println("Error writing to file: " + e.getMessage());
                    }
                }

                @Override
                public void onError(Throwable t) {
                    System.err.println("Download failed: " + t.getMessage());
                    try {
                        if (fileOutputStream != null) {
                            fileOutputStream.close();
                        }
                    } catch (IOException e) {
                        System.err.println("Error closing file: " + e.getMessage());
                    }
                }

                @Override
                public void onCompleted() {
                    System.out.println("Photo downloaded successfully to: " + savePath);
                    try {
                        if (fileOutputStream != null) {
                            fileOutputStream.close();
                        }
                    } catch (IOException e) {
                        System.err.println("Error closing file: " + e.getMessage());
                    }
                }
            };

            // Send the request to the server
            stub.downloadPhoto(ImageInfo.newBuilder().setId(imageId).build(), responseObserver);
        } catch (Exception e) {
            System.out.println("An error occurred: " + e.getMessage());
        } finally {
            if (channel != null) {
                channel.shutdown();
            }
        }
    }

    private static void notifyServerOnShutdown(String serverIp, int serverPort) {
        System.out.println("Notifying server about shutdown...");
        try {
            // Create a channel to the server
            channel = ManagedChannelBuilder.forAddress(serverIp, serverPort)
                    .usePlaintext()
                    .build();

            // Create a blocking stub for synchronous call
            CtSvcServiceGrpc.CtSvcServiceBlockingStub blockingStub = CtSvcServiceGrpc.newBlockingStub(channel);

            // Call the Disconnect RPC
            blockingStub.disconnect(Nothing.newBuilder().build());

            System.out.println("Server notified successfully.");
        } catch (Exception e) {
            System.err.println("Error notifying server: " + e.getMessage());
        } finally {
            if (channel != null) {
                channel.shutdownNow();
            }
        }
    }
}
