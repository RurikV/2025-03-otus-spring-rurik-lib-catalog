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
    
    private final JobRepository jobRepository;

    private final PlatformTransactionManager transactionManager;

    private final MongoBookItemReader mongoBookItemReader;

    private final BookItemProcessor bookItemProcessor;

    private final BookItemWriter bookItemWriter;

    private final MongoCommentItemReader mongoCommentItemReader;

    private final CommentItemProcessor commentItemProcessor;

    private final CommentItemWriter commentItemWriter;
    
    @Autowired
    public BatchConfiguration(JobRepository jobRepository,
                             PlatformTransactionManager transactionManager,
                             MongoBookItemReader mongoBookItemReader,
                             BookItemProcessor bookItemProcessor,
                             BookItemWriter bookItemWriter,
                             MongoCommentItemReader mongoCommentItemReader,
                             CommentItemProcessor commentItemProcessor,
                             CommentItemWriter commentItemWriter) {
        this.jobRepository = jobRepository;
        this.transactionManager = transactionManager;
        this.mongoBookItemReader = mongoBookItemReader;
        this.bookItemProcessor = bookItemProcessor;
        this.bookItemWriter = bookItemWriter;
        this.mongoCommentItemReader = mongoCommentItemReader;
        this.commentItemProcessor = commentItemProcessor;
        this.commentItemWriter = commentItemWriter;
    }
    
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