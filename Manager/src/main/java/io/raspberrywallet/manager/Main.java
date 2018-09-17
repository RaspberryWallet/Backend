package io.raspberrywallet.manager;

import io.raspberrywallet.server.Server;

public class Main {
    
    
    public static void main(String args[]) {
        System.out.println("123");

        Manager manager = new ExampleMockManager();
        
        Server server = new Server(manager);
        server.start();
    }
    
}
