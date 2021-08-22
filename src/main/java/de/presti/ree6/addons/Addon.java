package de.presti.ree6.addons;

import java.io.File;

public class Addon {

    final String name;
    final String author;
    final String addonVer;
    final String ree6Ver;
    final String mainPath;
    final File file;

    public Addon(String name, String author, String addonVer, String ree6Ver, String mainPath, File file) {
        this.name = name;
        this.author = author;
        this.addonVer = addonVer;
        this.ree6Ver = ree6Ver;
        this.mainPath = mainPath;
        this.file = file;
    }

    public String getName() {
        return name;
    }

    public String getAuthor() {
        return author;
    }

    public String getAddonVer() {
        return addonVer;
    }

    public String getRee6Ver() {
        return ree6Ver;
    }

    public String getMainPath() {
        return mainPath;
    }

    public File getFile() {
        return file;
    }

    @Override
    public String toString() {
        return "Addon{" +
                "name='" + getName() + '\'' +
                ", author='" + getAuthor() + '\'' +
                ", addonVer='" + getAddonVer() + '\'' +
                ", ree6Ver='" + getRee6Ver() + '\'' +
                ", mainPath='" + getMainPath() + '\'' +
                ", file=" + getFile().getAbsolutePath() +
                '}';
    }
}
