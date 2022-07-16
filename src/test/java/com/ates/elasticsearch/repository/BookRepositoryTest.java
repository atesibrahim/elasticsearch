package com.ates.elasticsearch.repository;

import com.ates.elasticsearch.configuration.ElasticsearchConfig;
import com.ates.elasticsearch.domain.Author;
import com.ates.elasticsearch.domain.Book;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.elasticsearch.index.query.Operator.AND;
import static org.elasticsearch.index.query.QueryBuilders.fuzzyQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.regexpQuery;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = ElasticsearchConfig.class)
class BookRepositoryTest {

    @Autowired
    private ElasticsearchRestTemplate elasticsearchTemplate;

    @Autowired
    private BookRepository bookRepository;

    private final Author johnSmith = new Author("John Smith");
    private final Author johnDoe = new Author("John Doe");

    @BeforeEach
    public void before() {
        Book book = Book.builder().title("Elastic Search").author(johnSmith).tags(new String[]{"elasticsearch", "spring data"}).build();
        bookRepository.save(book);

        Book book2 = Book.builder().title("Analytic Engine").author(johnDoe).tags(new String[]{"elasticsearch"}).build();
        bookRepository.save(book2);
    }

    @AfterEach
    public void after() {
        bookRepository.deleteAll();
    }

    @Test
    public void givenBookService_whenSaveBook_thenIdIsAssigned() {
        final List<Author> authors = List.of(johnSmith, johnDoe);

        Book book = Book.builder().title("Elastic Search").author(johnSmith).build();

        Book result = bookRepository.save(book);
        assertNotNull(result.getId());
    }

    @Test
    public void givenPersistedBooks_whenUseRegexQuery_thenRightBooksFound() {
        final NativeSearchQuery searchQuery = new NativeSearchQueryBuilder().withFilter(regexpQuery("title", ".*data.*"))
                .build();

        final SearchHits<Book> books = elasticsearchTemplate.search(searchQuery, Book.class, IndexCoordinates.of("blog"));

        assertEquals(1, books.getTotalHits());
    }

    @Test
    public void givenSavedDoc_whenTitleUpdated_thenCouldFindByUpdatedTitle() {
        final NativeSearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(fuzzyQuery("title", "serch"))
                .build();
        final SearchHits<Book> books = elasticsearchTemplate.search(searchQuery, Book.class, IndexCoordinates.of("blog"));

        assertEquals(1, books.getTotalHits());

        final Book book = books.getSearchHit(0)
                .getContent();
        final String newTitle = "Getting started with Search Engines";
        book.setTitle(newTitle);
        bookRepository.save(book);

        assertEquals(newTitle, bookRepository.findById(book.getId())
                .get()
                .getTitle());
    }

    @Test
    public void givenSavedDoc_whenDelete_thenRemovedFromIndex() {
        final String bookTitle = "Spring Data Elasticsearch";

        final NativeSearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(matchQuery("title", bookTitle).minimumShouldMatch("75%"))
                .build();
        final SearchHits<Book> books = elasticsearchTemplate.search(searchQuery, Book.class, IndexCoordinates.of("blog"));

        assertEquals(1, books.getTotalHits());
        final long count = bookRepository.count();

        bookRepository.delete(books.getSearchHit(0)
                .getContent());

        assertEquals(count - 1, bookRepository.count());
    }

    @Test
    public void givenSavedDoc_whenOneTermMatches_thenFindByTitle() {
        final NativeSearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(matchQuery("title", "Search engines").operator(AND))
                .build();
        final SearchHits<Book> books = elasticsearchTemplate.search(searchQuery, Book.class, IndexCoordinates.of("blog"));
        assertEquals(1, books.getTotalHits());
    }

}