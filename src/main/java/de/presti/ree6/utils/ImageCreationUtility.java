package de.presti.ree6.utils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;

public class ImageCreationUtility {

    public static byte[] createRankImage(String userImageUrl) throws Exception {
        BufferedImage base = new BufferedImage(885, 211, BufferedImage.TYPE_INT_ARGB);

        BufferedImage user = convertToCircleShape(new URL(userImageUrl));

        Graphics2D graphics2D = base.createGraphics();

        graphics2D.setColor(Color.DARK_GRAY);
        graphics2D.fillRoundRect(0, 0, base.getWidth(), base.getHeight(), 10, 10);

        graphics2D.drawImage(user, null,25, 25);
        graphics2D.setColor(Color.WHITE);
        graphics2D.setFont(new Font("Verdana", Font.PLAIN, 25));
        graphics2D.drawString("Presti", 250, 120);
        graphics2D.fillRoundRect(250, 130, base.getWidth() - 300, 50, 30, 30);

        graphics2D.dispose();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        ImageIO.write(base, "PNG", outputStream);
        return outputStream.toByteArray();
    }

    public static BufferedImage convertToCircleShape(URL url) throws IOException {

        BufferedImage mainImage = ImageIO.read(url);

        BufferedImage output = new BufferedImage(mainImage.getWidth(), mainImage.getHeight(), BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2 = output.createGraphics();

        // This is what we want, but it only does hard-clipping, i.e. aliasing
        // g2.setClip(new RoundRectangle2D ...)

        // so instead fake soft-clipping by first drawing the desired clip shape
        // in fully opaque white with antialiasing enabled...
        g2.setComposite(AlphaComposite.Src);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Color.WHITE);
        g2.fill(new Ellipse2D.Float(0, 0, mainImage.getHeight(), mainImage.getHeight()));

        // ... then compositing the image on top,
        // using the white shape from above as alpha source
        g2.setComposite(AlphaComposite.SrcAtop);
        g2.drawImage(mainImage, 0, 0, null);

        g2.dispose();

        return output;
    }

}
