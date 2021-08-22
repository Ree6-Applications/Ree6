package de.presti.ree6.addons.impl;

import de.presti.ree6.main.Main;

import java.util.ArrayList;

public class ChatProtector {


    public static void addWordtoProtector(String gid, String word) {

        ArrayList<String> words = Main.sqlWorker.getChatProtector(gid);

        if (!words.isEmpty() && !words.contains(word)) {
            Main.sqlWorker.addChatProtector(gid, word);
        } else if (words.isEmpty()) {
            Main.sqlWorker.addChatProtector(gid, word);
        }
    }

    public static void addWordstoProtector(String gid, ArrayList<String> words2) {

        ArrayList<String> words = Main.sqlWorker.getChatProtector(gid);

        if (!words.isEmpty()) {
            for (String s : words2) {
                if (!words.contains(s)) {
                    Main.sqlWorker.addChatProtector(gid, s);
                    words.add(s);
                }
            }
        } else {

            for (String s : words2) {
                if (!words.contains(s)) {
                    Main.sqlWorker.addChatProtector(gid, s);
                    words.add(s);
                }
            }
        }
    }

    public static void removeWordsfromProtector(String gid, ArrayList<String> words2) {
        ArrayList<String> words = Main.sqlWorker.getChatProtector(gid);

        if (!words.isEmpty()) {
            for (String s : words2) {
                if (words.contains(s)) {
                    Main.sqlWorker.removeChatProtector(gid, s);
                    words.remove(s);
                }
            }
        }
    }

    public static void removeWordfromProtector(String gid, String word) {
        ArrayList<String> words = Main.sqlWorker.getChatProtector(gid);
        if(!words.isEmpty()) {
            if(words.contains(word)) {
                Main.sqlWorker.removeChatProtector(gid, word);
            }
        }
    }

    public static boolean hasChatProtector(String gid) {
        return Main.sqlWorker.hasChatProtectorSetuped(gid);
    }

    public static ArrayList<String> getChatProtector(String gid) {
        return Main.sqlWorker.getChatProtector(gid);
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
