package core;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.*;


//klasa bazowa dla klas z mechanizmem extentu
public class ObjectPlus implements Serializable {
    // Zachowuje zgodnosc z plikami zapisanymi przed dodaniem obslugi sciezek.
    private static final long serialVersionUID = 5712865437999136732L;

    //extensja
    private static Map<Class<?>, List<Object>> extent = new HashMap<>();

    public static final String EXTENT_NAME = "data/ObjectPlus.data";

    public static Path getExtentPath() {
        String configuredPath = System.getProperty("steamlite.dataFile");
        if (configuredPath != null && !configuredPath.isBlank()) {
            return Path.of(configuredPath).toAbsolutePath().normalize();
        }

        var codeSource = ObjectPlus.class.getProtectionDomain().getCodeSource();
        if (codeSource == null) {
            return Path.of(EXTENT_NAME).toAbsolutePath().normalize();
        }
        try {
            Path location = Path.of(codeSource.getLocation().toURI()).toAbsolutePath().normalize();
            Path applicationDirectory = Files.isDirectory(location) ? location : location.getParent();
            // Obsluguje out/, katalogi IDE oraz zmiane nazwy lub polozenia projektu.
            for (Path directory = applicationDirectory; directory != null; directory = directory.getParent()) {
                if (Files.isDirectory(directory.resolve("src"))) {
                    return directory.resolve(EXTENT_NAME);
                }
            }
            if (applicationDirectory.getFileName() != null
                    && Set.of("out", "classes", "bin").contains(applicationDirectory.getFileName().toString())) {
                applicationDirectory = applicationDirectory.getParent();
            }
            return applicationDirectory.resolve(EXTENT_NAME);
        } catch (URISyntaxException ex) {
            throw new IllegalStateException("Cannot locate the application data directory.", ex);
        }
    }

    public ObjectPlus() {
        addToExtent();
    }


    public static <T> List<T> getExtentFromClass(Class<T> c) {
        extent.computeIfAbsent(c, k -> new ArrayList<>());
        List<T> result = (List<T>) Collections.unmodifiableList(extent.get(c));
        return result;
    }

    public static <T> List<T> getExtentFromClassIncludingSubclasses(Class<T> c) {
        List<T> result = new ArrayList<>();
        for (Map.Entry<Class<?>, List<Object>> entry : extent.entrySet()) {
            if (c.isAssignableFrom(entry.getKey())) {
                for (Object obj : entry.getValue()) {
                    result.add(c.cast(obj));
                }
            }
        }
        return Collections.unmodifiableList(result);
    }


    private void addToExtent () {
        extent.computeIfAbsent(this.getClass(), k -> new ArrayList<>()).add(this);
    }


    public void removeFromExtent() {
        List<Object> list = extent.get(this.getClass());
        if (list != null) {
            list.remove(this);
        }
    }

    //metody do zapisu i odczytu extentu (trwałość)
    public static void saveExtent() throws IOException {
        Path target = getExtentPath();
        Files.createDirectories(target.getParent());
        Path temporary = Files.createTempFile(target.getParent(), "ObjectPlus-", ".tmp");
        try {
            try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(temporary))) {
                oos.writeObject(extent);
            }
            try {
                Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException ex) {
                Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporary);
        }
    }


    public static void loadExtent() throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(getExtentPath()))) {
            extent = (Map<Class<?>, List<Object>>) ois.readObject();
        }
    }
    
}
