package com.ates.elasticsearch.service;

import com.ates.elasticsearch.domain.Book;
import com.ates.elasticsearch.repository.BookRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@Service
public class BookService {

    @Autowired
    private BookRepository bookRepository;

    public List<Book> findAll() {
        Iterable<Book> book = bookRepository.findAll();
        List<Book> books = new ArrayList<>();
        book.iterator().forEachRemaining(books::add);
        return books;
    }

    public Book save(Book book) {
        return bookRepository.save(book);
    }

    public void deleteById(String id) {
        bookRepository.deleteById(id);
    }
}
