package org.projectexat.server;

import org.projectexat.common.CipherUtil;
import org.projectexat.common.Message;
import org.projectexat.common.User;
import org.projectexat.database.DatabaseManager;

import java.io.*;
import java.net.Socket;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ClientHandler extends Thread {

    private final Socket socket;
    private final List<ClientHandler> clients;
    private final DatabaseManager db;

    private PrintWriter out;
    private BufferedReader in;
    private String username;

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public ClientHandler(Socket socket, List<ClientHandler> clients, DatabaseManager db) {
        this.socket = socket;
        this.clients = clients;
        this.db = db;
    }

    @Override
    public void run() {
        try {
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            if (!processarLogin()) {
                socket.close();
                return;
            }

            enviarUsuarisConnectats();

            Server.broadcast("SISTEMA:" + username + " s'ha connectat.", this);

            String linia;
            while ((linia = in.readLine()) != null) {
                String desxifrat = CipherUtil.desxifrar(linia);
                if (desxifrat.startsWith("MSG:")) {
                    processarMissatge(desxifrat.substring(4));
                } else if (desxifrat.startsWith("HISTORY:")) {
                    processarHistorial(desxifrat.substring(8));
                } else if (desxifrat.equals("DISCONNECT")) {
                    break;
                }
            }

        } catch (IOException e) {
            System.err.println("Connexió perduda amb: " + username);
        } finally {
            desconnectar();
        }
    }

    private boolean processarLogin() throws IOException {
        String linia = in.readLine();
        if (linia == null) return false;

        String desxifrat = CipherUtil.desxifrar(linia);
        if (!desxifrat.startsWith("LOGIN:")) return false;

        String[] parts = desxifrat.split(":");
        if (parts.length != 3) return false;

        String user = parts[1];
        String passHash = parts[2];

        User usuari = db.findUser(user, passHash);
        if (usuari == null) {
            enviarXifrat("LOGIN:ERROR");
            return false;
        }

        this.username = user;
        enviarXifrat("LOGIN:OK");
        System.out.println("Usuari autenticat: " + username);
        return true;
    }

    private void enviarUsuarisConnectats() {
        StringBuilder sb = new StringBuilder("USUARIS:");
        synchronized (clients) {
            for (ClientHandler c : clients) {
                if (c.username != null && c != this) {
                    sb.append(c.username).append(",");
                }
            }
        }
        enviarXifrat(sb.toString());
    }

    private void processarMissatge(String text) {
        LocalDateTime ara = LocalDateTime.now();
        String missatgeFormatat = "[" + ara.format(FORMATTER) + "] " + username + ": " + text;

        db.insertMessage(new Message(username, text, ara));

        Server.broadcastATots("MSG:" + missatgeFormatat);
        System.out.println(missatgeFormatat);
    }

    private void processarHistorial(String data) {
        try {
            LocalDate dia = LocalDate.parse(data, DATE_FORMATTER);
            LocalDateTime inici = dia.atStartOfDay();
            LocalDateTime fi = dia.atTime(23, 59, 59);

            List<Message> historial = db.getMessagesByDateRange(inici, fi);
            enviarXifrat("HISTORY:INICI");
            for (Message m : historial) {
                enviarXifrat("HISTORY:" + "[" + m.getTimestamp().format(FORMATTER) + "] "
                        + m.getUsername() + ": " + m.getText());
            }
            enviarXifrat("HISTORY:FI");
        } catch (Exception e) {
            enviarXifrat("HISTORY:ERROR");
        }
    }

    private void desconnectar() {
        clients.remove(this);
        if (username != null) {
            Server.broadcast("SISTEMA:" + username + " s'ha desconnectat.", this);
        }
        try {
            socket.close();
        } catch (IOException e) {
            System.err.println("Error tancant socket: " + e.getMessage());
        }
    }

    public void enviarMissatge(String missatge) {
        enviarXifrat(missatge);
    }

    private void enviarXifrat(String text) {
        out.println(CipherUtil.xifrar(text));
    }

    public String getUsername() {
        return username;
    }
}