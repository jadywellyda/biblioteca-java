package br.dev.jady.biblioteca;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

/** Arquivo versionado: campos de texto em Base64 e atualização por arquivo temporário. */
final class RepositorioLivros {
    private final Path arquivo;
    RepositorioLivros(Path arquivo) { this.arquivo = arquivo.toAbsolutePath(); }

    List<Livro> carregar() throws IOException {
        if (!Files.exists(arquivo)) return List.of();
        List<String> linhas = Files.readAllLines(arquivo, StandardCharsets.UTF_8);
        if (linhas.isEmpty() || !linhas.getFirst().equals("BIBLIOTECA_V1")) {
            throw new IOException("Arquivo de biblioteca inválido ou de versão desconhecida.");
        }
        List<Livro> livros = new ArrayList<>();
        HashSet<UUID> ids = new HashSet<>();
        for (int i = 1; i < linhas.size(); i++) {
            try {
                String[] c = linhas.get(i).split("\t", -1);
                if (c.length != 5) throw new IllegalArgumentException("Quantidade de campos inválida.");
                UUID id = UUID.fromString(c[0]);
                if (!ids.add(id)) throw new IllegalArgumentException("Identificador duplicado.");
                livros.add(new Livro(id, decodificar(c[1]), decodificar(c[2]),
                        c[3].equals("-") ? null : decodificar(c[3]),
                        c[4].equals("-") ? null : LocalDate.parse(c[4])));
            } catch (RuntimeException erro) {
                throw new IOException("Registro inválido na linha " + (i + 1) + ". Arquivo preservado.", erro);
            }
        }
        return List.copyOf(livros);
    }

    void salvar(List<Livro> livros) throws IOException {
        Files.createDirectories(arquivo.getParent());
        Path temporario = Files.createTempFile(arquivo.getParent(), ".biblioteca-", ".tmp");
        try {
            List<String> linhas = new ArrayList<>();
            linhas.add("BIBLIOTECA_V1");
            for (Livro livro : livros) {
                linhas.add(livro.id() + "\t" + codificar(livro.titulo()) + "\t" + codificar(livro.autor())
                        + "\t" + (livro.disponivel() ? "-" : codificar(livro.leitor()))
                        + "\t" + (livro.disponivel() ? "-" : livro.prazo()));
            }
            Files.write(temporario, linhas, StandardCharsets.UTF_8);
            try {
                Files.move(temporario, arquivo, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException erro) {
                Files.move(temporario, arquivo, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporario);
        }
    }

    private static String codificar(String valor) {
        return Base64.getEncoder().encodeToString(valor.getBytes(StandardCharsets.UTF_8));
    }
    private static String decodificar(String valor) {
        return new String(Base64.getDecoder().decode(valor), StandardCharsets.UTF_8);
    }
}
