package community;

// MemberRole.java
// Typ wyliczeniowy używany jako dyskryminator w dziedziczeniu łącznym (overlapping).
// Jeden CommunityMember może mieć dowolną kombinację ról.

public enum MemberRole {
    // Zwykły gracz – interesują nas m.in. liczba gier i liczba rozegranych godzin.
    PLAYER,

    // Twórca modyfikacji – liczba opublikowanych modów.
    MODDER,

    // Recenzent – liczba recenzji i średnia ocena wystawiana grom.
    REVIEWER
}
