package de.presti.ree6.utils;

import de.presti.ree6.bot.BotInfo;
import de.presti.ree6.sql.entities.UserLevel;
import net.dv8tion.jda.api.entities.User;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class ImageCreationUtility {

    /**
     * Constructor for the Image Creation Utility class.
     */
    private ImageCreationUtility() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Generate a Rank Image with Java native Graphics2D.
     *
     * @param userLevel the User Level Object.
     * @return the bytes of the Image.
     * @throws IOException when URL-Format is Invalid or the URL is not a valid Image.
     */
    public static byte[] createRankImage(UserLevel userLevel) throws IOException {
        if (userLevel == null || userLevel.getUser() == null)
            return new byte[128];

        // Generate a 885x211 Image Background.
        BufferedImage base = new BufferedImage(885, 211, BufferedImage.TYPE_INT_ARGB);

        User user = userLevel.getUser();
        BufferedImage userImage;

        // Generated a Circle Image with the Avatar of the User.
        if (user.getAvatarUrl() != null) {
            userImage = convertToCircleShape(new URL(user.getAvatarUrl()));
        } else {
            userImage = convertToCircleShape(new URL(user.getDefaultAvatarUrl()));
        }

        // Create a new Graphics2D instance from the base.
        Graphics2D graphics2D = base.createGraphics();

        // Change the background to black.
        graphics2D.setColor(Color.BLACK);
        graphics2D.fillRoundRect(0, 0, base.getWidth(), base.getHeight(), 10, 10);

        // TODO find a way to allow unicode Names.

        // Draw basic Information, such as the User Image, Username and the Experience Rect.
        graphics2D.drawImage(userImage, null, 25, 45);
        graphics2D.setColor(Color.WHITE);
        graphics2D.setFont(new Font("Verdana", Font.PLAIN, 40));
        graphics2D.drawString(new String(user.getName().getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8), 200, 110);
        graphics2D.fillRoundRect(200, 130, base.getWidth() - 300, 50, 50, 50);

        //region Experience

        // Draw The current Experience and needed Experience for the next Level.
        graphics2D.setFont(new Font("Verdana", Font.PLAIN, 20));
        graphics2D.drawString(userLevel.getFormattedExperience() + "", (base.getWidth() - 210), 110);
        graphics2D.setColor(Color.DARK_GRAY);
        graphics2D.drawString("/" + userLevel.getFormattedExperience(userLevel.getExperienceForNextLevel()), (base.getWidth() - 170), 110);

        //endregion

        //region Rank

        // Draw the current Ranking.
        graphics2D.setColor(Color.WHITE);
        graphics2D.setFont(new Font("Verdana", Font.PLAIN, 20));
        graphics2D.drawString("Rank", (base.getWidth() - 370), 60);

        graphics2D.setFont(new Font("Verdana", Font.PLAIN, 30));
        graphics2D.drawString("" + userLevel.getRank(), (base.getWidth() - 310), 60);

        //endregion

        //region Level

        // Draw the current Level.
        graphics2D.setColor(Color.WHITE);
        graphics2D.setFont(new Font("Verdana", Font.PLAIN, 20));
        graphics2D.drawString("Level", (base.getWidth() - 235), 60);

        graphics2D.setColor(Color.magenta.darker().darker());
        graphics2D.setFont(new Font("Verdana", Font.PLAIN, 30));
        graphics2D.drawString("" + userLevel.getLevel(), (base.getWidth() - 175), 60);

        //endregion

        // Draw the Progressbar.
        graphics2D.setColor(Color.magenta.darker().darker());
        graphics2D.fillRoundRect(200, 130, (base.getWidth() - 300) * userLevel.getProgress() / 100, 50, 50, 50);

        // Close the Graphics2d instance.
        graphics2D.dispose();

        // Create a ByteArrayOutputStream to convert the BufferedImage to a Array of Bytes.
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        ImageIO.write(base, "PNG", outputStream);
        return outputStream.toByteArray();
    }

    /**
     * Generated a Circle Shaped Version of the given Image.
     *
     * @param url the URL to the Image.
     * @return the edited {@link BufferedImage}.
     * @throws IOException if the link is not a valid Image.
     */
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
