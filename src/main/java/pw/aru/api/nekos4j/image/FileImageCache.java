package pw.aru.api.nekos4j.image;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

class FileImageCache implements ImageCache {
    private final int bufferSize;
    private final File directory;
    private final Map<String, ReentrantLock> modified = new ConcurrentHashMap<>();

    FileImageCache(File directory, int bufferSize) throws IOException {
        if (directory.isFile()) {
            throw new IOException("Directory is a file: " + directory.getAbsolutePath());
        }
        if (!directory.exists()) {
            if (!directory.mkdirs()) {
                throw new IOException("Unable to create directory: " + directory.getAbsolutePath());
            }
        }
        if (!directory.canRead()) {
            throw new IOException("Unable to read directory: " + directory.getAbsolutePath());
        }
        if (!directory.canWrite()) {
            throw new IOException("Unable to write directory: " + directory.getAbsolutePath());
        }
        if (bufferSize < 1) {
            throw new IllegalArgumentException("Buffer size < 1");
        }
        this.directory = directory;
        this.bufferSize = bufferSize;
    }

    @Override
    @Nullable
    public InputStream retrieve(@Nonnull String name) throws IOException {
        ReentrantLock lock = modified.computeIfAbsent(name, k -> new ReentrantLock());
        lock.lock();
        try {
            File image = new File(directory, name);
            if (!image.exists()) return null;
            return new FileInputStream(image);
        } finally {
            lock.unlock();
            modified.remove(name, lock);
        }
    }

    @Override
    public void save(@Nonnull String name, @Nonnull InputStream in) throws IOException {
        ReentrantLock lock = modified.computeIfAbsent(name, k -> new ReentrantLock());
        lock.lock();
        try (FileOutputStream fos = new FileOutputStream(new File(directory, name));
             InputStream i = in) {
            byte[] buffer = new byte[bufferSize];
            int r;
            while ((r = i.read(buffer)) != -1) {
                fos.write(buffer, 0, r);
            }
        } finally {
            lock.unlock();
            modified.remove(name, lock);
        }
    }

    @Override
    public void purge(@Nonnull String name) throws IOException {
        File f = new File(directory, name);
        if (!f.exists()) return;
        ReentrantLock lock = modified.computeIfAbsent(name, k -> new ReentrantLock());
        lock.lock();
        try {
            if (!f.exists()) return;
            if (!f.delete()) {
                throw new IOException("Unable to delete file");
            }
        } finally {
            lock.unlock();
            modified.remove(name, lock);
        }
    }
}
