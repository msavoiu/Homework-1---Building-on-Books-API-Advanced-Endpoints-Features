package com.example.webbackend.controller;

import com.example.webbackend.entity.Book;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class BookController {

    private List<Book> books = new ArrayList<>();

    private Long nextId = 1L;

    public BookController() {
        // Add 15 books with varied data for testing
        books.add(new Book(nextId++, "Spring Boot in Action", "Craig Walls", 39.99));
        books.add(new Book(nextId++, "Effective Java", "Joshua Bloch", 45.00));
        books.add(new Book(nextId++, "Clean Code", "Robert Martin", 42.50));
        books.add(new Book(nextId++, "Java Concurrency in Practice", "Brian Goetz", 49.99));
        books.add(new Book(nextId++, "Design Patterns", "Gang of Four", 54.99));
        books.add(new Book(nextId++, "Head First Java", "Kathy Sierra", 35.00));
        books.add(new Book(nextId++, "Spring in Action", "Craig Walls", 44.99));
        books.add(new Book(nextId++, "Clean Architecture", "Robert Martin", 39.99));
        books.add(new Book(nextId++, "Refactoring", "Martin Fowler", 47.50));
        books.add(new Book(nextId++, "The Pragmatic Programmer", "Andrew Hunt", 41.99));
        books.add(new Book(nextId++, "You Don't Know JS", "Kyle Simpson", 29.99));
        books.add(new Book(nextId++, "JavaScript: The Good Parts", "Douglas Crockford", 32.50));
        books.add(new Book(nextId++, "Eloquent JavaScript", "Marijn Haverbeke", 27.99));
        books.add(new Book(nextId++, "Python Crash Course", "Eric Matthes", 38.00));
        books.add(new Book(nextId++, "Automate the Boring Stuff", "Al Sweigart", 33.50));
    }

    // get all books - /api/books
    @GetMapping("/books")
    public List<Book> getBooks() {
        return books;
    }

    // get book by id
    @GetMapping("/books/{id}")
    public Book getBook(@PathVariable Long id) {
        return books.stream().filter(book -> book.getId().equals(id))
                .findFirst().orElse(null);
    }

    // create a new book
    @PostMapping("/books")
    public List<Book> createBook(@RequestBody Book book) {
        books.add(book);
        return books;
    }

    // ADDED ENDPOINTS: //////////////////////////////////////////////////////////////////////////////////////////

    // PUT endpoint (update book)
    @PutMapping("/books/{id}")
    public ResponseEntity<Book> putBook(
        @PathVariable Long id,
        @RequestParam(required = true) String title,
        @RequestParam(required = true) String author,
        @RequestParam(required = true) Double price
    ) {

        for (Book book : books) {
            if (book.getId().equals(id)) {
                book.setTitle(title);
                book.setAuthor(author);
                book.setPrice(price);
                
                return ResponseEntity.ok(book);
            }
        }

        return ResponseEntity.notFound().build();

    }

    // PATCH endpoint (partial update)
    @PatchMapping("/books/{id}")
    public ResponseEntity<Book> patchBook(
        @PathVariable Long id,
        @RequestParam(required = false) String title,
        @RequestParam(required = false) String author,
        @RequestParam(required = false) Double price
    ) {
        for (Book book : books) {
            if (book.getId().equals(id)) {
                if (title != null && !title.isBlank()) {
                    book.setTitle(title);
                }
                if (author != null && !author.isBlank()) {
                    book.setAuthor(author);
                }
                if (price != null) {
                    book.setPrice(price);
                }
                return ResponseEntity.ok(book);
            }
        }

        return ResponseEntity.notFound().build();
    }

    // DELETE endpoint (remove book)
    @DeleteMapping("/books/{id}")
    public ResponseEntity<Book> deleteBook(@PathVariable Long id) {
        int deletion_index = -1;

        for (int i = 0; i < books.size(); ++i) {
            if (books.get(i).getId().equals(id)) {
                deletion_index = i;
                break;
            }
        }

        if (deletion_index != -1) {
            books.remove(deletion_index);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // GET endpoint with pagination (offset and limit pagination)
    @GetMapping("/books/paginated")
    public List<Book> getPage(@RequestParam int offset, @RequestParam int limit) {
        return books.stream().skip(offset).limit(limit)
               .collect(Collectors.toList());
    }

    // Advanced GET endpoint with filtering, sorting, and pagination combined in the valid order
    // For this endpoint, I've chosen to filter and sort by price
    @GetMapping("/books/advanced")
    public List<Book> getBooksByPrice(
        @RequestParam Long offset,
        @RequestParam Long limit,
        @RequestParam boolean priceAbove,
        @RequestParam double cutoffPrice,
        @RequestParam boolean ascending) {

        Stream<Book> stream = books.stream();

        // Filter
        if (priceAbove) {
            stream = stream.filter(book -> book.getPrice() > cutoffPrice);
        } else {
            stream = stream.filter(book -> book.getPrice() < cutoffPrice);
        }

        Comparator<Book> comparator = Comparator.comparing(Book::getPrice);
        if (!ascending) {
            comparator = comparator.reversed();
        }

        // Sort and paginate
        return stream.sorted(comparator).skip(offset).limit(limit).collect(Collectors.toList());
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // search by title
    @GetMapping("/books/search")
    public List<Book> searchByTitle(
            @RequestParam(required = false, defaultValue = "") String title
    ) {
        if(title.isEmpty()) {
            return books;
        }

        return books.stream()
                .filter(book -> book.getTitle().toLowerCase().contains(title.toLowerCase()))
                .collect(Collectors.toList());

    }

    // price range
    @GetMapping("/books/price-range")
    public List<Book> getBooksByPrice(
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice
    ) {
        return books.stream()
                .filter(book -> {
                    boolean min = minPrice == null || book.getPrice() >= minPrice;
                    boolean max = maxPrice == null || book.getPrice() <= maxPrice;

                    return min && max;
                }).collect(Collectors.toList());
    }

    // sort
    @GetMapping("/books/sorted")
    public List<Book> getSortedBooks(
            @RequestParam(required = false, defaultValue = "title") String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String order
    ){
        Comparator<Book> comparator;

        switch(sortBy.toLowerCase()) {
            case "author":
                comparator = Comparator.comparing(Book::getAuthor);
                break;
                case "title":
                comparator = Comparator.comparing(Book::getTitle);
            default:
                comparator = Comparator.comparing(Book::getTitle);
                break;
        }

        if("desc".equalsIgnoreCase(order)) {
            comparator = comparator.reversed();
        }

        return books.stream().sorted(comparator)
                .collect(Collectors.toList());



    }


}
