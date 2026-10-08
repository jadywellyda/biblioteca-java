# Biblioteca Java

Aplicação de console para organizar um pequeno acervo e acompanhar empréstimos. O projeto demonstra programação orientada a objetos, validação, coleções, datas e persistência em arquivos usando Java 21.

## Funcionalidades

- Cadastrar, listar, pesquisar, editar e excluir livros.
- Pesquisar por título ou autor sem distinguir maiúsculas e minúsculas.
- Registrar empréstimos com leitor e prazo de devolução.
- Devolver livros e consultar empréstimos atrasados.
- Preservar os registros ao fechar e abrir o programa.

## Regras de negócio

Um livro já emprestado não pode ser emprestado novamente. O prazo de um novo empréstimo deve ser hoje ou uma data futura. O livro se torna atrasado no dia seguinte ao prazo, conforme a data do computador. A exclusão exige que o livro esteja disponível. Editar título ou autor mantém o empréstimo existente.

Títulos, autores e leitores devem conter texto, respeitar o limite informado e não conter caracteres de controle. Os identificadores são UUIDs; o menu de listagem exibe o ID para uso nas demais operações.

## Requisitos

- JDK 21 ou superior, com `java` e `javac` disponíveis no terminal.
- PowerShell no Windows ou um shell compatível com `sh` no Linux/macOS.

Não usa Maven, banco de dados nem bibliotecas externas.

## Executar

Baixe o repositório e abra um terminal na pasta do projeto.

**Windows / PowerShell:**

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\run.ps1
```

**Linux / macOS:**

```sh
sh run.sh
```

O script compila o código antes de abrir o menu. O programa começa com o acervo vazio; os dados ficam em `dados/biblioteca.tsv`. Essa pasta é ignorada pelo Git.

Para escolher outro arquivo de dados:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\run.ps1 -Arquivo dados/exemplo.tsv
```

```sh
sh run.sh executar dados/exemplo.tsv
```

## Exemplo de uso

1. Escolha **1 Cadastrar**, com o título `Lógica com Java` e autor `Autora Exemplo`.
2. Escolha **2 Listar** e copie o ID exibido.
3. Escolha **4 Emprestar**, informe o ID, `Leitor Exemplo` e um prazo futuro em `dd/MM/aaaa`.
4. Escolha **5 Devolver** e informe o ID.
5. Feche o programa e abra novamente para conferir os registros.

Use um terminal configurado para UTF-8; a aplicação lê e escreve texto nesse formato.

## Testes

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\run.ps1 -Acao testar
```

```sh
sh run.sh testar
```

São **19 verificações automáticas** de cadastro, validação, busca, empréstimo, prazo, devolução, edição, exclusão, persistência e falha de gravação. Os testes usam relógio fixo e arquivos temporários para não depender da data atual nem alterar o acervo de uso.

**Validação realizada:** compilação em JDK 21 com `-Xlint:all -Werror`, execução das verificações e teste dos menus reais no Windows, incluindo acentos e reinício com persistência. O script para Linux/macOS é fornecido, mas não foi executado neste ambiente.

## Organização do código

```text
src/main/java/br/dev/jady/biblioteca/
  Livro.java              Modelo imutável e validações
  Biblioteca.java         Regras de negócio e consultas
  RepositorioLivros.java   Leitura e gravação do arquivo
  Main.java               Menu e entrada de dados
src/test/java/br/dev/jady/biblioteca/
  BibliotecaTest.java      Verificações executáveis sem dependências
run.ps1                   Compilação e execução no Windows
run.sh                    Compilação e execução no Linux/macOS
```

O formato de arquivo tem cabeçalho de versão e campos separados por tabulação. Textos são codificados em Base64 para não quebrar a estrutura; isso não é criptografia. A gravação usa um arquivo temporário na mesma pasta e tenta substituir o destino de forma atômica, com alternativa quando o sistema de arquivos não oferece esse recurso. O estado em memória só é atualizado depois que a gravação termina.

Um arquivo inválido impede a inicialização e não é sobrescrito automaticamente.

## Limitações e contexto

Projeto de estudo desenvolvido com assistência de IA, com exemplos fictícios. É uma aplicação de console, para uma pessoa e uma instância de cada vez. Não possui autenticação, sincronização, interface web, controle de acesso ou tratamento de alterações simultâneas por dois processos. O arquivo local não é criptografado; use dados de exemplo e mantenha cópias dos arquivos que quiser preservar.

## Referências técnicas

- [API Java 21 — Clock](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/time/Clock.html)
- [API Java 21 — Files](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/nio/file/Files.html)
