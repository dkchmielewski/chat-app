package com.chatapp.demo.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder // Dodano Builder dla bezpieczniejszej inicjalizacji
public class Content {
    private String role;
    private List<Part> parts;
}
