package org.projectexat.server;


import org.projectexat.database.DatabaseManager;
import org.projectexat.database.MongoDBManager;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Server {

    private static final int PORT = 12345;
    private static final List<ClientHandler> clients =
            Collections.synchronizedList(new ArrayList<>());
    private static DatabaseManager db;

    public static void main(String[] args) {
        db = new MongoDBManager("mongodb://user1:H6sY2fG8nK4t@51.91.56.13:27017", "user1db");
        System.out.println("Servidor iniciat al port " + PORT);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Nova connexió: " + socket.getInetAddress());
                ClientHandler handler = new ClientHandler(socket, clients, db);
                clients.add(handler);
                new Thread(handler).start();
            }
        } catch (IOException e) {
            System.err.println("Error al servidor: " + e.getMessage());
        }
    }

    public static void broadcast(String missatge, ClientHandler origen) {
        synchronized (clients) {
            for (ClientHandler client : clients) {
                if (client != origen) {
                    client.enviarMissatge(missatge);
                }
            }
        }
    }

    public static void broadcastATots(String missatge) {
        synchronized (clients) {
            for (ClientHandler client : clients) {
                client.enviarMissatge(missatge);
            }
        }
    }

    public static List<ClientHandler> getClients() {
        return clients;
    }
}
