package de.presti.ree6.addons;

import de.presti.ree6.main.Main;

import java.util.ArrayList;
import java.util.HashMap;

public class ChatProtector {

    public static HashMap<String, ArrayList<String>> chachedchatprotector = new HashMap<>();

    public static void addWordtoProtector(String gid, String word) {

        ArrayList<String> words;
        if (chachedchatprotector.containsKey(gid)) {
            words = chachedchatprotector.get(gid);
            words.add(word);

            chachedchatprotector.remove(gid);
        } else {
            words = new ArrayList<>();
            words.add(word);
        }
        chachedchatprotector.put(gid, words);
    }

    public static void addWordstoProtector(String gid, ArrayList<String> words2) {

        ArrayList<String> words;
        if (chachedchatprotector.containsKey(gid)) {
            words = chachedchatprotector.get(gid);

            for (String s : words2) {
                words.add(s);
            }

            chachedchatprotector.remove(gid);
        } else {
            words = new ArrayList<>();

            for (String s : words2) {
                words.add(s);
            }
        }
        chachedchatprotector.put(gid, words);
    }

    public static void removeWordsfromProtector(String gid, ArrayList<String> words2) {
        if (chachedchatprotector.containsKey(gid)) {
            ArrayList<String> words = chachedchatprotector.get(gid);


            for (String s : words2) {
                if (words.contains(s)) {
                    words.remove(s);
                }
            }

            chachedchatprotector.remove(gid);
            chachedchatprotector.put(gid, words);
        }
    }

    public static void removeWordfromProtector(String gid, String word) {
        if (chachedchatprotector.containsKey(gid)) {
            ArrayList<String> words = chachedchatprotector.get(gid);

            if (words.contains(word)) {
                words.remove(word);
            }

            chachedchatprotector.remove(gid);
            chachedchatprotector.put(gid, words);
        }
    }

    public static boolean hasChatProtector(String gid) {
        return Main.sqlWorker.hasChatProtectorSetuped(gid);
    }

    public static ArrayList<String> getChatProtector(String gid) {
        if (chachedchatprotector.containsKey(gid)) {
            return chachedchatprotector.get(gid);
        } else if (hasChatProtector(gid)) {
            chachedchatprotector.put(gid, Main.sqlWorker.getChatProtector(gid));
            return chachedchatprotector.get(gid);
        }
        return new ArrayList<>();
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

    public static boolean hasChatProtector2(String gid) {
        return chachedchatprotector.containsKey(gid);
    }
}
