package de.presti.ree6.addons.impl;

import de.presti.ree6.main.Main;

import java.util.ArrayList;

// TODO recode.

/**
 * Created to work faster with the ChatProtector without calling the SQLWorker everytime.
 * Needs a revamp.
 */
public class ChatProtector {


    public static void addWordToProtector(String gid, String word) {

        ArrayList<String> words = Main.getInstance().getSqlConnector().getSqlWorker().getChatProtectorWords(gid);

        if (!words.isEmpty() && !words.contains(word)) {
            Main.getInstance().getSqlConnector().getSqlWorker().addChatProtectorWord(gid, word);
        } else if (words.isEmpty()) {
            Main.getInstance().getSqlConnector().getSqlWorker().addChatProtectorWord(gid, word);
        }
    }

    public static void addWordsToProtector(String gid, ArrayList<String> words2) {

        ArrayList<String> words = Main.getInstance().getSqlConnector().getSqlWorker().getChatProtectorWords(gid);

        if (!words.isEmpty()) {
            for (String s : words2) {
                if (!words.contains(s)) {
                    Main.getInstance().getSqlConnector().getSqlWorker().addChatProtectorWord(gid, s);
                    words.add(s);
                }
            }
        } else {

            for (String s : words2) {
                if (!words.contains(s)) {
                    Main.getInstance().getSqlConnector().getSqlWorker().addChatProtectorWord(gid, s);
                    words.add(s);
                }
            }
        }
    }

    public static void removeWordsFromProtector(String gid, ArrayList<String> words2) {
        ArrayList<String> words = Main.getInstance().getSqlConnector().getSqlWorker().getChatProtectorWords(gid);

        if (!words.isEmpty()) {
            for (String s : words2) {
                if (words.contains(s)) {
                    Main.getInstance().getSqlConnector().getSqlWorker().removeChatProtectorWord(gid, s);
                    words.remove(s);
                }
            }
        }
    }

    public static void removeWordFromProtector(String gid, String word) {
        ArrayList<String> words = Main.getInstance().getSqlConnector().getSqlWorker().getChatProtectorWords(gid);
        if(!words.isEmpty()) {
            if(words.contains(word)) {
                Main.getInstance().getSqlConnector().getSqlWorker().removeChatProtectorWord(gid, word);
            }
        }
    }

    public static boolean hasChatProtector(String gid) {
        return Main.getInstance().getSqlConnector().getSqlWorker().isChatProtectorSetup(gid);
    }

    public static ArrayList<String> getChatProtector(String gid) {
        return Main.getInstance().getSqlConnector().getSqlWorker().getChatProtectorWords(gid);
    }

    public static boolean checkMessage(String gid, String message) {
        ArrayList<String> words = getChatProtector(gid);
        String[] args = message.toLowerCase().split(" ");

        for (String s : args) {
            if (words.contains(s.toLowerCase())) {
                return true;
            }
        }
        return false;
    }
}
