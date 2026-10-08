package br.dev.jady.biblioteca;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/** Estado imutável de um livro e, quando houver, de seu empréstimo. */
public record Livro(UUID id, String titulo, String autor, String leitor, LocalDate prazo) {
    public Livro {
        Objects.requireNonNull(id, "Identificador obrigatório.");
        titulo = texto(titulo, "Título", 120);
        autor = texto(autor, "Autor", 80);
        if ((leitor == null) != (prazo == null)) {
            throw new IllegalArgumentException("Leitor e prazo devem ser informados juntos.");
        }
        if (leitor != null) leitor = texto(leitor, "Leitor", 80);
    }

    public boolean disponivel() { return leitor == null; }

    public boolean atrasadoEm(LocalDate data) {
        return !disponivel() && prazo.isBefore(Objects.requireNonNull(data));
    }

    static String texto(String valor, String campo, int limite) {
        if (valor == null || valor.isBlank()) throw new IllegalArgumentException(campo + " obrigatório.");
        String limpo = valor.strip();
        if (limpo.length() > limite || limpo.chars().anyMatch(Character::isISOControl)) {
            throw new IllegalArgumentException(campo + " deve ter até " + limite + " caracteres, sem controles.");
        }
        return limpo;
    }
}
