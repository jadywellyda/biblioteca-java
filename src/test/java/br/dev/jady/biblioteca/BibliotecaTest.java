package br.dev.jady.biblioteca;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.UUID;

/** Testes executáveis sem dependências externas. Falhas terminam o processo com erro. */
public final class BibliotecaTest {
    private static int verificacoes;
    private static final Clock HOJE = Clock.fixed(Instant.parse("2026-10-08T12:00:00Z"), ZoneOffset.UTC);
    @FunctionalInterface interface Acao { void executar() throws Exception; }

    public static void main(String[] args) throws Exception {
        Path pasta = Files.createTempDirectory("biblioteca-test-");
        try {
            Path arquivo = pasta.resolve("livros.tsv");
            Biblioteca b = new Biblioteca(arquivo, HOJE);
            checar(b.listar().isEmpty(), "Biblioteca nova vazia");
            Livro l = b.cadastrar("  Lógica com Java  ", "Autora Exemplo");
            checar(l.titulo().equals("Lógica com Java"), "Normalização e acentos");
            checar(b.pesquisar("JAVA").size() == 1, "Pesquisa sem distinção de maiúsculas");
            falha(IllegalArgumentException.class, () -> b.cadastrar(" ", "Autora"));
            falha(IllegalArgumentException.class, () -> b.cadastrar("Título\tinválido", "Autora"));
            falha(IllegalArgumentException.class, () -> b.emprestar(l.id(), "Leitor Exemplo", LocalDate.of(2026, 10, 7)));
            b.emprestar(l.id(), "Leitor Exemplo", LocalDate.of(2026, 10, 8));
            checar(b.atrasados().isEmpty(), "Prazo vence hoje, ainda não atrasado");
            falha(IllegalArgumentException.class, () -> b.emprestar(l.id(), "Outro", LocalDate.of(2026, 10, 9)));
            falha(IllegalArgumentException.class, () -> b.excluir(l.id()));
            b.editar(l.id(), "Java em prática", "Autora Exemplo");
            checar(!b.listar().getFirst().disponivel(), "Edição preserva empréstimo");
            Biblioteca recarregada = new Biblioteca(arquivo, HOJE);
            checar(recarregada.listar().getFirst().leitor().equals("Leitor Exemplo"), "Empréstimo persistido");
            Clock amanha = Clock.fixed(Instant.parse("2026-10-09T12:00:00Z"), ZoneOffset.UTC);
            checar(new Biblioteca(arquivo, amanha).atrasados().size() == 1, "Detecção de atraso");
            b.devolver(l.id());
            falha(IllegalArgumentException.class, () -> b.devolver(l.id()));
            b.excluir(l.id());
            checar(new Biblioteca(arquivo, HOJE).listar().isEmpty(), "Exclusão persistida");
            falha(IllegalArgumentException.class, () -> b.editar(UUID.randomUUID(), "Livro", "Autor"));

            Files.writeString(arquivo, "BIBLIOTECA_V1\nconteúdo inválido\n");
            String original = Files.readString(arquivo);
            falha(IOException.class, () -> new Biblioteca(arquivo, HOJE));
            checar(Files.readString(arquivo).equals(original), "Arquivo corrompido preservado");

            Path bloqueio = pasta.resolve("bloqueio");
            Biblioteca semGravacao = new Biblioteca(bloqueio.resolve("livros.tsv"), HOJE);
            Files.writeString(bloqueio, "Este caminho é um arquivo, não uma pasta.");
            falha(IOException.class, () -> semGravacao.cadastrar("Não salvo", "Autor"));
            checar(semGravacao.listar().isEmpty(), "Falha de gravação não altera estado em memória");
            System.out.println("BibliotecaTest: " + verificacoes + " verificações passaram.");
        } finally {
            try (var caminhos = Files.walk(pasta)) {
                for (Path p : caminhos.sorted(java.util.Comparator.reverseOrder()).toList()) Files.delete(p);
            }
        }
    }
    private static void checar(boolean condicao, String mensagem) {
        if (!condicao) throw new AssertionError(mensagem);
        verificacoes++;
    }
    private static void falha(Class<? extends Throwable> esperado, Acao acao) throws Exception {
        try { acao.executar(); } catch (Exception erro) {
            if (!esperado.isInstance(erro)) throw erro;
            verificacoes++; return;
        }
        throw new AssertionError("Esperada exceção: " + esperado.getSimpleName());
    }
}
