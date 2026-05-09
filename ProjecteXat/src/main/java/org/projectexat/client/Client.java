package org.projectexat.client;

import org.projectexat.common.CipherUtil;
import org.projectexat.common.HashUtil;

import java.io.*;
import java.net.Socket;

public class Client {

    private static final String HOST = "localhost";
    private static final int PORT = 12345;

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    private volatile MissatgeListener listener;
    public interface MissatgeListener {
        void onMissatge(String missatge);
        void onDesconnexio();
    }

    public Client(MissatgeListener listener) {
        this.listener = listener;
    }

    public boolean connectar(String username, String password) {
        try {
            socket = new Socket(HOST, PORT);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Hash de la contrasenya abans d'enviar
            String passwordHash = HashUtil.sha256(password);

            // Login xifrat
            String loginMsg = CipherUtil.xifrar("LOGIN:" + username + ":" + passwordHash);
            out.println(loginMsg);

            String resposta = CipherUtil.desxifrar(in.readLine());
            if (!"LOGIN:OK".equals(resposta)) return false;

            // Fil d'escolta
            new Thread(this::escoltar).start();
            return true;

        } catch (IOException e) {
            System.err.println("Error connectant: " + e.getMessage());
            return false;
        }
    }

    private void escoltar() {
        try {
            String linia;
            while ((linia = in.readLine()) != null) {
                String desxifrat = CipherUtil.desxifrar(linia);
                listener.onMissatge(desxifrat);
            }
        } catch (IOException e) {
            listener.onDesconnexio();
        }
    }

    public void enviarMissatge(String text) {
        out.println(CipherUtil.xifrar("MSG:" + text));
    }

    public void demanarHistorial(String data) {
        out.println(CipherUtil.xifrar("HISTORY:" + data));
    }

    public void desconnectar() {
        out.println(CipherUtil.xifrar("DISCONNECT"));
        try {
            socket.close();
        } catch (IOException e) {
            System.err.println("Error tancant: " + e.getMessage());
        }
    }

    // Afegeix aquest mètode a la classe Client
    public void setMissatgeListener(MissatgeListener listener) {
        this.listener = listener;
    }


}