package br.dev.ampliar.caipora.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Runs a deploy script as a child process: the script is written to a temporary file and executed with the
 * configured interpreter. Values about the deploy are passed only as environment variables, never spliced into the
 * script or the command line. Standard output and error are combined and captured (only the most recent part is kept
 * when it grows beyond the limit). No database access here.
 */
public class DeployScriptRunner {

    private static final Logger log = LoggerFactory.getLogger(DeployScriptRunner.class);

    /**
     * Environment variables of the Caipora process that must not leak to scripts (their output is shown in the UI).
     */
    private static final Set<String> HIDDEN_VARIABLES = Set.of("ADMIN_INITIAL_PASSWORD", "REMEMBER_ME_KEY");
    private static final List<String> HIDDEN_PREFIXES = List.of("DATABASE_", "SPRING_");
    private static final Duration POLL_INTERVAL = Duration.ofSeconds(1);
    private static final Duration READER_GRACE = Duration.ofSeconds(5);

    private final List<String> interpreter;
    private final String scriptExtension;
    private final Path workingDirectory;
    private final int maxOutputChars;

    /**
     * Called about once per second while the script runs.
     */
    @FunctionalInterface
    public interface ProgressListener {
        /**
         * @param outputSoFar output captured until now
         * @return true to stop the script (cancelled)
         */
        boolean onProgress(String outputSoFar);
    }

    /**
     * @param exitCode null when the script could not be started
     */
    public record Result(Integer exitCode, String output, boolean timedOut, boolean cancelled) {

        public boolean succeeded() {
            return exitCode != null && exitCode == 0 && !timedOut && !cancelled;
        }
    }

    public DeployScriptRunner(final List<String> interpreter, final String scriptExtension,
                              final Path workingDirectory, final int maxOutputChars) {
        this.interpreter = List.copyOf(interpreter);
        this.scriptExtension = scriptExtension.startsWith(".") ? scriptExtension : "." + scriptExtension;
        this.workingDirectory = workingDirectory;
        this.maxOutputChars = maxOutputChars;
    }

    public Result run(final String script, final Map<String, String> variables, final Duration timeout,
                      final ProgressListener listener) {
        final OutputBuffer output = new OutputBuffer(maxOutputChars);
        final long started = System.nanoTime();
        Path scriptFile = null;
        Process process = null;
        try {
            scriptFile = writeScript(script);
            final List<String> command = new ArrayList<>(interpreter);
            command.add(scriptFile.toString());
            output.append("$ " + String.join(" ", command) + "\n");

            Files.createDirectories(workingDirectory);
            final ProcessBuilder builder = new ProcessBuilder(command)
                    .directory(workingDirectory.toFile())
                    .redirectErrorStream(true);
            final Map<String, String> environment = builder.environment();
            environment.keySet().removeIf(DeployScriptRunner::isHidden);
            environment.putAll(variables);

            process = builder.start();
            process.getOutputStream().close(); // no input: a script waiting for it gets EOF instead of hanging
            final Thread reader = Thread.ofVirtual().name("deploy-output-" + process.pid())
                    .start(readerTask(process, output));

            boolean timedOut = false;
            boolean cancelled = false;
            while (!process.waitFor(POLL_INTERVAL.toMillis(), TimeUnit.MILLISECONDS)) {
                if (Duration.ofNanos(System.nanoTime() - started).compareTo(timeout) > 0) {
                    timedOut = true;
                } else if (listener != null && listener.onProgress(output.snapshot())) {
                    cancelled = true;
                }
                if (timedOut || cancelled) {
                    kill(process);
                    process.waitFor(10, TimeUnit.SECONDS);
                    break;
                }
            }
            // a background process started by the script may keep the pipe open: don't wait for it forever
            reader.join(READER_GRACE);
            final Integer exitCode = process.isAlive() ? null : process.exitValue();
            final String elapsed = formatElapsed(Duration.ofNanos(System.nanoTime() - started));
            if (timedOut) {
                output.append("\n[Caipora] Timed out after " + elapsed + ": the script was stopped.\n");
            } else if (cancelled) {
                output.append("\n[Caipora] Cancelled after " + elapsed + ": the script was stopped.\n");
            } else {
                output.append("\n[Caipora] Exit code " + exitCode + " after " + elapsed + ".\n");
            }
            return new Result(exitCode, output.snapshot(), timedOut, cancelled);
        } catch (InterruptedException e) {
            // e.g. Caipora shutting down: never leave the script running unattended
            if (process != null) {
                kill(process);
            }
            Thread.currentThread().interrupt();
            output.append("\n[Caipora] Interrupted: the script was stopped.\n");
            return new Result(null, output.snapshot(), false, true);
        } catch (IOException | RuntimeException e) {
            if (process != null) {
                kill(process);
            }
            log.warn("Could not run the deploy script", e);
            output.append("\n[Caipora] Could not run the script: " + e.getMessage() + "\n");
            return new Result(null, output.snapshot(), false, false);
        } finally {
            if (scriptFile != null) {
                try {
                    Files.deleteIfExists(scriptFile);
                } catch (IOException e) {
                    log.warn("Could not delete the temporary script {}", scriptFile, e);
                }
            }
        }
    }

