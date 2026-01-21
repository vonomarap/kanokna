package com.kanokna.test.grpc;

import java.io.IOException;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import io.grpc.BindableService;
import io.grpc.ManagedChannel;
import io.grpc.Server;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;

/**
 * Utility for building in-process gRPC servers and channels for tests.
 */
public final class GrpcTestChannelBuilder {
    private static final Duration SHUTDOWN_TIMEOUT = Duration.ofSeconds(5);

    private GrpcTestChannelBuilder() {
    }

    public static GrpcTestServer startInProcessServer(BindableService... services) {
        Objects.requireNonNull(services, "services");
        if (services.length == 0) {
            throw new IllegalArgumentException("At least one service must be provided");
        }

        String serverName = InProcessServerBuilder.generateName();
        InProcessServerBuilder builder = InProcessServerBuilder.forName(serverName)
            .directExecutor();
        for (BindableService service : services) {
            builder.addService(service);
        }

        try {
            Server server = builder.build().start();
            ManagedChannel channel = InProcessChannelBuilder.forName(serverName)
                .directExecutor()
                .build();
            return new GrpcTestServer(server, channel);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to start in-process gRPC server", ex);
        }
    }

    public record GrpcTestServer(Server server, ManagedChannel channel) implements AutoCloseable {
        public GrpcTestServer {
            Objects.requireNonNull(server, "server");
            Objects.requireNonNull(channel, "channel");
        }

        @Override
        public void close() {
            channel.shutdownNow();
            server.shutdownNow();
            awaitTermination();
        }

        private void awaitTermination() {
            try {
                channel.awaitTermination(SHUTDOWN_TIMEOUT.toSeconds(), TimeUnit.SECONDS);
                server.awaitTermination(SHUTDOWN_TIMEOUT.toSeconds(), TimeUnit.SECONDS);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
