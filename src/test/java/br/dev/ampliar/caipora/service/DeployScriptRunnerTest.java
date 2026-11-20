package br.dev.ampliar.caipora.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@EnabledOnOs({OS.LINUX, OS.MAC})
class DeployScriptRunnerTest {

    @TempDir
    Path workDir;

    private DeployScriptRunner runner(final int maxOutput) {
        return new DeployScriptRunner(List.of("bash"), ".sh", workDir, maxOutput);
    }

    @Test
    void passesTheDeployAsEnvironmentVariablesAndCapturesOutput() {
        final var result = runner(10_000).run("echo \"pkg=$CAIPORA_PACKAGE\"\necho oops >&2\n",
                Map.of("CAIPORA_PACKAGE", "/srv/app 1.zip; rm -rf /"), Duration.ofSeconds(30), null);

        assertTrue(result.succeeded(), result.output());
        assertEquals(0, result.exitCode());
        assertTrue(result.output().contains("pkg=/srv/app 1.zip; rm -rf /"), result.output());
        assertTrue(result.output().contains("oops"), result.output());
    }

    @Test
    void nonZeroExitCodeFails() {
        final var result = runner(10_000).run("exit 3", Map.of(), Duration.ofSeconds(30), null);

        assertFalse(result.succeeded());
        assertEquals(3, result.exitCode());
    }

    @Test
    void windowsLineEndingsAreNormalized() {
        final var result = runner(10_000).run("echo one\r\necho two\r\n", Map.of(), Duration.ofSeconds(30), null);

        assertTrue(result.succeeded(), result.output());
        assertTrue(result.output().contains("one\ntwo"), result.output());
    }

    @Test
    void timesOut() {
        final var result = runner(10_000).run("sleep 30", Map.of(), Duration.ofSeconds(2), null);

        assertTrue(result.timedOut());
        assertFalse(result.succeeded());
    }

    @Test
    void canBeCancelledFromTheProgressListener() {
        final AtomicInteger calls = new AtomicInteger();
        final var result = runner(10_000).run("echo started; sleep 30", Map.of(), Duration.ofSeconds(60),
                output -> calls.incrementAndGet() >= 2);

        assertTrue(result.cancelled());
        assertFalse(result.succeeded());
        assertTrue(result.output().contains("started"), result.output());
    }

    @Test
    void keepsOnlyTheEndOfALongOutput() {
        final var result = runner(2_000).run("for i in $(seq 1 2000); do echo \"line $i\"; done", Map.of(),
                Duration.ofSeconds(30), null);

        assertTrue(result.succeeded());
        assertTrue(result.output().contains("earlier output truncated"));
        assertTrue(result.output().contains("line 2000"));
        assertFalse(result.output().contains("line 1\n"));
    }

    @Test
    void hidesSecretsOfTheServer() {
        assertTrue(DeployScriptRunner.isHidden("DATABASE_PASSWORD"));
        assertTrue(DeployScriptRunner.isHidden("ADMIN_INITIAL_PASSWORD"));
        assertTrue(DeployScriptRunner.isHidden("remember_me_key"));
        assertTrue(DeployScriptRunner.isHidden("SPRING_DATASOURCE_PASSWORD"));
        assertFalse(DeployScriptRunner.isHidden("PATH"));
    }

    @Test
    void reportsAnInterpreterThatCannotStart() {
        final var result = new DeployScriptRunner(List.of("/nonexistent/interpreter"), ".sh", workDir, 10_000)
                .run("echo hi", Map.of(), Duration.ofSeconds(5), null);

        assertNull(result.exitCode());
        assertFalse(result.succeeded());
        assertTrue(result.output().contains("Could not run the script"), result.output());
    }
}
