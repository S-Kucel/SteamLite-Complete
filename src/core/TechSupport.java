package core;

public class TechSupport extends User {
    public TechSupport(String nick, String username, String password, int age) {
        super(nick, username, password, age);
    }

    public TechSupport(String username, String password, int age) {
        super(username, password, age);
    }

    @Override
    public String toString() {
        return "TechSupport{" +
                "nick='" + getNick() + '\'' +
                ", username='" + getUsername() + '\'' +
                ", age=" + getAge() +
                '}';
    }
}
