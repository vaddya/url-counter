package com.vaddya.urlcounter;

import java.io.IOException;
import java.util.Set;

import com.vaddya.urlcounter.local.HitCounter;
import com.vaddya.urlcounter.local.InMemoryHitCounter;
import com.vaddya.urlcounter.topology.ConsistentHashingTopology;
import com.vaddya.urlcounter.topology.Topology;

import one.nio.http.HttpServer;

public class Server {
    private static final int PORT = 8080;

    public static void main(String[] args) throws IOException {
        Set<String> nodes = Set.of("http://localhost:" + PORT);
        Topology topology = new ConsistentHashingTopology(nodes, "http://localhost:" + PORT);
        HitCounter counter = new InMemoryHitCounter();
        HttpServer server = new HitCounterServer(PORT, topology, counter);
        server.start();
    }
}
