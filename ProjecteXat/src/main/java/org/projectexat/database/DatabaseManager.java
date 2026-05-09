package org.projectexat.database;


import org.projectexat.common.Message;
import org.projectexat.common.User;   // canviat

import java.time.LocalDateTime;
import java.util.List;

public interface DatabaseManager {
    // Usuaris
    void insertUser(User user);
    User findUser(String username, String passwordHash);

    // Missatges
    void insertMessage(Message message);
    List<Message> getAllMessages();
    List<Message> getMessagesByDateRange(LocalDateTime from, LocalDateTime to);
}