package com.acme.poc.notes.restservice.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
public class BaseRestException {
    @Getter@Setter
    private String status;

    @Getter @Setter
    private String message;
}
