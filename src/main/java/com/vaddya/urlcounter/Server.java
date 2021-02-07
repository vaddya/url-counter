package com.vaddya.urlcounter;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import com.vaddya.urlcounter.local.InMemoryUrlCounter;
import com.vaddya.urlcounter.local.UrlCounter;
import com.vaddya.urlcounter.topology.Topology;

import one.nio.http.HttpServer;
import one.nio.http.HttpServerConfig;
import one.nio.server.AcceptorConfig;

public class Server {
    private static final int[] PORTS = {8080, 8081, 8082};

    public static void main(String[] args) throws IOException {
        Set<String> topology = new HashSet<>(3);
        for (final int port : PORTS) {
            topology.add("http://localhost:" + port);
        }

        // Start nodes
        for (int port : PORTS) {
            Topology nodes = Topology.consistentHashing(topology, "http://localhost:" + port);
            AcceptorConfig acceptor = new AcceptorConfig();
            acceptor.port = port;
            HttpServerConfig config = new HttpServerConfig();
            config.acceptors = new AcceptorConfig[]{acceptor};
            UrlCounter counter = new InMemoryUrlCounter();
            HttpServer server = new UrlCounterServer(config, nodes, counter);
            server.start();
        }
    }
}
