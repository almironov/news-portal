package com.dev.news.newsportal.service.rabbitmq;

import com.dev.news.newsportal.config.NewsPortalProperties;
import com.dev.news.newsportal.event.CommentCreatedEvent;
import com.dev.news.newsportal.event.NewsCreatedEvent;
import com.dev.news.newsportal.event.NewsUpdatedEvent;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Service for publishing events to RabbitMQ.
 * Handles event publishing with error handling, logging, and retry mechanisms.
 */
@Service
@Slf4j
class EventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final NewsPortalProperties properties;
    private final MeterRegistry meterRegistry;
    
    // Metrics counters
    private final Counter newsCreatedCounter;
    private final Counter newsUpdatedCounter;
    private final Counter commentCreatedCounter;
    private final Counter publishingErrorCounter;
    private final Timer publishingTimer;
    
    public EventPublisher(RabbitTemplate rabbitTemplate, NewsPortalProperties properties, MeterRegistry meterRegistry) {
        this.rabbitTemplate = rabbitTemplate;
        this.properties = properties;
        this.meterRegistry = meterRegistry;
        
        // Initialize metrics
        this.newsCreatedCounter = Counter.builder("rabbitmq.events.published")
                .tag("event.type", "news.created")
                .description("Number of news created events published")
                .register(meterRegistry);
        
        this.newsUpdatedCounter = Counter.builder("rabbitmq.events.published")
                .tag("event.type", "news.updated")
                .description("Number of news updated events published")
                .register(meterRegistry);
        
        this.commentCreatedCounter = Counter.builder("rabbitmq.events.published")
                .tag("event.type", "comment.created")
                .description("Number of comment created events published")
                .register(meterRegistry);
        
        this.publishingErrorCounter = Counter.builder("rabbitmq.events.publishing.errors")
                .description("Number of event publishing errors")
                .register(meterRegistry);
        
        this.publishingTimer = Timer.builder("rabbitmq.events.publishing.duration")
                .description("Time taken to publish events")
                .register(meterRegistry);
    }

    /**
     * Publishes a news created event to RabbitMQ.
     *
     * @param event the NewsCreatedEvent to publish
     */
    @Retryable(retryFor = Exception.class, maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 2))
    public void publishNewsCreatedEvent(NewsCreatedEvent event) {
        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            log.debug("Publishing news created event: {}", event);
            
            String exchange = properties.getRabbitMq().getExchanges().getNews().getExchange();
            String routingKey = properties.getRabbitMq().getExchanges().getNews().getBinding().getCreated().getKey();
            
            CorrelationData correlationData = new CorrelationData(UUID.randomUUID().toString());
            
            rabbitTemplate.convertAndSend(exchange, routingKey, event, correlationData);
            
            newsCreatedCounter.increment();
            log.info("Successfully published news created event for newsId: {}", event.newsId());
        } catch (Exception e) {
            publishingErrorCounter.increment();
            log.error("Failed to publish news created event for newsId: {}", event.newsId(), e);
            throw e;
        } finally {
            sample.stop(publishingTimer);
        }
    }

    /**
     * Publishes a news updated event to RabbitMQ.
     *
     * @param event the NewsUpdatedEvent to publish
     */
    @Retryable(retryFor = Exception.class, maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 2))
    public void publishNewsUpdatedEvent(NewsUpdatedEvent event) {
        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            log.debug("Publishing news updated event: {}", event);
            
            String exchange = properties.getRabbitMq().getExchanges().getNews().getExchange();
            String routingKey = properties.getRabbitMq().getExchanges().getNews().getBinding().getUpdated().getKey();
            
            CorrelationData correlationData = new CorrelationData(UUID.randomUUID().toString());
            
            rabbitTemplate.convertAndSend(exchange, routingKey, event, correlationData);
            
            newsUpdatedCounter.increment();
            log.info("Successfully published news updated event for newsId: {}", event.newsId());
        } catch (Exception e) {
            publishingErrorCounter.increment();
            log.error("Failed to publish news updated event for newsId: {}", event.newsId(), e);
            throw e;
        } finally {
            sample.stop(publishingTimer);
        }
    }

    /**
     * Publishes a comment created event to RabbitMQ.
     *
     * @param event the CommentCreatedEvent to publish
     */
    @Retryable(retryFor = Exception.class, maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 2))
    public void publishCommentCreatedEvent(CommentCreatedEvent event) {
        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            log.debug("Publishing comment created event: {}", event);
            
            String exchange = properties.getRabbitMq().getExchanges().getComments().getExchange();
            String routingKey = properties.getRabbitMq().getExchanges().getComments().getBinding().getCreated().getKey();
            
            CorrelationData correlationData = new CorrelationData(UUID.randomUUID().toString());
            
            rabbitTemplate.convertAndSend(exchange, routingKey, event, correlationData);
            
            commentCreatedCounter.increment();
            log.info("Successfully published comment created event for commentId: {} on newsId: {}", 
                    event.commentId(), event.newsId());
        } catch (Exception e) {
            publishingErrorCounter.increment();
            log.error("Failed to publish comment created event for commentId: {} on newsId: {}", 
                    event.commentId(), event.newsId(), e);
            throw e;
        } finally {
            sample.stop(publishingTimer);
        }
    }
}