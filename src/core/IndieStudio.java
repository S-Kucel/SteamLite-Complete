package core;

/**
 * Dziedziczenie rozłączne (disjoint) – podklasa Studio:
 * niezależne studio (nie należy do dużej korporacji).
 *
 * Każde IndieStudio jest szczególnym przypadkiem Studio.
 * Obiekt NIE może być jednocześnie IndieStudio i CorporationStudio.
 */
public class IndieStudio extends Studio {

    // prosta cecha biznesowa tej podklasy – wielkość zespołu
    private int teamSize;

    public IndieStudio(String name, String country, int teamSize) {
        // niezależne studio -> isIndependent = true
        super(name, country, true);

        if (teamSize <= 0) {
            throw new IllegalArgumentException("Team size must be positive.");
        }
        this.teamSize = teamSize;
    }

    public int getTeamSize() {
        return teamSize;
    }

    @Override
    public String toString() {
        return "IndieStudio{" +
                "name='" + getName() + '\'' +
                ", country='" + getCountry() + '\'' +
                ", teamSize=" + teamSize +
                '}';
    }
}
