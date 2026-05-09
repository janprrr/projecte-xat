package org.projectexat.client.ui.controllers;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.projectexat.client.Client;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ChatController implements Client.MissatgeListener {

    @FXML private ListView<MessageItem> lvMessages;
    @FXML private ListView<String> lvUsers;
    @FXML private TextArea taInput;
    @FXML private DatePicker dpHistory;
    @FXML private Button btnSend;
    @FXML private Button btnDisconnect;
    @FXML private Button btnHistory;

    private Client client;
    private String ownUsername;
    private boolean collectingHistory = false;
    private List<MessageItem> historyItems = new ArrayList<>();

    // ── Classe per als ítems de missatge ─────────────────
    private static class MessageItem {
        String sender;
        String text;
        boolean locallyAdded;   // true si l'hem afegit des del client en prémer Enviar

        MessageItem(String sender, String text, boolean locallyAdded) {
            this.sender = sender;
            this.text = text;
            this.locallyAdded = locallyAdded;
        }
    }

    // ── setters cridats des de LoginController ───────────
    public void setClient(Client client) {
        this.client = client;
        client.setMissatgeListener(this);
    }

    public void setUsername(String username) {
        this.ownUsername = username;
    }

    // ── Inicialització de la UI (cal username) ──────────
    public void initializeUI() {
        lvMessages.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldVal, newVal) -> {
                    if (newVal != null)
                        Platform.runLater(() -> lvMessages.getSelectionModel().clearSelection());
                });

        lvMessages.setCellFactory(param -> new ListCell<MessageItem>() {
            private final HBox container = new HBox();
            private final Label label = new Label();

            {
                container.getChildren().add(label);
                label.setWrapText(true);
            }

            @Override
            protected void updateItem(MessageItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                    label.maxWidthProperty().unbind();
                } else {
                    label.setText(item.text);
                    label.maxWidthProperty().unbind();
                    label.maxWidthProperty().bind(getListView().widthProperty().multiply(0.75));

                    if ("SISTEMA".equals(item.sender)) {
                        label.setStyle(
                                "-fx-text-fill: #FFD700; -fx-font-style: italic; " +
                                        "-fx-background-color: rgba(255, 215, 0, 0.1); " +
                                        "-fx-background-radius: 8; -fx-padding: 6 10;"
                        );
                        container.setAlignment(Pos.CENTER);
                    } else if (item.sender.equals(ownUsername)) {
                        label.setStyle(
                                "-fx-text-fill: white; " +
                                        "-fx-background-color: #00d4aa; " +
                                        "-fx-background-radius: 15 15 5 15; " +
                                        "-fx-padding: 10 15;"
                        );
                        container.setAlignment(Pos.CENTER_RIGHT);
                    } else {
                        label.setStyle(
                                "-fx-text-fill: #E0E0E0; " +
                                        "-fx-background-color: rgba(255, 255, 255, 0.1); " +
                                        "-fx-background-radius: 15 15 15 5; " +
                                        "-fx-padding: 10 15;"
                        );
                        container.setAlignment(Pos.CENTER_LEFT);
                    }
                    setGraphic(container);
                }
            }
        });
    }

    // ── Recepció de missatges del servidor ──────────────
    @Override
    public void onMissatge(String missatge) {
        Platform.runLater(() -> processarMissatgeRebut(missatge));
    }

    private void processarMissatgeRebut(String missatge) {
        if (missatge == null) return;

        // Mode acumulació d'historial
        if (collectingHistory) {
            if (missatge.equals("HISTORY:FI")) {
                collectingHistory = false;
                mostrarFinestraHistorial(new ArrayList<>(historyItems));
                historyItems.clear();
            } else if (missatge.equals("HISTORY:ERROR")) {
                collectingHistory = false;
                historyItems.clear();
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText(null);
                    alert.setContentText("No s'ha pogut carregar l'historial.");
                    alert.showAndWait();
                });
            } else if (missatge.startsWith("HISTORY:")) {
                // Cada línia d'historial ve com "HISTORY:[timestamp] usuari: text"
                String line = missatge.substring(8);
                // Parsejar [timestamp] usuari: text
                int tancament = line.indexOf("] ");
                if (tancament != -1) {
                    String rest = line.substring(tancament + 2); // "usuari: text"
                    int dosPunts = rest.indexOf(": ");
                    if (dosPunts != -1) {
                        String sender = rest.substring(0, dosPunts);
                        String text = rest.substring(dosPunts + 2);
                        historyItems.add(new MessageItem(sender, text, false));
                    } else {
                        historyItems.add(new MessageItem("", rest, false));
                    }
                } else {
                    historyItems.add(new MessageItem("", line, false));
                }
            }
            return;
        }

        // Començament d'historial
        if (missatge.startsWith("HISTORY:INICI")) {
            collectingHistory = true;
            historyItems.clear();
            return;
        }

        // Missatge normal (MSG: ...)
        if (missatge.startsWith("MSG:")) {
            String content = missatge.substring(4); // "[timestamp] usuari: text"
            // Parsejar per extreure usuari i text
            int tancament = content.indexOf("] ");
            if (tancament != -1) {
                String rest = content.substring(tancament + 2); // "usuari: text"
                int dosPunts = rest.indexOf(": ");
                String sender, messageText;
                if (dosPunts != -1) {
                    sender = rest.substring(0, dosPunts);
                    messageText = rest.substring(dosPunts + 2);
                } else {
                    sender = "";
                    messageText = rest;
                }

                ObservableList<MessageItem> items = lvMessages.getItems();

                // Si és un missatge propi, evitar duplicar-lo si ja l'hem afegit localment
                if (sender.equals(ownUsername)) {
                    if (!items.isEmpty()) {
                        MessageItem last = items.get(items.size() - 1);
                        if (last.sender.equals(ownUsername)
                                && last.text.equals(messageText)
                                && last.locallyAdded) {
                            // Confirmació del servidor: marcar com a no local
                            last.locallyAdded = false;
                            return;
                        }
                    }
                }

                // Afegir missatge normal
                items.add(new MessageItem(sender, messageText, false));
                lvMessages.scrollTo(items.size() - 1);
            }
            return;
        }

        // Missatges de sistema
        if (missatge.startsWith("SISTEMA:")) {
            String text = missatge.substring(8);
            lvMessages.getItems().add(new MessageItem("SISTEMA", text, false));
            lvMessages.scrollTo(lvMessages.getItems().size() - 1);
            return;
        }

        // Actualització de la llista d'usuaris
        if (missatge.startsWith("USUARIS:")) {
            String[] usuaris = missatge.substring(8).split(",");
            lvUsers.getItems().clear();
            for (String u : usuaris) {
                if (!u.isEmpty()) lvUsers.getItems().add(u);
            }
            return;
        }

        // Qualsevol altra cosa
        lvMessages.getItems().add(new MessageItem("", missatge, false));
        lvMessages.scrollTo(lvMessages.getItems().size() - 1);
    }

    @Override
    public void onDesconnexio() {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Connexió perduda");
            alert.setHeaderText(null);
            alert.setContentText("S'ha perdut la connexió amb el servidor.");
            alert.showAndWait();
            tornarAlLogin();
        });
    }

    @FXML
    private void handleSend() {
        String text = taInput.getText().trim();
        if (!text.isEmpty() && client != null) {
            // Afegir immediatament com a local
            MessageItem myMsg = new MessageItem(ownUsername, text, true);
            lvMessages.getItems().add(myMsg);
            lvMessages.scrollTo(lvMessages.getItems().size() - 1);

            client.enviarMissatge(text);
            taInput.clear();
        }
    }

    @FXML
    private void handleDisconnect() {
        if (client != null) {
            client.desconnectar();
            tornarAlLogin();
        }
    }

    @FXML
    private void handleHistory() {
        LocalDate date = dpHistory.getValue();
        if (date == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Data no seleccionada");
            alert.setHeaderText(null);
            alert.setContentText("Selecciona una data per consultar l'historial.");
            alert.showAndWait();
            return;
        }
        String dataStr = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        client.demanarHistorial(dataStr);
    }

    // ── Torna al login conservant la mida actual ───────
    private void tornarAlLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/LoginView.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) lvMessages.getScene().getWindow();
            double ample = stage.getWidth();
            double alt = stage.getHeight();
            stage.setTitle("Projecte Xat - Login");
            stage.setScene(new Scene(root, ample, alt));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
