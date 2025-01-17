package com.dietiestate25backend.model;

public enum TipoClasseEnergetica {
    A_PLUS_PLUS("A++"),
    A_PLUS("A+"),
    A("A"),
    B("B"),
    C("C"),
    D("D"),
    E("E"),
    F("F"),
    G("G");

    private final String classe;

    TipoClasseEnergetica(String classe) {
        this.classe = classe;
    }

    public String getClasse() {
        return classe;
    }

    /// Probabilmente non serve. Tenere fino a quando non si è sicuri
    public static TipoClasseEnergetica fromString(String classe) {
        for (TipoClasseEnergetica c : TipoClasseEnergetica.values()) {
            if (c.getClasse().equalsIgnoreCase(classe)) {
                return c;
            }
        }
        throw new IllegalArgumentException("Classe energetica non valida: " + classe);
    }
}
