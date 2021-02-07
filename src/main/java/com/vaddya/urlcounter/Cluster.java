package com.vaddya.urlcounter;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import com.vaddya.urlcounter.local.HitCounter;
import com.vaddya.urlcounter.local.InMemoryHitCounter;
import com.vaddya.urlcounter.topology.ConsistentHashingTopology;
import com.vaddya.urlcounter.topology.Topology;

import one.nio.http.HttpServer;

public class Cluster {
    private static final int[] PORTS = {8080, 8081, 8082};

    public static void main(String[] args) throws IOException {
        Set<String> nodes = new HashSet<>(3);
        for (final int port : PORTS) {
            nodes.add("http://localhost:" + port);
        }

        // Start nodes
        for (int port : PORTS) {
            Topology topology = new ConsistentHashingTopology(nodes, "http://localhost:" + port);
            HitCounter counter = new InMemoryHitCounter();
            HttpServer server = new HitCounterServer(port, topology, counter);
            server.start();
        }
    }
}
