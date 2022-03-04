package de.presti.ree6.utils;

import de.presti.ree6.sql.entities.UserLevel;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;

public class ImageCreationUtility {

    public static byte[] createRankImage(UserLevel userLevel) throws Exception {
        if (userLevel == null || userLevel.getUser() == null)
            return new byte[128];

        BufferedImage base = new BufferedImage(885, 211, BufferedImage.TYPE_INT_ARGB);

        BufferedImage userImage = convertToCircleShape(new URL(userLevel.getUser().getAvatarUrl()));

        Graphics2D graphics2D = base.createGraphics();

        graphics2D.setColor(Color.BLACK);
        graphics2D.fillRoundRect(0, 0, base.getWidth(), base.getHeight(), 10, 10);

        StringBuilder usernameBuilder = new StringBuilder();

        for (char c : userLevel.getUser().getName().toCharArray()) {
            usernameBuilder.append((char)Integer.parseInt(Integer.toHexString(c | 0x10000).substring(1),16));
        }

        graphics2D.drawImage(userImage, null,25, 45);
        graphics2D.setColor(Color.WHITE);
        graphics2D.setFont(new Font("Verdana", Font.PLAIN, 40));
        graphics2D.drawString(usernameBuilder.toString(), 200, 110);
        graphics2D.fillRoundRect(200, 130, base.getWidth() - 300, 50, 50, 50);

        //region Experience

        graphics2D.setFont(new Font("Verdana", Font.PLAIN, 20));
        graphics2D.drawString(userLevel.getFormattedExperience() + "", (base.getWidth() - 210), 110);
        graphics2D.setColor(Color.DARK_GRAY);
        graphics2D.drawString("/" + userLevel.getFormattedExperience(userLevel.getExperienceForNextLevel()), (base.getWidth() - 170), 110);

        //endregion

        //region Rank

        graphics2D.setColor(Color.WHITE);
        graphics2D.setFont(new Font("Verdana", Font.PLAIN, 20));
        graphics2D.drawString("Rank", (base.getWidth() - 370), 60);

        graphics2D.setFont(new Font("Verdana", Font.PLAIN, 30));
        graphics2D.drawString("" + userLevel.getRank(), (base.getWidth() - 310), 60);

        //endregion

        //region Level

        graphics2D.setColor(Color.WHITE);
        graphics2D.setFont(new Font("Verdana", Font.PLAIN, 20));
        graphics2D.drawString("Level", (base.getWidth() - 235), 60);

        graphics2D.setColor(Color.magenta.darker().darker());
        graphics2D.setFont(new Font("Verdana", Font.PLAIN, 30));
        graphics2D.drawString("" + userLevel.getLevel(), (base.getWidth() - 175), 60);

        //endregion

        graphics2D.setColor(Color.magenta.darker().darker());
        graphics2D.fillRoundRect(200, 130, (base.getWidth() - 300) * userLevel.getProgress() / 100, 50, 50, 50);

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
