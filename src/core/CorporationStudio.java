package core;

/**
 * Dziedziczenie rozłączne (disjoint) – podklasa Studio:
 * studio należące do dużej korporacji (np. Sony, Microsoft).
 */
public class CorporationStudio extends Studio {

    private String parentCompany;

    public CorporationStudio(String name, String country, String parentCompany) {
        // studio korporacyjne -> isIndependent = false
        super(name, country, false);

        if (parentCompany == null || parentCompany.trim().isEmpty()) {
            throw new IllegalArgumentException("Parent company cannot be null or empty.");
        }
        this.parentCompany = parentCompany;
    }

    public String getParentCompany() {
        return parentCompany;
    }

    @Override
    public String toString() {
        return "CorporationStudio{" +
                "name='" + getName() + '\'' +
                ", country='" + getCountry() + '\'' +
                ", parentCompany='" + parentCompany + '\'' +
                '}';
    }
}
