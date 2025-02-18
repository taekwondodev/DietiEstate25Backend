package com.dietiestate25backend.dto.response;

import com.dietiestate25backend.model.Immobile;

public class CercaImmobileResponse {
    private final Immobile immobile;
    private final boolean hasParcoVicino;
    private final boolean hasScuolaVicino;
    private final boolean hasFermataVicino;

    public CercaImmobileResponse(Immobile immobile, boolean hasParcoVicino, boolean hasScuolaVicino, boolean hasFermataVicino) {
        this.immobile = immobile;
        this.hasParcoVicino = hasParcoVicino;
        this.hasScuolaVicino = hasScuolaVicino;
        this.hasFermataVicino = hasFermataVicino;
    }

    public Immobile getImmobile() {
        return immobile;
    }

    public boolean isHasParcoVicino() {
        return hasParcoVicino;
    }

    public boolean isHasScuolaVicino() {
        return hasScuolaVicino;
    }

    public boolean isHasFermataVicino() {
        return hasFermataVicino;
    }
}
