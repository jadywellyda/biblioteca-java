package br.dev.jady.biblioteca;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

/** Regras de negócio independentes da interface do terminal. */
public final class Biblioteca {
    private final RepositorioLivros repositorio;
    private final Clock relogio;
    private List<Livro> livros;

    public Biblioteca(Path arquivo, Clock relogio) throws IOException {
        repositorio = new RepositorioLivros(Objects.requireNonNull(arquivo));
        this.relogio = Objects.requireNonNull(relogio);
        livros = repositorio.carregar();
    }

    public List<Livro> listar() {
        return livros.stream().sorted(Comparator.comparing(Livro::titulo, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(Livro::id)).toList();
    }

    public List<Livro> pesquisar(String termo) {
        String busca = Objects.requireNonNull(termo).strip().toLowerCase(Locale.ROOT);
        return listar().stream().filter(l -> (l.titulo() + " " + l.autor()).toLowerCase(Locale.ROOT).contains(busca)).toList();
    }

    public List<Livro> atrasados() {
        LocalDate hoje = LocalDate.now(relogio);
        return listar().stream().filter(l -> l.atrasadoEm(hoje)).toList();
    }

    public Livro cadastrar(String titulo, String autor) throws IOException {
        Livro novo = new Livro(UUID.randomUUID(), titulo, autor, null, null);
        List<Livro> proxima = new ArrayList<>(livros);
        proxima.add(novo);
        confirmar(proxima);
        return novo;
    }

    public void editar(UUID id, String titulo, String autor) throws IOException {
        Livro atual = encontrar(id);
        substituir(new Livro(id, titulo, autor, atual.leitor(), atual.prazo()));
    }

    public void emprestar(UUID id, String leitor, LocalDate prazo) throws IOException {
        Livro atual = encontrar(id);
        if (!atual.disponivel()) throw new IllegalArgumentException("Livro já emprestado.");
        if (prazo == null || prazo.isBefore(LocalDate.now(relogio))) {
            throw new IllegalArgumentException("Prazo deve ser hoje ou uma data futura.");
        }
        substituir(new Livro(id, atual.titulo(), atual.autor(), leitor, prazo));
    }

    public void devolver(UUID id) throws IOException {
        Livro atual = encontrar(id);
        if (atual.disponivel()) throw new IllegalArgumentException("Livro não está emprestado.");
        substituir(new Livro(id, atual.titulo(), atual.autor(), null, null));
    }

    public void excluir(UUID id) throws IOException {
        Livro atual = encontrar(id);
        if (!atual.disponivel()) throw new IllegalArgumentException("Devolva o livro antes de excluí-lo.");
        List<Livro> proxima = new ArrayList<>(livros);
        proxima.remove(atual);
        confirmar(proxima);
    }

    private Livro encontrar(UUID id) {
        return livros.stream().filter(l -> l.id().equals(id)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Livro não encontrado."));
    }

    private void substituir(Livro novo) throws IOException {
        List<Livro> proxima = new ArrayList<>(livros);
        proxima.replaceAll(l -> l.id().equals(novo.id()) ? novo : l);
        confirmar(proxima);
    }

    private void confirmar(List<Livro> proxima) throws IOException {
        repositorio.salvar(proxima);
        livros = List.copyOf(proxima);
    }
}
