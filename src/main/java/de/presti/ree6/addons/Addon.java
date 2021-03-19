package de.presti.ree6.addons;

import java.io.File;

public class Addon {

    String name;
    String author;
    String addonver;
    String ree6ver;
    String mainpath;
    File file;

    public Addon(String name, String author, String addonver, String ree6ver, String mainpath, File file) {
        this.name = name;
        this.author = author;
        this.addonver = addonver;
        this.ree6ver = ree6ver;
        this.mainpath = mainpath;
        this.file = file;
    }

    public String getName() {
        return name;
    }

    public String getAuthor() {
        return author;
    }

    public String getAddonver() {
        return addonver;
    }

    public String getRee6ver() {
        return ree6ver;
    }

    public String getMainpath() {
        return mainpath;
    }

    public File getFile() {
        return file;
    }

    @Override
    public String toString() {
        return "Addon{" +
                "name='" + getName() + '\'' +
                ", author='" + getAuthor() + '\'' +
                ", addonver='" + getAddonver() + '\'' +
                ", ree6ver='" + getRee6ver() + '\'' +
                ", mainpath='" + getMainpath() + '\'' +
                '}';
    }
}
