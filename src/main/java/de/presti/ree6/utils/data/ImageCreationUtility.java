package de.presti.ree6.utils.data;

import de.presti.ree6.bot.BotConfig;
import de.presti.ree6.bot.BotWorker;
import de.presti.ree6.main.Main;
import de.presti.ree6.sql.entities.level.UserLevel;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.User;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;

// TODO:: translate.

/**
 * A utility to create Images.
 */
@Slf4j
public class ImageCreationUtility {

    /**
     * Constructor for the Image Creation Utility class.
     */
    private ImageCreationUtility() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Cached Background Image, for performance.
     */
    private static BufferedImage rankBackgroundBase;

    /**
     * Cached Background Image, for performance.
     * Key is the hash of the Image and the value is the already created Image.
     */
    private static final HashMap<String, BufferedImage> joinBackgroundBase = new HashMap<>();

    /**
     * Generate a Rank Image with Java native Graphics2D.
     *
     * @param userLevel the User Level Object.
     * @return the bytes of the Image.
     * @throws IOException when URL-Format is Invalid or the URL is not a valid Image.
     */
    public static byte[] createRankImage(UserLevel userLevel) throws IOException {
        long start = System.currentTimeMillis();
        long actionPerformance = System.currentTimeMillis();

        Main.getInstance().logAnalytic("Started User Rank Image creation.");
        Main.getInstance().logAnalytic("Getting User. ({}ms)", System.currentTimeMillis() - actionPerformance);
        actionPerformance = System.currentTimeMillis();

        User user = BotWorker.getShardManager().getUserById(userLevel.getUserId());

        Main.getInstance().logAnalytic("Getting default needed Data. ({}ms)", System.currentTimeMillis() - actionPerformance);
        actionPerformance = System.currentTimeMillis();

        String formattedExperience,
                formattedMaxExperience,
                level,
                rank;

        double progress;

        formattedExperience = userLevel.getFormattedExperience();
        formattedMaxExperience = userLevel.getFormattedNeededExperience();
        level = String.valueOf(userLevel.getLevel());
        rank = String.valueOf(userLevel.getRank());
        progress = userLevel.getProgress();

        Main.getInstance().logAnalytic("Starting actual creation. ({}ms)", System.currentTimeMillis() - actionPerformance);
        actionPerformance = System.currentTimeMillis();

        if (user == null)
            return new byte[128];

        Main.getInstance().logAnalytic("Loading and creating Background base. ({}ms)", System.currentTimeMillis() - actionPerformance);
        actionPerformance = System.currentTimeMillis();

        // Generate a 885x211 Image Background.
        if (rankBackgroundBase == null) rankBackgroundBase = ImageIO.read(new File("storage/images/base.png"));
        BufferedImage base = new BufferedImage(1920, 1080, BufferedImage.TYPE_INT_ARGB);

        Main.getInstance().logAnalytic("Loaded and created Background base. ({}ms)", System.currentTimeMillis() - actionPerformance);
        actionPerformance = System.currentTimeMillis();

        BufferedImage userImage;

        Main.getInstance().logAnalytic("Getting User avatar. ({}ms)", System.currentTimeMillis() - actionPerformance);
        actionPerformance = System.currentTimeMillis();
        // Generated a Circle Image with the Avatar of the User.
        userImage = convertToCircleShape(new URL(user.getEffectiveAvatarUrl()));

        Main.getInstance().logAnalytic("Creating Graphics2D. ({}ms)", System.currentTimeMillis() - actionPerformance);
        actionPerformance = System.currentTimeMillis();
        // Create a new Graphics2D instance from the base.
        Graphics2D graphics2D = base.createGraphics();
        Main.getInstance().logAnalytic("Created Graphics2D. ({}ms)", System.currentTimeMillis() - actionPerformance);
        actionPerformance = System.currentTimeMillis();
        // Change the background to black.

        Main.getInstance().logAnalytic("Making Background Transparent. ({}ms)", System.currentTimeMillis() - actionPerformance);
        actionPerformance = System.currentTimeMillis();
        // Make it transparent.
        graphics2D.setComposite(AlphaComposite.Clear);
        graphics2D.fillRect(0, 0, base.getWidth(), base.getHeight());
        Main.getInstance().logAnalytic("Finished drawing Background Transparent. ({}ms)", System.currentTimeMillis() - actionPerformance);
        actionPerformance = System.currentTimeMillis();

        Main.getInstance().logAnalytic("Drawing Background Image. ({}ms)", System.currentTimeMillis() - actionPerformance);
        actionPerformance = System.currentTimeMillis();
        // Draw Background art.
        graphics2D.setComposite(AlphaComposite.Src);
        graphics2D.drawImage(rankBackgroundBase, null, 0, 0);
        graphics2D.setColor(Color.WHITE);
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Main.getInstance().logAnalytic("Finished drawing Background Image. ({}ms)", System.currentTimeMillis() - actionPerformance);
        actionPerformance = System.currentTimeMillis();

        Main.getInstance().logAnalytic("Drawing User Image. ({}ms)", System.currentTimeMillis() - actionPerformance);
        actionPerformance = System.currentTimeMillis();
        // Draw basic Information, such as the User Image, Username and the Experience Rect.
        graphics2D.setComposite(AlphaComposite.SrcAtop);
        graphics2D.drawImage(userImage, null, 175, 450);
        graphics2D.setColor(Color.WHITE);
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Main.getInstance().logAnalytic("Finished drawing User Image. ({}ms)", System.currentTimeMillis() - actionPerformance);
        actionPerformance = System.currentTimeMillis();

        String discriminatorText = "#" + user.getDiscriminator();

        Font verdana60 = retrieveFont(60, discriminatorText);
        Font verdana50 = retrieveFont(50, rank);
        Font verdana40 = retrieveFont(40, formattedExperience + " Rank Level");

        Main.getInstance().logAnalytic("Finished creating Fonts. ({}ms)", System.currentTimeMillis() - actionPerformance);
        actionPerformance = System.currentTimeMillis();

        String username = new String(user.getName().getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);

        if (username.length() > 13) {
            username = username.substring(0, 12);
        }

        Main.getInstance().logAnalytic("Finished substring on Username. ({}ms)", System.currentTimeMillis() - actionPerformance);
        actionPerformance = System.currentTimeMillis();

        graphics2D.setColor(BotConfig.getRankTextColor());
        graphics2D.setFont(verdana60);
        graphics2D.drawString(username, 425, 675);

        // TODO:: remove this once discord removes discriminators.
        if (!user.getDiscriminator().equals("#0000")) {
            graphics2D.setColor(BotConfig.getRankDetailColor());
            graphics2D.setFont(verdana40);
            graphics2D.drawString(discriminatorText, 425, 675 - graphics2D.getFontMetrics(verdana60).getHeight() + 5);
        }

        graphics2D.setColor(BotConfig.getRankProgressbarBackgroundColor());
        graphics2D.fillRoundRect(175, 705, base.getWidth() - 950, 50, 50, 50);

        Main.getInstance().logAnalytic("Finished drawing User-Info on card. ({}ms)", System.currentTimeMillis() - actionPerformance);
        actionPerformance = System.currentTimeMillis();

        //region Experience

        // Draw The current Experience and needed Experience for the next Level.
        graphics2D.setColor(BotConfig.getRankDetailColor());
        graphics2D.setFont(verdana40);
        graphics2D.drawString(formattedExperience, (base.getWidth() - 800) - (graphics2D.getFontMetrics().stringWidth("/" + formattedMaxExperience)) - 5 - graphics2D.getFontMetrics().stringWidth(formattedExperience + ""), 675);
        graphics2D.setColor(BotConfig.getRankDetailColor().darker());
        graphics2D.drawString("/" + formattedMaxExperience, (base.getWidth() - 800) - (graphics2D.getFontMetrics().stringWidth("/" + formattedMaxExperience)), 675);

        Main.getInstance().logAnalytic("Finished drawing User-Experience on card. ({}ms)", System.currentTimeMillis() - actionPerformance);
        actionPerformance = System.currentTimeMillis();
        //endregion

        //region Rank

        // Draw the current Ranking.
        graphics2D.setColor(BotConfig.getRankTextColor());
        graphics2D.setFont(verdana40);
        graphics2D.drawString("Rank", (base.getWidth() - 800) - (graphics2D.getFontMetrics(verdana50).stringWidth(rank)) - (graphics2D.getFontMetrics().stringWidth("Rank")) - 10, 675 - graphics2D.getFontMetrics().getHeight() - graphics2D.getFontMetrics().getHeight());

        graphics2D.setColor(BotConfig.getRankHighlightColor());
        graphics2D.setFont(verdana50);
        graphics2D.drawString(rank, (base.getWidth() - 800) - (graphics2D.getFontMetrics().stringWidth(rank)), 675 - graphics2D.getFontMetrics(verdana40).getHeight() - graphics2D.getFontMetrics(verdana40).getHeight());

        Main.getInstance().logAnalytic("Finished drawing User-Rank on card. ({}ms)", System.currentTimeMillis() - actionPerformance);
        actionPerformance = System.currentTimeMillis();
        //endregion

        //region Level

        // Draw the current Level.
        graphics2D.setColor(BotConfig.getRankTextColor());
        graphics2D.setFont(verdana40);
        graphics2D.drawString("Level", (base.getWidth() - 800) - (graphics2D.getFontMetrics(verdana50).stringWidth(level)) - (graphics2D.getFontMetrics().stringWidth("Level")) - 10, 675 - graphics2D.getFontMetrics().getHeight());

        graphics2D.setColor(BotConfig.getRankHighlightColor());
        graphics2D.setFont(verdana50);
        graphics2D.drawString(level, (base.getWidth() - 800) - (graphics2D.getFontMetrics().stringWidth(level)), 675 - graphics2D.getFontMetrics(verdana40).getHeight());

        Main.getInstance().logAnalytic("Finished drawing User-Level on card. ({}ms)", System.currentTimeMillis() - actionPerformance);
        actionPerformance = System.currentTimeMillis();

        //endregion

        // Draw the Progressbar.
        graphics2D.setColor(BotConfig.getRankProgressbarColor());
        graphics2D.fillRoundRect(175, 705, (base.getWidth() - 950) * (int) progress / 100, 50, 50, 50);

        Main.getInstance().logAnalytic("Finished drawing Progressbar on card. ({}ms)", System.currentTimeMillis() - actionPerformance);
        actionPerformance = System.currentTimeMillis();

        // Close the Graphics2D instance.
        graphics2D.dispose();
        Main.getInstance().logAnalytic("Finished disposing Graphics2D instance. ({}ms)", System.currentTimeMillis() - actionPerformance);
        actionPerformance = System.currentTimeMillis();

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            ImageIO.write(base, "PNG", outputStream);
            Main.getInstance().logAnalytic("Finished writing Image into ByteArrayOutputStream. ({}ms)", System.currentTimeMillis() - actionPerformance);
            Main.getInstance().logAnalytic("Finished creation in {}ms", System.currentTimeMillis() - start);
            return outputStream.toByteArray();
        }
    }

    /**
     * This method is used to create a Join Image.
     *
     * @param user         The User who joined the Guild.
     * @param messageImage The Image of the Message.
     * @param messageText  The Text of the Message.
     * @return The Image as an Array of Bytes.
     * @throws IOException If an error occurs while creating the Image.
     */
    public static byte[] createJoinImage(User user, String messageImage, String messageText) throws IOException {
        long start = System.currentTimeMillis();
        long actionPerformance = System.currentTimeMillis();

        BufferedImage base = new BufferedImage(1920, 1080, BufferedImage.TYPE_INT_ARGB);

        // Load the Background Image.
        BufferedImage backgroundImage;

        if (joinBackgroundBase.containsKey(messageImage)) {
            backgroundImage = joinBackgroundBase.get(messageImage);
        } else {
            backgroundImage = resize(ImageIO.read(new ByteArrayInputStream(Base64.getDecoder().decode(messageImage))), 1920, 1080);
            if (backgroundImage == null) {
                try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

                    ImageIO.write(base, "PNG", outputStream);
                    return outputStream.toByteArray();
                }
            }
            joinBackgroundBase.put(messageImage, backgroundImage);
        }

        Main.getInstance().logAnalytic("Finished loading Base Image. ({}ms)", System.currentTimeMillis() - actionPerformance);
        actionPerformance = System.currentTimeMillis();

        // Load the Avatar Image.
        BufferedImage avatar = convertToCircleShape(new URL(user.getEffectiveAvatarUrl()));
        Main.getInstance().logAnalytic("Finished loading Avatar Image. ({}ms)", System.currentTimeMillis() - actionPerformance);
        actionPerformance = System.currentTimeMillis();

        // Create a Graphics2D instance to draw on the Base Image.
        Graphics2D graphics2D = base.createGraphics();
        Main.getInstance().logAnalytic("Finished creating Graphics2D instance. ({}ms)", System.currentTimeMillis() - actionPerformance);

        actionPerformance = System.currentTimeMillis();

        // Make it transparent.
        graphics2D.setComposite(AlphaComposite.Clear);
        graphics2D.fillRect(0, 0, base.getWidth(), base.getHeight());

        // Draw Background art.
        graphics2D.setComposite(AlphaComposite.Src);
        graphics2D.drawImage(backgroundImage, null, 0, 0);
        graphics2D.setColor(Color.WHITE);
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        graphics2D.setComposite(AlphaComposite.SrcOver);

        // Draw the Avatar Image on the Base Image. 125
        graphics2D.drawImage(avatar, backgroundImage.getWidth() / 2 - 250, backgroundImage.getHeight() / 2 - 375, 500, 500, null);
        Main.getInstance().logAnalytic("Finished drawing Avatar Image on Base Image. ({}ms)", System.currentTimeMillis() - actionPerformance);
        actionPerformance = System.currentTimeMillis();

        Font verdana30 = retrieveFont(35, messageText.replace("\n", " "));

        Main.getInstance().logAnalytic("Finished creating Fonts. ({}ms)", System.currentTimeMillis() - actionPerformance);
        actionPerformance = System.currentTimeMillis();

        graphics2D.setFont(verdana30);

        if (messageText.contains("\n")) {
            String[] lines = messageText.split("\n");
            for (int i = 0; i < lines.length; i++) {
                graphics2D.drawString(lines[i],
                        backgroundImage.getWidth() / 2 - (graphics2D.getFontMetrics(verdana30).stringWidth(lines[i]) / 2),
                        backgroundImage.getHeight() / 2 + 125 + (graphics2D.getFontMetrics(verdana30).getHeight() * (i + 1)));
            }
        } else {
            graphics2D.drawString(messageText,
                    backgroundImage.getWidth() / 2 - (graphics2D.getFontMetrics(verdana30).stringWidth(messageText) / 2),
                    backgroundImage.getHeight() / 2 + 125 + graphics2D.getFontMetrics(verdana30).getHeight());
        }

        // Close the Graphics2D instance.
        graphics2D.dispose();
        Main.getInstance().logAnalytic("Finished disposing Graphics2D instance. ({}ms)", System.currentTimeMillis() - actionPerformance);
        actionPerformance = System.currentTimeMillis();

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            ImageIO.write(base, "PNG", outputStream);
            Main.getInstance().logAnalytic("Finished writing Image into ByteArrayOutputStream. ({}ms)", System.currentTimeMillis() - actionPerformance);
            Main.getInstance().logAnalytic("Finished creation in {}ms", System.currentTimeMillis() - start);
            return outputStream.toByteArray();
        }
    }

    /**
     * Generate a HornyJail Image with Java native Graphics2D.
     *
     * @param user the User Object.
     * @return the bytes of the Image.
     * @throws IOException when URL-Format is Invalid or the URL is not a valid Image.
     */
    public static byte[] createHornyJailImage(User user) throws IOException {
        if (user == null)
            return new byte[128];

        // Generate a 128x128 Image Background.
        BufferedImage base = new BufferedImage(128, 128, BufferedImage.TYPE_INT_RGB);
        BufferedImage userImage;

        // Generated a Circle Image with the Avatar of the User.
        userImage = resize(ImageIO.read(new URL(user.getEffectiveAvatarUrl())), 128, 128);

        // Create a new Graphics2D instance from the base.
        Graphics2D graphics2D = base.createGraphics();

        // Change the background to black.
        graphics2D.setColor(Color.BLACK);
        graphics2D.fillRoundRect(0, 0, base.getWidth(), base.getHeight(), 10, 10);

        // Draw basic Information, such as the User Image, Username and the Experience Rect.
        graphics2D.drawImage(userImage, null, 0, 0);
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int xOffset = base.getWidth() / 7;

        for (int i = -1; i < 7; i++) {
            graphics2D.setColor(Color.DARK_GRAY);
            graphics2D.drawRect((xOffset * i) + xOffset, -1, 4, 130);
            graphics2D.setColor(Color.GRAY);
            graphics2D.fillRect((xOffset * i) + xOffset + 1, -1, 3, 130);
        }

        // Close the Graphics2D instance.
        graphics2D.dispose();

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            ImageIO.write(base, "PNG", outputStream);
            return outputStream.toByteArray();
        }
    }

    /**
     * Generated a Circle Shaped Version of the given Image.
     *
     * @param url the URL to the Image.
     * @return the edited {@link BufferedImage}.
     * @throws IOException if the link is not a valid Image.
     */
    public static BufferedImage convertToCircleShape(URL url) throws IOException {
        long start = System.currentTimeMillis();
        long actionPerformance = System.currentTimeMillis();

        Main.getInstance().logAnalytic("Started User Image creation.");

        BufferedImage mainImage = resize(ImageIO.read(url), 250, 250);

        if (mainImage == null) return null;

        Main.getInstance().logAnalytic("Loading Image from URL and resizing it. ({}ms)", System.currentTimeMillis() - actionPerformance);
        actionPerformance = System.currentTimeMillis();

        BufferedImage output = new BufferedImage(mainImage.getWidth(), mainImage.getHeight(), BufferedImage.TYPE_INT_ARGB);

        Main.getInstance().logAnalytic("Creating Output BufferedImage. ({}ms)", System.currentTimeMillis() - actionPerformance);
        actionPerformance = System.currentTimeMillis();

        Graphics2D g2 = output.createGraphics();

        Main.getInstance().logAnalytic("Created Graphics2D. ({}ms)", System.currentTimeMillis() - actionPerformance);
        actionPerformance = System.currentTimeMillis();

        // This is what we want, but it only does hard-clipping, i.e. aliasing
        // g2.setClip(new RoundRectangle2D ...)

        // so instead fake soft-clipping by first drawing the desired clip shape
        // in fully opaque white with antialiasing enabled...
        g2.setComposite(AlphaComposite.Src);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Color.WHITE);
        Main.getInstance().logAnalytic("Set Graphic Presets. ({}ms)", System.currentTimeMillis() - actionPerformance);
        actionPerformance = System.currentTimeMillis();
        g2.fill(new Ellipse2D.Float(0, 0, mainImage.getHeight(), mainImage.getHeight()));
        Main.getInstance().logAnalytic("Filled Graphic Base with Image. ({}ms)", System.currentTimeMillis() - actionPerformance);
        actionPerformance = System.currentTimeMillis();

        // ... then compositing the image on top,
        // using the white shape from above as alpha source
        g2.setComposite(AlphaComposite.SrcAtop);
        g2.drawImage(mainImage, 0, 0, null);

        Main.getInstance().logAnalytic("Drawing Image finished. ({}ms)", System.currentTimeMillis() - actionPerformance);
        actionPerformance = System.currentTimeMillis();

        g2.dispose();

        Main.getInstance().logAnalytic("Disposing Graphics2D finished. ({}ms)", System.currentTimeMillis() - actionPerformance);

        Main.getInstance().logAnalytic("Finished creation. ({}ms)", System.currentTimeMillis() - start);
        return output;
    }

    /**
     * Resizes an image to an absolute width and height (the image may not be
     * proportional)
     *
     * @param inputImage   The original image
     * @param scaledWidth  absolute width in pixels
     * @param scaledHeight absolute height in pixels
     * @return The new resized image
     */
    public static BufferedImage resize(BufferedImage inputImage, int scaledWidth, int scaledHeight) {
        if (inputImage == null)
            return null;

        if (inputImage.getWidth() == scaledWidth && inputImage.getHeight() == scaledHeight) {
            return inputImage;
        }

        // creates output image
        BufferedImage outputImage = new BufferedImage(scaledWidth,
                scaledHeight, inputImage.getType());

        // scales the input image to the output image
        Graphics2D g2d = outputImage.createGraphics();
        g2d.drawImage(inputImage, 0, 0, scaledWidth, scaledHeight, null);
        g2d.dispose();

        return outputImage;
    }

    /**
     * Retrieves a Font with the given size and fallbacks to Arial if it can't display the given text.
     *
     * @param size the size of the Font.
     * @param text the text to display.
     * @return the Font.
     */
    public static Font retrieveFont(int size, String text) {
        Font font = new Font(BotConfig.getTextFont(), Font.PLAIN, size);

        if (font.canDisplayUpTo(text) != -1) {
            font = new Font("Arial", Font.PLAIN, size);
        }

        return font;
    }
}
