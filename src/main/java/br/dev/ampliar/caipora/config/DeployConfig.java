package br.dev.ampliar.caipora.config;

import br.dev.ampliar.caipora.service.DeployScriptRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * Execution of deploy scripts: a dedicated, bounded pool (scripts can run for a long time and must not starve the
 * request threads) and the runner configured with the interpreter of the server.
 */
@Configuration
public class DeployConfig {

    @Bean(name = "deployExecutor")
    public ThreadPoolTaskExecutor deployExecutor(@Value("${caipora.deploy.max-concurrent:4}") final int maxConcurrent) {
        final ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(maxConcurrent);
        executor.setMaxPoolSize(maxConcurrent);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("deploy-");
        executor.initialize();
        return executor;
    }

    @Bean
    public DeployScriptRunner deployScriptRunner(@Value("${caipora.deploy.interpreter:bash}") final String interpreter,
                                                 @Value("${caipora.deploy.script-extension:.sh}") final String extension,
                                                 @Value("${caipora.deploy.working-directory:${java.io.tmpdir}}") final String workingDirectory,
                                                 @Value("${caipora.deploy.max-output-chars:1000000}") final int maxOutputChars) {
        final var command = Arrays.stream(interpreter.split(","))
                .map(String::trim)
                .filter(part -> !part.isEmpty())
                .toList();
        if (command.isEmpty()) {
            throw new IllegalStateException("caipora.deploy.interpreter must not be empty");
        }
        return new DeployScriptRunner(command, extension, Paths.get(workingDirectory).toAbsolutePath(), maxOutputChars);
    }

    /**
     * Name of this instance, stored with the deploys it runs (defaults to the host name).
     */
    @Bean(name = "deployNodeId")
    public String deployNodeId(@Value("${caipora.node-id:}") final String nodeId) {
        if (nodeId != null && !nodeId.isBlank()) {
            return nodeId.trim();
        }
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return "caipora";
        }
    }
}
