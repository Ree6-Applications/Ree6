package de.presti.ree6.addons;

import de.presti.ree6.bot.BotInfo;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;

public class AddonManager {

    public ArrayList<Addon> addons = new ArrayList<>();

    public AddonManager() {
    }

    public void startAddons() {
        for (Addon addon : addons) {

            if (!addon.getRee6ver().equalsIgnoreCase(BotInfo.build)) {
                System.out.println("The Addon " + addon.getName() + " by " + addon.getAuthor() + " has been developed for Ree6 b" + addon.getRee6ver() + " but you have a newer Version so becarefull!");
            }

            try {

                Class urlcl = new URLClassLoader(new URL[]{ addon.getFile().toURI().toURL()}).loadClass(addon.getMainpath());

                boolean valid = false;


                Class[] ifs = urlcl.getInterfaces();

                for(int i = 0; i < ifs.length && !valid; i++) {
                    if(ifs[i].getName().equalsIgnoreCase("de.presti.ree6.addons.AddonInterface")) {
                        valid = true;
                    }
                }

                if(valid) {
                    AddonInterface inf = (AddonInterface) urlcl.newInstance();
                    inf.onEnable();
                } else {
                    System.out.println("Couldnt start the Addon " + addon.getName() + "(" + addon.getAddonver() + ") by " + addon.getAuthor());
                    System.out.println("It doesnt implement the AddonInterface!");
                }


            } catch (Exception ex) {
                System.out.println("Couldnt start the Addon " + addon.getName() + "(" + addon.getAddonver() + ") by " + addon.getAuthor());
                System.out.println("Infos: " + addon.getMainpath() + ", " + addon.getAddonver() + ", " + addon.getRee6ver());
                System.out.println("Exception: " + ex.getCause().getMessage());
                ex.printStackTrace();
            }
        }
    }

    public void stopAddons() {
        for (Addon addon : addons) {
            try {
                Class urlcl = new URLClassLoader(new URL[]{ addon.getFile().toURI().toURL()}).loadClass(addon.getMainpath());

                boolean valid = false;


                Class[] ifs = urlcl.getInterfaces();

                for(int i = 0; i < ifs.length && !valid; i++) {
                    if(ifs[i].getName().equalsIgnoreCase("de.presti.ree6.addons.AddonInterface")) {
                        valid = true;
                    }
                }

                if(valid) {
                    AddonInterface inf = (AddonInterface) urlcl.newInstance();
                    inf.onDisable();
                } else {
                    System.out.println("Couldnt start the Addon " + addon.getName() + "(" + addon.getAddonver() + ") by " + addon.getAuthor());
                    System.out.println("It doesnt implement the AddonInterface!");
                }
            } catch (Exception ex) {
                System.out.println("Couldnt stop the Addon " + addon.getName() + "(" + addon.getAddonver() + ") by " + addon.getAuthor() + "\nException: " + ex.getCause().getMessage());
            }
        }
    }

    public void loadAddon(Addon addon) {
        addons.add(addon);
    }

}
