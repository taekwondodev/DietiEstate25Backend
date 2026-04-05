package com.dietiestate25backend.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@AllArgsConstructor
public class Offerta {
    @Setter
    private int idOfferta;
    @Positive
    private final double importo;
    @NotNull
    private final StatoOfferta stato;
    @Setter
    private String idCliente;
    @NotNull
    private final Immobile immobile;
}
