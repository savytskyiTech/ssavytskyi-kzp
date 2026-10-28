package ua.lpnu.kzp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class MainTest {
    @Test
    void startsWithUkrainianMessage() throws IOException, InterruptedException {
        String output = run(0);
        assertTrue(output.contains("Ветклініка"), output);
        assertTrue(output.contains("ще не реалізована"), output);
    }

    @Test
    void showsAvailableOptions() throws IOException, InterruptedException {
        String output = run(0, "--help");
        assertTrue(output.contains("--help"), output);
        assertTrue(output.contains("--version"), output);
    }

    @Test
    void showsFilteredVersionWithBuildNumber() throws IOException, InterruptedException {
        String output = run(0, "--version").strip();
        assertTrue(output.matches("1\\.0\\.0\\+(local|[0-9]+)"), output);
        assertFalse(output.contains("${"), output);
    }

    @Test
    void rejectsUnknownOption() throws IOException, InterruptedException {
        assertTrue(run(1, "--unknown").contains("Невідомий параметр"));
    }

    /** Запускає справжній CLI в окремій JVM із явним UTF-8 та обмеженням часу. */
    private static String run(int expectedExitCode, String... args)
            throws IOException, InterruptedException {
        String executable = System.getProperty("os.name").startsWith("Windows")
                ? "java.exe" : "java";
        List<String> command = new ArrayList<>(List.of(
                Path.of(System.getProperty("java.home"), "bin", executable).toString(),
                "-Dfile.encoding=UTF-8", "-Dstdout.encoding=UTF-8", "-Dstderr.encoding=UTF-8",
                "-cp", System.getProperty("java.class.path"), Main.class.getName()));
        command.addAll(Arrays.asList(args));
        Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
        boolean finished = process.waitFor(20, TimeUnit.SECONDS);
        if (!finished) {
            process.destroyForcibly();
        }
        assertTrue(finished, "CLI не завершив роботу за 20 секунд.");
        String output;
        try (var stream = process.getInputStream()) {
            output = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
        assertEquals(expectedExitCode, process.exitValue(), output);
        return output;
    }
}
