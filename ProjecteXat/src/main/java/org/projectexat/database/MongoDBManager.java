package org.projectexat.database;

import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import org.projectexat.common.Message;
import org.projectexat.common.User;
import org.bson.Document;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MongoDBManager implements DatabaseManager {

    private final MongoClient mongoClient;
    private final MongoDatabase database;
    private final MongoCollection<Document> usersCollection;
    private final MongoCollection<Document> messagesCollection;

    public MongoDBManager(String connectionString, String dbName) {
        this.mongoClient = MongoClients.create(connectionString);
        this.database = mongoClient.getDatabase(dbName);
        this.usersCollection = database.getCollection("users");
        this.messagesCollection = database.getCollection("messages");
    }

    // ── USUARIS ──────────────────────────────────────────

    @Override
    public void insertUser(User user) {
        Document doc = new Document()
                .append("username", user.getUsername())
                .append("passwordHash", user.getPasswordHash());
        usersCollection.insertOne(doc);
    }

    @Override
    public User findUser(String username, String passwordHash) {
        Document doc = usersCollection.find(
                Filters.and(
                        Filters.eq("username", username),
                        Filters.eq("passwordHash", passwordHash)
                )
        ).first();

        if (doc == null) return null;
        return new User(doc.getString("username"), doc.getString("passwordHash"));
    }

    // ── MISSATGES ─────────────────────────────────────────

    @Override
    public void insertMessage(Message message) {
        Date date = Date.from(message.getTimestamp()
                .atZone(ZoneId.systemDefault()).toInstant());

        Document doc = new Document()
                .append("username", message.getUsername())
                .append("text", message.getText())
                .append("timestamp", date);
        messagesCollection.insertOne(doc);
    }

    @Override
    public List<Message> getAllMessages() {
        List<Message> messages = new ArrayList<>();
        for (Document doc : messagesCollection.find()) {
            messages.add(docToMessage(doc));
        }
        return messages;
    }

    @Override
    public List<Message> getMessagesByDateRange(LocalDateTime from, LocalDateTime to) {
        Date dateFrom = Date.from(from.atZone(ZoneId.systemDefault()).toInstant());
        Date dateTo = Date.from(to.atZone(ZoneId.systemDefault()).toInstant());

        List<Message> messages = new ArrayList<>();
        for (Document doc : messagesCollection.find(
                Filters.and(
                        Filters.gte("timestamp", dateFrom),
                        Filters.lte("timestamp", dateTo)
                )
        )) {
            messages.add(docToMessage(doc));
        }
        return messages;
    }

    // ── UTILITAT ──────────────────────────────────────────

    private Message docToMessage(Document doc) {
        Date date = doc.getDate("timestamp");
        LocalDateTime timestamp = date.toInstant()
                .atZone(ZoneId.systemDefault()).toLocalDateTime();
        return new Message(doc.getString("username"), doc.getString("text"), timestamp);
    }

    public void close() {
        mongoClient.close();
    }
}