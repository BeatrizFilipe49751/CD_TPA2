package rabbitMQapp;

import java.util.ArrayList;

public class PhotoTask {
    private final String fileName;
    private final ArrayList<String> words;

    public PhotoTask(String fileName, ArrayList<String> words) {
        this.fileName = fileName;
        this.words = words;
    }

    public String getFileName() {
        return fileName;
    }

    public ArrayList<String> getWords() {
        return words;
    }

    @Override
    public String toString() {
        return "PhotoTask{" +
                "fileName='" + fileName + '\'' +
                ", words=" + words +
                '}';
    }
}