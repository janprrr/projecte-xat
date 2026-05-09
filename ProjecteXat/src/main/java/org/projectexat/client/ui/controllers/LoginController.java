package org.projectexat.client.ui.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.projectexat.client.Client;

import java.io.IOException;

public class LoginController {

    @FXML
    private TextField txtUsername;

    @FXML
    private PasswordField txtPassword;

    @FXML
    private Label lblError;

    // Aquesta instància del client la passarem al xat un cop connectats
    private Client client;

    @FXML
    public void handleLogin(ActionEvent event) {
        String username = txtUsername.getText();
        String password = txtPassword.getText();

        if (username.isEmpty() || password.isEmpty()) {
            lblError.setText("Siusplau, omple tots els camps.");
            return;
        }

        lblError.setText("Connectant...");

        // Inicialitzem el client amb el listener buit de moment (es configurarà bé a la pantalla de xat)
        client = new Client(new Client.MissatgeListener() {
            @Override
            public void onMissatge(String missatge) {
                // De moment només fem un print, la gestió real serà al ChatController
                System.out.println("Missatge rebut durant login: " + missatge);
            }

            @Override
            public void onDesconnexio() {
                Platform.runLater(() -> lblError.setText("S'ha perdut la connexió."));
            }
        });

        // Intentem connectar. Com que connectar() fa I/O pot bloquejar la UI un moment.
        // En un entorn més avançat es faria en un Task, però per començar està bé així.
        boolean connectat = client.connectar(username, password);

        if (connectat) {
            lblError.setStyle("-fx-text-fill: green;");
            lblError.setText("Connexió exitosa!");

            // TODO: Aquí cridarem una funció per tancar aquesta finestra
            // i obrir la finestra del xat, passant-li l'objecte "client".
            obrirFinestraXat(username);
        } else {
            lblError.setStyle("-fx-text-fill: red;");
            lblError.setText("Credencials incorrectes o servidor inactiu.");
        }
    }

    private void obrirFinestraXat(String username) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/ChatView.fxml"));
            Parent root = loader.load();

            ChatController chatController = loader.getController();
            chatController.setClient(this.client);
            chatController.setUsername(username); 
            chatController.initializeUI();

            Stage stage = (Stage) lblError.getScene().getWindow();

            // Guardem la mida actual (la del login)
            double ample = stage.getWidth();
            double alt = stage.getHeight();

            stage.setTitle("Projecte Xat - Conversa");
            stage.setScene(new Scene(root, ample, alt));   // ← fa servir la mida exacta del login

            // Si vols mantenir uns mínims, pots posar-los així:
            stage.setMinWidth(600);
            stage.setMinHeight(400);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}