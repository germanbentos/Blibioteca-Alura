package com.alura.Literature.principal;

import com.alura.Literature.models.AllData;
import com.alura.Literature.models.Author;
import com.alura.Literature.models.Book;
import com.alura.Literature.models.DataBook;
import com.alura.Literature.repository.AuthorRepository;
import com.alura.Literature.repository.BookRepository;
import com.alura.Literature.service.APIConsumption;
import com.alura.Literature.service.ConvertData;

import java.util.*;
import java.util.stream.Collectors;

public class Principal {
    private APIConsumption apiConsumption = new APIConsumption();
    private ConvertData converter = new ConvertData();
    private final String URL_BASE = "https://gutendex.com/books/";
    private Scanner scanner = new Scanner(System.in);
    private BookRepository bookRepository;
    private AuthorRepository authorRepository;
    private List<Book> books;
    private List<Author> authors;

    public Principal(BookRepository bookRepository, AuthorRepository authorRepository) {
        this.bookRepository = bookRepository;
        this.authorRepository = authorRepository;
    }

    public void showMenu() {
        int option = -1;
        while (option != 0) {
            var menu = """
                    1 - Buscar libro por titulo
                    2 - Mostrar libros registrados
                    3 - Mostrar autores registrados
                    4-  Mostrar autores vivos en un determinado año
                    5 - Listar libros por idioma
                    6 - Buscar autor por nombre
                    7 - Top 5 libros más descargados
                    
                    """;

            System.out.println(menu);
            option = scanner.nextInt();
            scanner.nextLine();

            switch (option) {
                case 1:
                    searchBookForTitle();
                    break;
                case 2:
                    showBooksSearched();
                    break;
                case 3:
                    ShowAuthors();
                    break;
                case 4:
                    findAuthorsForYear();
                    break;
                case 5:
                    showBooksForLanguage();
                    break;
                case 6:
                    searchAuthorByName();
                    break;
                case 7:
                    top5MostDownloadedBooks();
                    break;
                case 0:
                    System.out.println("Cerrando aplicacion");
                    break;
                default:
                    System.out.println("Opción invalida");
            }

        }


    }


    //Traigo la info de la api
    private DataBook getDataBook() {
        System.out.println("Por favor ingresar el nombre del libro a buscar: ");
        String bookTitle = scanner.nextLine();
        var json = apiConsumption.getData(URL_BASE + "?search=" + bookTitle.replace(" ", "+"));
        System.out.println(json);
        AllData allData = converter.getDates(json, AllData.class);
        if (allData.books().isEmpty()) {
            throw new RuntimeException("No se encontraron libros con ese título.");
        }
        return allData.books().get(0);
    }

    public void searchBookForTitle() {
        //traigo info de la api y la convierto en dataBook
        DataBook dataBook = getDataBook();
        //Creo el nuevo libro con la info de la api y le seteo el autor
        // Verificar si el libro ya existe en la base de datos
        Book presentBook = bookRepository.findByTitleIgnoreCase(dataBook.title());

        if (presentBook != null) {
            System.out.println("No se puede registar el libro, ya existe");
        } else if (dataBook != null) {
            //Busco y/o guardo autores
            List<Author> authorList = dataBook.authors().stream()
                    .map(dataAuthor -> {
                        return authorRepository.findByFullName(dataAuthor.nameSurname())
                                .orElseGet(() -> authorRepository.save(new Author(dataAuthor)));

                    })
                    .collect(Collectors.toList());
            // Crear el nuevo libro
            Book newBook = new Book(dataBook, authorList);
            // Guardar el nuevo libro en la base de datos
            newBook.setAuthors(authorList);
            bookRepository.save(newBook);
            System.out.println(newBook);
        } else {
            System.out.println("Libro no encontrado");
        }

    }

    private void showBooksSearched() {
        books = bookRepository.findAllWithAuthors();
        books.stream()
                .sorted(Comparator.comparing(Book::getTitle))
                .forEach(System.out::println);
    }

    private void ShowAuthors() {
        authors = authorRepository.findAll();
        authors.stream()
                .sorted(Comparator.comparing(Author::getFullName))
                .forEach(System.out::println);
    }

    private void findAuthorsForYear() {
        System.out.println("Ingrese un año para buscar autores");
        int yearSearch = scanner.nextInt();
        authors = authorRepository.authorForYear(yearSearch);
        if (authors.isEmpty()) {
            System.out.println("No se encontraron autores vivos para ese año");
        } else {
            System.out.println(" ----- Autores vivos en ese año -----\n");
            authors.forEach(System.out::println);
        }


    }


    public void showBooksForLanguage() {
        System.out.println("Ingrese el idioma para buscar libros:\n" +
                "es - Español\n" +
                "en - Inglés\n" +
                "fr - Francés\n" +
                "pt - Portugués");

        Scanner scanner = new Scanner(System.in);
        String language = scanner.nextLine().trim().toLowerCase();

        // Lista de idiomas válidos
        List<String> validLanguages = Arrays.asList("es", "en", "fr", "pt");

        if (!validLanguages.contains(language)) {
            System.out.println("Opción de idioma inválida. Por favor, elija una de las opciones proporcionadas.");
            return;
        }

        List<Book> books = bookRepository.findByLanguagesContains(language);
        if (books.isEmpty()) {
            System.out.println("No se encontraron libros en el idioma '" + language + "'.");
        } else {
            System.out.println("Libros en el idioma '" + language + "':");
            books.forEach(book -> {
                System.out.println(book);
            });
        }
    }
    public void searchAuthorByName(){
        System.out.println("Ingrese el nombre del autor");
        String name = scanner.nextLine().toLowerCase();
        authors = authorRepository.findAll();
       Optional<Author> authorOptional = authors.stream()
                .filter(a-> a.getFullName().contains(name.toLowerCase()))
                .findFirst();
        if (authorOptional.isPresent()){
            System.out.println("Autor encontrado");
            System.out.println(authorOptional);
        } else{
            System.out.println("No se encontro en autor");
        }
    }
    public void top5MostDownloadedBooks(){
        books = bookRepository.findAll();
        books.stream()
                .sorted(Comparator.comparing(Book::getNumberOfDownloads).reversed())
                .limit(5)
                .forEach(System.out::println);
    }
}










