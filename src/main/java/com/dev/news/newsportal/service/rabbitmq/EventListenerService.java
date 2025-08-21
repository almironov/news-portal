package com.dev.news.newsportal.service.rabbitmq;

import com.dev.news.newsportal.event.CommentCreatedApplicationEvent;
import com.dev.news.newsportal.event.CommentCreatedEvent;
import com.dev.news.newsportal.event.NewsCreatedApplicationEvent;
import com.dev.news.newsportal.event.NewsCreatedEvent;
import com.dev.news.newsportal.event.NewsUpdatedApplicationEvent;
import com.dev.news.newsportal.event.NewsUpdatedEvent;
import com.dev.news.newsportal.mapper.event.CommentEventMapper;
import com.dev.news.newsportal.mapper.event.NewsEventMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Service for handling application events and publishing them to RabbitMQ.
 * Uses @TransactionalEventListener to ensure events are only published after successful transaction commit.
 */
@Service
@RequiredArgsConstructor
@Slf4j
class EventListenerService {

    private final EventPublisher eventPublisher;
    private final NewsEventMapper newsEventMapper;
    private final CommentEventMapper commentEventMapper;

    /**
     * Handles NewsCreatedApplicationEvent and publishes it to RabbitMQ.
     * Only executes after the transaction commits successfully.
     *
     * @param event the NewsCreatedApplicationEvent
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleNewsCreated(NewsCreatedApplicationEvent event) {
        try {
            log.debug("Processing news created application event for newsId: {}", event.getNews().getId());
            
            NewsCreatedEvent newsCreatedEvent = newsEventMapper.toNewsCreatedEvent(event.getNews());
            eventPublisher.publishNewsCreatedEvent(newsCreatedEvent);
            
            log.info("Successfully processed news created event for newsId: {}", event.getNews().getId());
        } catch (Exception e) {
            log.error("Failed to process news created event for newsId: {}", event.getNews().getId(), e);
            // Note: Exception is logged but not re-thrown to avoid affecting the main transaction
            // In production, consider implementing dead letter queue or retry mechanisms
        }
    }

    /**
     * Handles NewsUpdatedApplicationEvent and publishes it to RabbitMQ.
     * Only executes after the transaction commits successfully.
     *
     * @param event the NewsUpdatedApplicationEvent
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleNewsUpdated(NewsUpdatedApplicationEvent event) {
        try {
            log.debug("Processing news updated application event for newsId: {}", event.getNews().getId());
            
            NewsUpdatedEvent newsUpdatedEvent = newsEventMapper.toNewsUpdatedEvent(event.getNews());
            eventPublisher.publishNewsUpdatedEvent(newsUpdatedEvent);
            
            log.info("Successfully processed news updated event for newsId: {}", event.getNews().getId());
        } catch (Exception e) {
            log.error("Failed to process news updated event for newsId: {}", event.getNews().getId(), e);
            // Note: Exception is logged but not re-thrown to avoid affecting the main transaction
            // In production, consider implementing dead letter queue or retry mechanisms
        }
    }

    /**
     * Handles CommentCreatedApplicationEvent and publishes it to RabbitMQ.
     * Only executes after the transaction commits successfully.
     *
     * @param event the CommentCreatedApplicationEvent
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCommentCreated(CommentCreatedApplicationEvent event) {
        try {
            log.debug("Processing comment created application event for commentId: {} on newsId: {}", 
                    event.getComment().getId(), event.getComment().getNews().getId());
            
            CommentCreatedEvent commentCreatedEvent = commentEventMapper.toCommentCreatedEvent(event.getComment());
            eventPublisher.publishCommentCreatedEvent(commentCreatedEvent);
            
            log.info("Successfully processed comment created event for commentId: {} on newsId: {}", 
                    event.getComment().getId(), event.getComment().getNews().getId());
        } catch (Exception e) {
            log.error("Failed to process comment created event for commentId: {} on newsId: {}", 
                    event.getComment().getId(), event.getComment().getNews().getId(), e);
            // Note: Exception is logged but not re-thrown to avoid affecting the main transaction
            // In production, consider implementing dead letter queue or retry mechanisms
        }
    }
}