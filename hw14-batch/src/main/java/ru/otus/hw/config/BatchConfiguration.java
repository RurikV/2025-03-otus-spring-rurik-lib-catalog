package ru.otus.hw.config;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import jakarta.persistence.EntityManagerFactory;
import ru.otus.hw.batch.processors.JpaToMongoAuthorProcessor;
import ru.otus.hw.batch.processors.JpaToMongoBookProcessor;
import ru.otus.hw.batch.processors.JpaToMongoCommentProcessor;
import ru.otus.hw.batch.processors.JpaToMongoGenreProcessor;
import ru.otus.hw.batch.writers.MongoAuthorItemWriter;
import ru.otus.hw.batch.writers.MongoBookItemWriter;
import ru.otus.hw.batch.writers.MongoCommentItemWriter;
import ru.otus.hw.batch.writers.MongoGenreItemWriter;
import ru.otus.hw.models.jpa.Author;
import ru.otus.hw.models.jpa.Book;
import ru.otus.hw.models.jpa.Comment;
import ru.otus.hw.models.jpa.Genre;
import ru.otus.hw.models.mongo.MongoAuthor;
import ru.otus.hw.models.mongo.MongoBook;
import ru.otus.hw.models.mongo.MongoComment;
import ru.otus.hw.models.mongo.MongoGenre;

@Configuration
public class BatchConfiguration {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Autowired
    private JpaToMongoBookProcessor jpaToMongoBookProcessor;

    @Autowired
    private MongoBookItemWriter mongoBookItemWriter;

    @Autowired
    private JpaToMongoCommentProcessor jpaToMongoCommentProcessor;

    @Autowired
    private MongoCommentItemWriter mongoCommentItemWriter;

    @Autowired
    private JpaToMongoGenreProcessor jpaToMongoGenreProcessor;

    @Autowired
    private MongoGenreItemWriter mongoGenreItemWriter;

    @Autowired
    private JpaToMongoAuthorProcessor jpaToMongoAuthorProcessor;

    @Autowired
    private MongoAuthorItemWriter mongoAuthorItemWriter;

    @Bean
    public JpaPagingItemReader<Book> jpaPagingBookItemReader() {
        return new JpaPagingItemReaderBuilder<Book>()
                .name("jpaPagingBookItemReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("SELECT b FROM Book b LEFT JOIN FETCH b.author LEFT JOIN FETCH b.genres")
                .pageSize(10)
                .build();
    }

    @Bean
    public JpaPagingItemReader<Comment> jpaPagingCommentItemReader() {
        return new JpaPagingItemReaderBuilder<Comment>()
                .name("jpaPagingCommentItemReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("SELECT c FROM Comment c")
                .pageSize(10)
                .build();
    }

    @Bean
    public JpaPagingItemReader<Author> jpaPagingAuthorItemReader() {
        return new JpaPagingItemReaderBuilder<Author>()
                .name("jpaPagingAuthorItemReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("SELECT a FROM Author a")
                .pageSize(10)
                .build();
    }

    @Bean
    public JpaPagingItemReader<Genre> jpaPagingGenreItemReader() {
        return new JpaPagingItemReaderBuilder<Genre>()
                .name("jpaPagingGenreItemReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("SELECT g FROM Genre g")
                .pageSize(10)
                .build();
    }

    @Bean
    public Step bookMigrationStep() {
        return new StepBuilder("bookMigrationStep", jobRepository)
                .<Book, MongoBook>chunk(10, transactionManager)
                .reader(jpaPagingBookItemReader())
                .processor(jpaToMongoBookProcessor)
                .writer(mongoBookItemWriter)
                .build();
    }

    @Bean
    public Step commentMigrationStep() {
        return new StepBuilder("commentMigrationStep", jobRepository)
                .<Comment, MongoComment>chunk(10, transactionManager)
                .reader(jpaPagingCommentItemReader())
                .processor(jpaToMongoCommentProcessor)
                .writer(mongoCommentItemWriter)
                .build();
    }

    @Bean
    public Step genreMigrationStep() {
        return new StepBuilder("genreMigrationStep", jobRepository)
                .<Genre, MongoGenre>chunk(10, transactionManager)
                .reader(jpaPagingGenreItemReader())
                .processor(jpaToMongoGenreProcessor)
                .writer(mongoGenreItemWriter)
                .build();
    }

    @Bean
    public Step authorMigrationStep() {
        return new StepBuilder("authorMigrationStep", jobRepository)
                .<Author, MongoAuthor>chunk(10, transactionManager)
                .reader(jpaPagingAuthorItemReader())
                .processor(jpaToMongoAuthorProcessor)
                .writer(mongoAuthorItemWriter)
                .build();
    }

    @Bean
    public Job migrationJob() {
        return new JobBuilder("migrationJob", jobRepository)
                .start(genreMigrationStep())
                .next(authorMigrationStep())
                .next(bookMigrationStep())
                .next(commentMigrationStep())
                .build();
    }
}