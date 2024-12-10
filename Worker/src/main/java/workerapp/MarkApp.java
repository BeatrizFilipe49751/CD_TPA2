package workerapp;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class MarkApp {

    public static void annotateImage(File inputFile, File outputFile, String extension, ArrayList<String> keywords) throws IOException {
        BufferedImage img = ImageIO.read(inputFile);
        Graphics2D gfx = img.createGraphics();
        gfx.setFont(new Font("Arial", Font.PLAIN, 22));
        gfx.setColor(new Color(0x0000ff));
        String sentence="";
        for (String s : keywords) sentence+=s+" ";
        gfx.drawString(sentence, 10, 20);
        Polygon poly = new Polygon();
        poly.addPoint(3, 3);
        poly.addPoint(10*sentence.length(), 3);
        poly.addPoint(10*sentence.length(), 25);
        poly.addPoint(3, 25);
        poly.addPoint(3, 3);
        gfx.setColor(new Color(0xff0000));
        gfx.draw(poly);
        ImageIO.write(img, extension, outputFile);
        gfx.dispose();
    }
}