private void mostrarFinestraHistorial(List<MessageItem> items) {
    Platform.runLater(() -> {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Historial de missatges");

        ListView<MessageItem> listView = new ListView<>();
        listView.getItems().addAll(items);
        listView.getStyleClass().add("history-list");

        listView.setCellFactory(param -> new ListCell<MessageItem>() {
            private final HBox container = new HBox();
            private final VBox bubble = new VBox(2);
            private final Label senderLabel = new Label();
            private final Label msgLabel = new Label();

            {
                senderLabel.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-opacity: 0.7;");
                msgLabel.setWrapText(true);
                bubble.getChildren().addAll(senderLabel, msgLabel);
                container.getChildren().add(bubble);
                bubble.maxWidthProperty().bind(listView.widthProperty().multiply(0.75));
            }

            @Override
            protected void updateItem(MessageItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    return;
                }

                msgLabel.setText(item.text);

                if ("SISTEMA".equals(item.sender)) {
                    senderLabel.setText("⚙ Sistema");
                    senderLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #FFD700; -fx-opacity: 0.8;");
                    msgLabel.setStyle("-fx-text-fill: #FFD700; -fx-font-style: italic; " +
                            "-fx-background-color: rgba(255,215,0,0.1); -fx-background-radius: 8; -fx-padding: 6 10;");
                    container.setAlignment(Pos.CENTER);
                } else if (item.sender.equals(ownUsername)) {
                    senderLabel.setText("Tu");
                    senderLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #004d3d; -fx-font-weight: bold;");
                    msgLabel.setStyle("-fx-text-fill: white; -fx-background-color: #00d4aa; " +
                            "-fx-background-radius: 15 15 5 15; -fx-padding: 10 15;");
                    container.setAlignment(Pos.CENTER_RIGHT);
                } else {
                    senderLabel.setText(item.sender);
                    senderLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #00d4aa; -fx-font-weight: bold;");
                    msgLabel.setStyle("-fx-text-fill: #E0E0E0; -fx-background-color: rgba(255,255,255,0.1); " +
                            "-fx-background-radius: 15 15 15 5; -fx-padding: 10 15;");
                    container.setAlignment(Pos.CENTER_LEFT);
                }
                setGraphic(container);
            }
        });

        VBox vbox = new VBox(listView);
        VBox.setVgrow(listView, javafx.scene.layout.Priority.ALWAYS);
        vbox.setPrefSize(500, 420);

        Scene scene = new Scene(vbox);
        scene.getStylesheets().add(
                getClass().getResource("/css/history.css").toExternalForm()
        );

        stage.setScene(scene);
        stage.show();
    });
}
}