package br.dev.jady.biblioteca;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

public final class Main {
    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);

    public static void main(String[] args) {
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));
        System.setErr(new PrintStream(System.err, true, StandardCharsets.UTF_8));
        Path arquivo = Path.of(args.length > 0 ? args[0] : "dados/biblioteca.tsv");
        try (Scanner entrada = new Scanner(System.in, StandardCharsets.UTF_8)) {
            Biblioteca biblioteca = new Biblioteca(arquivo, Clock.systemDefaultZone());
            System.out.println("BIBLIOTECA JAVA | Arquivo: " + arquivo.toAbsolutePath());
            while (true) {
                System.out.println("\n1 Cadastrar  2 Listar  3 Pesquisar  4 Emprestar  5 Devolver  6 Atrasados  7 Editar  8 Excluir  0 Sair");
                String opcao = ler(entrada, "Opção");
                if (opcao == null || opcao.equals("0")) break;
                try {
                    switch (opcao) {
                        case "1" -> {
                            Livro livro = biblioteca.cadastrar(obrigatorio(entrada, "Título"), obrigatorio(entrada, "Autor"));
                            System.out.println("Cadastrado. ID: " + livro.id());
                        }
                        case "2" -> mostrar(biblioteca.listar());
                        case "3" -> mostrar(biblioteca.pesquisar(obrigatorio(entrada, "Título ou autor")));
                        case "4" -> {
                            UUID id = id(entrada);
                            String leitor = obrigatorio(entrada, "Leitor (use um exemplo fictício)");
                            LocalDate prazo = LocalDate.parse(obrigatorio(entrada, "Prazo (dd/MM/aaaa)"), DATA);
                            biblioteca.emprestar(id, leitor, prazo);
                            System.out.println("Empréstimo registrado.");
                        }
                        case "5" -> { biblioteca.devolver(id(entrada)); System.out.println("Devolução registrada."); }
                        case "6" -> mostrar(biblioteca.atrasados());
                        case "7" -> {
                            UUID id = id(entrada);
                            biblioteca.editar(id, obrigatorio(entrada, "Novo título"), obrigatorio(entrada, "Novo autor"));
                            System.out.println("Livro atualizado.");
                        }
                        case "8" -> {
                            UUID id = id(entrada);
                            if ("sim".equalsIgnoreCase(obrigatorio(entrada, "Digite sim para excluir"))) {
                                biblioteca.excluir(id); System.out.println("Livro excluído.");
                            } else System.out.println("Exclusão cancelada.");
                        }
                        default -> System.out.println("Escolha uma opção do menu.");
                    }
                } catch (FimEntrada erro) { break; }
                catch (IOException erro) { System.out.println("Não foi possível salvar: " + erro.getMessage()); }
                catch (RuntimeException erro) { System.out.println("Entrada inválida: " + erro.getMessage()); }
            }
            System.out.println("Até a próxima!");
        } catch (IOException erro) {
            System.err.println("Biblioteca não iniciada: " + erro.getMessage());
            System.exit(1);
        }
    }

    private static String ler(Scanner entrada, String mensagem) {
        System.out.print(mensagem + ": ");
        return entrada.hasNextLine() ? entrada.nextLine().strip() : null;
    }
    private static String obrigatorio(Scanner entrada, String mensagem) {
        String valor = ler(entrada, mensagem);
        if (valor == null) throw new FimEntrada();
        return valor;
    }
    private static UUID id(Scanner entrada) { return UUID.fromString(obrigatorio(entrada, "ID do livro")); }
    private static void mostrar(List<Livro> livros) {
        if (livros.isEmpty()) { System.out.println("Nenhum livro encontrado."); return; }
        for (Livro livro : livros) {
            System.out.println(livro.id() + " | " + livro.titulo() + " | " + livro.autor());
            System.out.println(livro.disponivel() ? "  Disponível" : "  Leitor: " + livro.leitor() + " | Prazo: " + DATA.format(livro.prazo()));
        }
    }
    private static final class FimEntrada extends RuntimeException {
        private static final long serialVersionUID = 1L;
    }
}
