package core;

import java.util.HashMap;
import java.util.Map;

public class User extends ObjectPlus4 {
    private static final Map<String, User> USERS_BY_USERNAME = new HashMap<>();

    // atrybut opcjonalny
    private String nick;

    private String username;
    private String password;
    private static int minimalAge = 13;
    private int age;


    //konstruktor z wszystkimi atrybutami
    public User (String nick, String username, String password, int age) {
        try {
            setNick(nick);
            setUsername(username);
            setPassword(password);
            setAge(age);
        } catch (Exception e) {
            removeFromExtent();
            throw new RuntimeException(e);
        }
    }

    //konstruktor bez nicku (przeciążony konstruktor)
    public User (String username, String password, int age) {
        try {
            setUsername(username);
            setPassword(password);
            setAge(age);
        } catch (Exception e) {
            removeFromExtent();
            throw new RuntimeException(e);
        }
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public String getNick() {
        return nick;
    }
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username cannot be blank");
        }
        if (username.equals(this.username)) {
            return;
        }
        User existing = USERS_BY_USERNAME.get(username);
        if (existing != null && existing != this) {
            throw new IllegalArgumentException("Username must be unique");
        }
        if (this.username != null && USERS_BY_USERNAME.get(this.username) == this) {
            USERS_BY_USERNAME.remove(this.username);
        }
        this.username = username;
        USERS_BY_USERNAME.put(username, this);
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        if(password.isBlank() || password.isEmpty())
            throw new IllegalArgumentException("Password cannot be blank");
        if(password.length() < 8)
            throw new IllegalArgumentException("Password must be at least 8 characters long");
        this.password = password;
    }
    public int getAge() {
        return age;
    }
    public void setAge(int age) {
        if (age < minimalAge) {
            throw new IllegalArgumentException("Age must be at least " + minimalAge);
        }
        this.age = age;
    }

    public static void rebuildUserIndex() {
        USERS_BY_USERNAME.clear();
        for (User user : ObjectPlus.getExtentFromClassIncludingSubclasses(User.class)) {
            String username = user.getUsername();
            if (username != null && !username.isBlank()) {
                USERS_BY_USERNAME.put(username, user);
            }
        }
    }

    @Override
    public void removeFromExtent() {
        if (username != null && USERS_BY_USERNAME.get(username) == this) {
            USERS_BY_USERNAME.remove(username);
        }
        super.removeFromExtent();
    }
}
