package de.presti.ree6.addons.impl;

import de.presti.ree6.main.Main;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created to work faster with the ChatProtector without calling the SQLWorker everytime.
 * @deprecated This command will be removed soon. In addition to a new upcoming feature, which uses AI Recognition to detect "bad words".
 * @since 1.7.13
 */
@Deprecated(forRemoval = true)
public class ChatProtector {

    /**
     * Constructor should not be called, since it is a utility class that doesn't need an instance.
     * @throws IllegalStateException it is a utility class.
     */
    private ChatProtector() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Blacklist a Word.
     *
     * @param guildId the ID of the Guild.
     * @param word    the Word you want to blacklist.
     * @return true, if the word has been blacklisted | false, if it is already blacklisted.
     */
    public static boolean blacklist(String guildId, String word) {
        if (!Main.getInstance().getSqlConnector().getSqlWorker().getChatProtectorWords(guildId).contains(word)) {
            Main.getInstance().getSqlConnector().getSqlWorker().addChatProtectorWord(guildId, word);
            return true;
        }
        return false;
    }

    /**
     * Blacklist a list of words.
     *
     * @param guildId  the ID of the Guild.
     * @param wordList the List of Words, which should be blacklisted.
     */
    public static void blacklist(String guildId, ArrayList<String> wordList) {

        ArrayList<String> blacklisted = (ArrayList<String>) Main.getInstance().getSqlConnector().getSqlWorker().getChatProtectorWords(guildId);

        wordList.stream().filter(s -> !blacklisted.contains(s)).forEach(s -> Main.getInstance().getSqlConnector().getSqlWorker().addChatProtectorWord(guildId, s));
    }

    /**
     * Remove a word from the Blacklist.
     *
     * @param guildId the ID of the Guild.
     * @param word    the Word that should be removed from the Blacklist.
     * @return true, if it was blacklisted and has been removed | false, if it wasn't blacklisted at all.
     */
    public static boolean removeFromBlacklist(String guildId, String word) {
        if (Main.getInstance().getSqlConnector().getSqlWorker().getChatProtectorWords(guildId).contains(word)) {
            Main.getInstance().getSqlConnector().getSqlWorker().removeChatProtectorWord(guildId, word);
            return true;
        }
        return false;
    }

    /**
     * Remove a List of words from the Blacklist.
     *
     * @param guildId  the ID of the Guild.
     * @param wordList the List of Words that should be removed from the Blacklist.
     */
    public static void removeFromBlacklist(String guildId, ArrayList<String> wordList) {
        wordList.stream().filter(Main.getInstance().getSqlConnector().getSqlWorker().getChatProtectorWords(guildId)::contains)
                .forEach(s -> Main.getInstance().getSqlConnector().getSqlWorker().removeChatProtectorWord(guildId, s));
    }

    /**
     * Check if the Chat-Protector is set up.
     *
     * @param guildId the ID of the Guild.
     * @return true, if there is the Chat-Protector is set up for the Guild. | false, if not.
     */
    public static boolean isChatProtectorSetup(String guildId) {
        return Main.getInstance().getSqlConnector().getSqlWorker()
                .isChatProtectorSetup(guildId);
    }

    /**
     * Get the Blacklisted Words.
     *
     * @param guildId the ID of the Guild.
     * @return an {@link ArrayList} with every Blacklisted word from the Guild.
     */
    public static ArrayList<String> getBlacklist(String guildId) {
        return (ArrayList<String>) Main.getInstance().getSqlConnector().getSqlWorker()
                .getChatProtectorWords(guildId);
    }

    /**
     * Check if the given Message contains any word that is blacklisted.
     *
     * @param guildId the ID of the Guild.
     * @param message the Message-Content.
     * @return true, if there is a blacklisted for contained.
     */
    public static boolean checkMessage(String guildId, String message) {
        return Arrays.stream(message.toLowerCase().split(" "))
                .anyMatch(getBlacklist(guildId)::contains);
    }
}
