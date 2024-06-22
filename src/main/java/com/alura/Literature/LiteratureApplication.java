package com.alura.Literature;

import com.alura.Literature.principal.Principal;
import com.alura.Literature.repository.AuthorRepository;
import com.alura.Literature.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class LiteratureApplication implements CommandLineRunner {
	@Autowired
	private BookRepository bookRepository;
	@Autowired
	private AuthorRepository authorRepository;

	public static void main(String[] args) {
		SpringApplication.run(LiteratureApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		Principal principal = new Principal(bookRepository, authorRepository);
		principal.showMenu();
	}
}
