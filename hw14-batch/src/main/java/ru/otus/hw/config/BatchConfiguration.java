package ru.otus.hw.config;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import ru.otus.hw.batch.processors.BookItemProcessor;
import ru.otus.hw.batch.processors.CommentItemProcessor;
import ru.otus.hw.batch.readers.MongoBookItemReader;
import ru.otus.hw.batch.readers.MongoCommentItemReader;
import ru.otus.hw.batch.writers.BookItemWriter;
import ru.otus.hw.batch.writers.CommentItemWriter;
import ru.otus.hw.models.jpa.Book;
import ru.otus.hw.models.jpa.Comment;
import ru.otus.hw.models.mongo.MongoBook;
import ru.otus.hw.models.mongo.MongoComment;

@Configuration
public class BatchConfiguration {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private MongoBookItemReader mongoBookItemReader;

    @Autowired
    private BookItemProcessor bookItemProcessor;

    @Autowired
    private BookItemWriter bookItemWriter;

    @Autowired
    private MongoCommentItemReader mongoCommentItemReader;

    @Autowired
    private CommentItemProcessor commentItemProcessor;

    @Autowired
    private CommentItemWriter commentItemWriter;

    @Bean
    public Step bookMigrationStep() {
        return new StepBuilder("bookMigrationStep", jobRepository)
                .<MongoBook, Book>chunk(10, transactionManager)
                .reader(mongoBookItemReader)
                .processor(bookItemProcessor)
                .writer(bookItemWriter)
                .build();
    }

    @Bean
    public Step commentMigrationStep() {
        return new StepBuilder("commentMigrationStep", jobRepository)
                .<MongoComment, Comment>chunk(10, transactionManager)
                .reader(mongoCommentItemReader)
                .processor(commentItemProcessor)
                .writer(commentItemWriter)
                .build();
    }

    @Bean
    public Job migrationJob() {
        return new JobBuilder("migrationJob", jobRepository)
                .start(bookMigrationStep())
                .next(commentMigrationStep())
                .build();
    }
}