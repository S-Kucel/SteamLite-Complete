import app.gui.AppDataLoader;
import app.gui.AppState;
import core.Customer;
import core.Game;
import core.ObjectPlus;
import java.io.IOException;
import java.io.NotSerializableException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

public final class StartupRegressionTest {
    private static final class UnsavableEntry extends ObjectPlus {
        private final Object value = new Object();
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
        System.out.println("PASS " + message);
    }

    public static void main(String[] args) throws Exception {
        if (args[0].equals("suite")) {
            runSuite(Path.of(args[1]));
            return;
        }

        String scenario = args[0];
        Path expected = Path.of(args[1]);
        check(ObjectPlus.getExtentPath().equals(expected), scenario + ": data path independent of working directory");
        if (scenario.equals("corrupt") || scenario.equals("read-error")) {
            byte[] before = scenario.equals("corrupt") ? Files.readAllBytes(expected) : null;
            boolean rejected = false;
            try {
                AppDataLoader.load();
            } catch (IOException ex) {
                rejected = true;
            }
            check(rejected, scenario + ": read error reported");
            check(ObjectPlus.getExtentFromClassIncludingSubclasses(Customer.class).isEmpty(),
                    scenario + ": demo data was not created");
            check(before == null ? Files.isDirectory(expected) : Arrays.equals(before, Files.readAllBytes(expected)),
                    scenario + ": existing data preserved");
            return;
        }

        byte[] original = Files.exists(expected) ? Files.readAllBytes(expected) : null;
        int savedCustomers = -1;
        if (original != null) {
            ObjectPlus.loadExtent();
            savedCustomers = ObjectPlus.getExtentFromClassIncludingSubclasses(Customer.class).size();
        }
        AppState state = new AppState(AppDataLoader.load());
        check(!state.getGames().isEmpty() && !state.getCustomers().isEmpty(), scenario + ": startup succeeds");
        if (original != null) {
            check(state.getCustomers().size() == savedCustomers && Arrays.equals(original, Files.readAllBytes(expected)),
                    scenario + ": existing data loaded without replacement");
        } else {
            check(Files.isRegularFile(expected), scenario + ": missing data directory and file created");
        }

        if (scenario.equals("failed-save")) {
            new UnsavableEntry();
            boolean rejected = false;
            try {
                state.save();
            } catch (NotSerializableException ex) {
                rejected = true;
            }
            check(rejected && Arrays.equals(original, Files.readAllBytes(expected)),
                    "failed-save: serialization failure preserves previous save");
            try (var files = Files.list(expected.getParent())) {
                check(files.noneMatch(path -> path.toString().endsWith(".tmp")), "failed-save: temporary file cleaned up");
            }
        } else if (scenario.equals("roundtrip")) {
            Customer customer = state.registerCustomer("startupRegression", "TestPassword123", 25, false, null);
            Game game = state.getGames().get(0);
            customer.addFunds(game.getPrice() + 50);
            state.purchaseGame(customer, game);
            state.save();
            AppState restored = new AppState(AppDataLoader.load());
            Customer restoredCustomer = restored.login("startupRegression", "TestPassword123").getCustomer();
            check(restoredCustomer.findGameInLibrary(game.getTitle()) != null
                            && restored.getPurchasesFor(restoredCustomer).size() == 1
                            && Math.abs(restoredCustomer.getBalance() - 50) < 0.000001,
                    "roundtrip: account, library, purchase and balance survive restart");
            boolean duplicateRejected = false;
            try {
                restored.registerCustomer("startupRegression", "TestPassword123", 25, false, null);
            } catch (RuntimeException ex) {
                duplicateRejected = true;
            }
            check(duplicateRejected, "roundtrip: duplicate username rejected after reload");
        }
    }

    private static void runSuite(Path originalData) throws Exception {
        Path temporary = Files.createTempDirectory("SteamLite-regression-");
        Path compiledClasses = Path.of(ObjectPlus.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        Path testClasses = Path.of(StartupRegressionTest.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        Path unrelatedDirectory = Files.createDirectories(temporary.resolve("unrelated working directory/data")).getParent();
        Files.writeString(unrelatedDirectory.resolve(ObjectPlus.EXTENT_NAME), "This file must not be used.");
        try {
            for (String scenario : List.of("existing", "fresh", "roundtrip", "override", "corrupt", "read-error", "failed-save")) {
                Path project = temporary.resolve(scenario + " renamed project with spaces");
                Files.createDirectories(project.resolve("src"));
                Path output = project.resolve("out");
                try (var paths = Files.walk(compiledClasses)) {
                    for (Path source : paths.toList()) {
                        Path destination = output.resolve(compiledClasses.relativize(source));
                        if (Files.isDirectory(source)) {
                            Files.createDirectories(destination);
                        } else {
                            Files.copy(source, destination);
                        }
                    }
                }
                Path expected = scenario.equals("override")
                        ? temporary.resolve("custom data/nested/ObjectPlus.data") : project.resolve(ObjectPlus.EXTENT_NAME);
                if (List.of("existing", "roundtrip", "failed-save").contains(scenario)) {
                    Files.createDirectories(expected.getParent());
                    Files.copy(originalData, expected);
                } else if (scenario.equals("corrupt")) {
                    Files.createDirectories(expected.getParent());
                    Files.writeString(expected, "Invalid serialized data");
                } else if (scenario.equals("read-error")) {
                    Files.createDirectories(expected);
                }

                List<String> command = new ArrayList<>();
                command.add(Path.of(System.getProperty("java.home"), "bin", "java.exe").toString());
                command.add("-Djava.awt.headless=true");
                if (scenario.equals("override")) {
                    command.add("-Dsteamlite.dataFile=" + expected);
                }
                command.addAll(List.of("-cp", output + System.getProperty("path.separator") + testClasses,
                        StartupRegressionTest.class.getName(), scenario, expected.toString()));
                Process process = new ProcessBuilder(command).directory(unrelatedDirectory.toFile())
                        .redirectErrorStream(true).start();
                if (!process.waitFor(30, TimeUnit.SECONDS)) {
                    process.destroyForcibly();
                    throw new AssertionError(scenario + ": timed out");
                }
                System.out.print(new String(process.getInputStream().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8));
                check(process.exitValue() == 0, scenario + ": process completed successfully");
                if (scenario.equals("override")) {
                    check(!Files.exists(project.resolve(ObjectPlus.EXTENT_NAME)), "override: default location untouched");
                }
            }
            check(Files.readString(unrelatedDirectory.resolve(ObjectPlus.EXTENT_NAME)).equals("This file must not be used."),
                    "unrelated working directory data untouched");
            System.out.println("All 7 startup regression scenarios passed.");
        } finally {
            try (var paths = Files.walk(temporary)) {
                for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) {
                    Files.deleteIfExists(path);
                }
            }
        }
    }
}