    static boolean isHidden(final String name) {
        final String upper = name.toUpperCase(Locale.ROOT);
        return HIDDEN_VARIABLES.contains(upper) || HIDDEN_PREFIXES.stream().anyMatch(upper::startsWith);
    }

    private Path writeScript(final String script) throws IOException {
        Path file;
        try {
            file = Files.createTempFile("caipora-deploy-", scriptExtension,
                    PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rwx------")));
        } catch (UnsupportedOperationException e) {
            file = Files.createTempFile("caipora-deploy-", scriptExtension); // Windows: no POSIX permissions
        }
        String content = script.replace("\r\n", "\n");
        final String extension = scriptExtension.toLowerCase(Locale.ROOT);
        if (extension.equals(".cmd") || extension.equals(".bat")) {
            content = content.replace("\n", "\r\n");
        }
        // PowerShell reads BOM-less files with the legacy code page, a UTF-8 BOM keeps non-ASCII characters intact
        final byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
        if (extension.equals(".ps1")) {
            final byte[] withBom = new byte[bytes.length + 3];
            withBom[0] = (byte) 0xEF;
            withBom[1] = (byte) 0xBB;
            withBom[2] = (byte) 0xBF;
            System.arraycopy(bytes, 0, withBom, 3, bytes.length);
            Files.write(file, withBom);
        } else {
            Files.write(file, bytes);
        }
        return file;
    }

    private static Runnable readerTask(final Process process, final OutputBuffer output) {
        return () -> readOutput(process, output);
    }

    private static void readOutput(final Process process, final OutputBuffer output) {
        final String nativeEncoding = System.getProperty("native.encoding");
        final Charset charset = nativeEncoding != null && Charset.isSupported(nativeEncoding)
                ? Charset.forName(nativeEncoding) : Charset.defaultCharset();
        try (Reader reader = new InputStreamReader(process.getInputStream(), charset)) {
            final char[] chunk = new char[4096];
            int read;
            while ((read = reader.read(chunk)) != -1) {
                output.append(new String(chunk, 0, read));
            }
        } catch (IOException e) {
            // the stream is closed when the process is killed
            log.debug("Output of process {} closed", process.pid(), e);
        }
    }

    private static void kill(final Process process) {
        process.descendants().forEach(ProcessHandle::destroyForcibly);
        process.destroyForcibly();
    }

    static String formatElapsed(final Duration duration) {
        final long seconds = duration.toSeconds();
        if (seconds < 60) {
            return String.format(Locale.ROOT, "%.1f s", duration.toMillis() / 1000.0);
        }
        return String.format(Locale.ROOT, "%d min %02d s", seconds / 60, seconds % 60);
    }

    /**
     * Thread-safe text buffer that keeps only the most recent {@code max} characters.
     */
    static final class OutputBuffer {

        private static final String TRUNCATED = "[Caipora] ... earlier output truncated ...\n";

        private final int max;
        private final StringBuilder text = new StringBuilder();
        private boolean truncated;

        OutputBuffer(final int max) {
            this.max = Math.max(max, 1024);
        }

        synchronized void append(final String value) {
            // PostgreSQL text columns cannot hold NUL characters
            text.append(value.replace("\u0000", ""));
            if (text.length() > max) {
                text.delete(0, text.length() - max);
                truncated = true;
            }
        }

        synchronized String snapshot() {
            return truncated ? TRUNCATED + text : text.toString();
        }
    }
}
