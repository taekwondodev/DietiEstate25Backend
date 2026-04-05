package com.dietiestate25backend.dto.requests;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MeteoRequest {
    @NotBlank(message = "Latitudine non può essere vuota")
    private String latitudine;

    @NotBlank(message = "Longitudine non può essere vuota")
    private String longitudine;

    @NotBlank(message = "Data non può essere vuota")
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "Data deve essere nel formato YYYY-MM-DD")
    private String date;
}
