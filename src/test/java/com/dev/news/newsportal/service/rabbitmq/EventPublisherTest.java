package com.dev.news.newsportal.service.rabbitmq;

import com.dev.news.newsportal.config.NewsPortalProperties;
import com.dev.news.newsportal.event.CommentCreatedEvent;
import com.dev.news.newsportal.event.NewsCreatedEvent;
import com.dev.news.newsportal.event.NewsUpdatedEvent;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for EventPublisher service.
 * Tests RabbitMQ message publishing functionality with proper error handling.
 */
@ExtendWith(MockitoExtension.class)
class EventPublisherTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private NewsPortalProperties properties;

    private MeterRegistry meterRegistry;

    @Mock
    private NewsPortalProperties.RabbitMq rabbitMq;

    @Mock
    private NewsPortalProperties.RabbitMq.Exchanges exchanges;

    @Mock
    private NewsPortalProperties.RabbitMq.Exchanges.NewsExchange newsExchange;

    @Mock
    private NewsPortalProperties.RabbitMq.Exchanges.NewsExchange.Binding newsBinding;

    @Mock
    private NewsPortalProperties.RabbitMq.Exchanges.NewsExchange.Binding.Created newsCreated;

    @Mock
    private NewsPortalProperties.RabbitMq.Exchanges.NewsExchange.Binding.Updated newsUpdated;

    @Mock
    private NewsPortalProperties.RabbitMq.Exchanges.CommentsExchange commentsExchange;

    @Mock
    private NewsPortalProperties.RabbitMq.Exchanges.CommentsExchange.Binding commentsBinding;

    @Mock
    private NewsPortalProperties.RabbitMq.Exchanges.CommentsExchange.Binding.Created commentsCreated;

    private EventPublisher eventPublisher;

    private NewsCreatedEvent newsCreatedEvent;
    private NewsUpdatedEvent newsUpdatedEvent;
    private CommentCreatedEvent commentCreatedEvent;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        eventPublisher = new EventPublisher(rabbitTemplate, properties, meterRegistry);

        // Set up property mocks with lenient stubbing to avoid unnecessary stubbing errors
        lenient().when(properties.getRabbitMq()).thenReturn(rabbitMq);
        lenient().when(rabbitMq.getExchanges()).thenReturn(exchanges);
        lenient().when(exchanges.getNews()).thenReturn(newsExchange);
        lenient().when(exchanges.getComments()).thenReturn(commentsExchange);
        lenient().when(newsExchange.getExchange()).thenReturn("exchange.news");
        lenient().when(newsExchange.getBinding()).thenReturn(newsBinding);
        lenient().when(newsBinding.getCreated()).thenReturn(newsCreated);
        lenient().when(newsBinding.getUpdated()).thenReturn(newsUpdated);
        lenient().when(newsCreated.getKey()).thenReturn("news.created");
        lenient().when(newsUpdated.getKey()).thenReturn("news.updated");
        lenient().when(commentsExchange.getExchange()).thenReturn("exchange.comments");
        lenient().when(commentsExchange.getBinding()).thenReturn(commentsBinding);
        lenient().when(commentsBinding.getCreated()).thenReturn(commentsCreated);
        lenient().when(commentsCreated.getKey()).thenReturn("comments.created");

        // Create test events
        LocalDateTime eventTime = LocalDateTime.of(2024, 1, 15, 10, 30, 0);
        
        newsCreatedEvent = new NewsCreatedEvent(
                1L,
                "Test News",
                "Test news content",
                "https://example.com/image.jpg",
                eventTime.minusHours(1),
                100L,
                "testuser",
                eventTime
        );

        newsUpdatedEvent = new NewsUpdatedEvent(
                1L,
                "Updated Test News",
                "Updated test news content",
                "https://example.com/updated-image.jpg",
                eventTime.minusHours(1),
                eventTime.minusMinutes(30),
                100L,
                "testuser",
                eventTime
        );

        commentCreatedEvent = new CommentCreatedEvent(
                2L,
                "Test comment",
                eventTime.minusMinutes(15),
                "commentuser",
                1L,
                "Test News",
                null,
                eventTime
        );
    }

    @Test
    void publishNewsCreatedEvent_shouldPublishSuccessfully() {
        // When
        eventPublisher.publishNewsCreatedEvent(newsCreatedEvent);

        // Then
        ArgumentCaptor<String> exchangeCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> routingKeyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<NewsCreatedEvent> eventCaptor = ArgumentCaptor.forClass(NewsCreatedEvent.class);
        ArgumentCaptor<CorrelationData> correlationCaptor = ArgumentCaptor.forClass(CorrelationData.class);

        verify(rabbitTemplate).convertAndSend(
                exchangeCaptor.capture(),
                routingKeyCaptor.capture(),
                eventCaptor.capture(),
                correlationCaptor.capture()
        );

        assertThat(exchangeCaptor.getValue()).isEqualTo("exchange.news");
        assertThat(routingKeyCaptor.getValue()).isEqualTo("news.created");
        assertThat(eventCaptor.getValue()).isEqualTo(newsCreatedEvent);
        assertThat(correlationCaptor.getValue()).isNotNull();
        assertThat(correlationCaptor.getValue().getId()).isNotNull();
    }

    @Test
    void publishNewsUpdatedEvent_shouldPublishSuccessfully() {
        // When
        eventPublisher.publishNewsUpdatedEvent(newsUpdatedEvent);

        // Then
        ArgumentCaptor<String> exchangeCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> routingKeyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<NewsUpdatedEvent> eventCaptor = ArgumentCaptor.forClass(NewsUpdatedEvent.class);
        ArgumentCaptor<CorrelationData> correlationCaptor = ArgumentCaptor.forClass(CorrelationData.class);

        verify(rabbitTemplate).convertAndSend(
                exchangeCaptor.capture(),
                routingKeyCaptor.capture(),
                eventCaptor.capture(),
                correlationCaptor.capture()
        );

        assertThat(exchangeCaptor.getValue()).isEqualTo("exchange.news");
        assertThat(routingKeyCaptor.getValue()).isEqualTo("news.updated");
        assertThat(eventCaptor.getValue()).isEqualTo(newsUpdatedEvent);
        assertThat(correlationCaptor.getValue()).isNotNull();
        assertThat(correlationCaptor.getValue().getId()).isNotNull();
    }

    @Test
    void publishCommentCreatedEvent_shouldPublishSuccessfully() {
        // When
        eventPublisher.publishCommentCreatedEvent(commentCreatedEvent);

        // Then
        ArgumentCaptor<String> exchangeCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> routingKeyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<CommentCreatedEvent> eventCaptor = ArgumentCaptor.forClass(CommentCreatedEvent.class);
        ArgumentCaptor<CorrelationData> correlationCaptor = ArgumentCaptor.forClass(CorrelationData.class);

        verify(rabbitTemplate).convertAndSend(
                exchangeCaptor.capture(),
                routingKeyCaptor.capture(),
                eventCaptor.capture(),
                correlationCaptor.capture()
        );

        assertThat(exchangeCaptor.getValue()).isEqualTo("exchange.comments");
        assertThat(routingKeyCaptor.getValue()).isEqualTo("comments.created");
        assertThat(eventCaptor.getValue()).isEqualTo(commentCreatedEvent);
        assertThat(correlationCaptor.getValue()).isNotNull();
        assertThat(correlationCaptor.getValue().getId()).isNotNull();
    }

    @Test
    void publishNewsCreatedEvent_whenRabbitTemplateThrowsException_shouldRethrowException() {
        // Given
        RuntimeException rabbitException = new RuntimeException("RabbitMQ connection failed");
        doThrow(rabbitException).when(rabbitTemplate).convertAndSend(
                any(String.class), any(String.class), any(NewsCreatedEvent.class), any(CorrelationData.class)
        );

        // When/Then
        assertThatThrownBy(() -> eventPublisher.publishNewsCreatedEvent(newsCreatedEvent))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("RabbitMQ connection failed");

        verify(rabbitTemplate).convertAndSend(
                eq("exchange.news"),
                eq("news.created"),
                eq(newsCreatedEvent),
                any(CorrelationData.class)
        );
    }

    @Test
    void publishNewsUpdatedEvent_whenRabbitTemplateThrowsException_shouldRethrowException() {
        // Given
        RuntimeException rabbitException = new RuntimeException("Message serialization failed");
        doThrow(rabbitException).when(rabbitTemplate).convertAndSend(
                any(String.class), any(String.class), any(NewsUpdatedEvent.class), any(CorrelationData.class)
        );

        // When/Then
        assertThatThrownBy(() -> eventPublisher.publishNewsUpdatedEvent(newsUpdatedEvent))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Message serialization failed");

        verify(rabbitTemplate).convertAndSend(
                eq("exchange.news"),
                eq("news.updated"),
                eq(newsUpdatedEvent),
                any(CorrelationData.class)
        );
    }

    @Test
    void publishCommentCreatedEvent_whenRabbitTemplateThrowsException_shouldRethrowException() {
        // Given
        RuntimeException rabbitException = new RuntimeException("Exchange not found");
        doThrow(rabbitException).when(rabbitTemplate).convertAndSend(
                any(String.class), any(String.class), any(CommentCreatedEvent.class), any(CorrelationData.class)
        );

        // When/Then
        assertThatThrownBy(() -> eventPublisher.publishCommentCreatedEvent(commentCreatedEvent))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Exchange not found");

        verify(rabbitTemplate).convertAndSend(
                eq("exchange.comments"),
                eq("comments.created"),
                eq(commentCreatedEvent),
                any(CorrelationData.class)
        );
    }

    @Test
    void publishNewsCreatedEvent_shouldGenerateUniqueCorrelationIds() {
        // When
        eventPublisher.publishNewsCreatedEvent(newsCreatedEvent);
        eventPublisher.publishNewsCreatedEvent(newsCreatedEvent);

        // Then
        ArgumentCaptor<CorrelationData> correlationCaptor = ArgumentCaptor.forClass(CorrelationData.class);
        verify(rabbitTemplate, times(2)).convertAndSend(
                any(String.class), any(String.class), any(NewsCreatedEvent.class), correlationCaptor.capture()
        );

        var correlationIds = correlationCaptor.getAllValues();
        assertThat(correlationIds).hasSize(2);
        assertThat(correlationIds.get(0).getId()).isNotEqualTo(correlationIds.get(1).getId());
        assertThat(correlationIds.get(0).getId()).isNotNull();
        assertThat(correlationIds.get(1).getId()).isNotNull();
    }

    @Test
    void publishNewsUpdatedEvent_shouldGenerateUniqueCorrelationIds() {
        // When
        eventPublisher.publishNewsUpdatedEvent(newsUpdatedEvent);
        eventPublisher.publishNewsUpdatedEvent(newsUpdatedEvent);

        // Then
        ArgumentCaptor<CorrelationData> correlationCaptor = ArgumentCaptor.forClass(CorrelationData.class);
        verify(rabbitTemplate, times(2)).convertAndSend(
                any(String.class), any(String.class), any(NewsUpdatedEvent.class), correlationCaptor.capture()
        );

        var correlationIds = correlationCaptor.getAllValues();
        assertThat(correlationIds).hasSize(2);
        assertThat(correlationIds.get(0).getId()).isNotEqualTo(correlationIds.get(1).getId());
        assertThat(correlationIds.get(0).getId()).isNotNull();
        assertThat(correlationIds.get(1).getId()).isNotNull();
    }

    @Test
    void publishCommentCreatedEvent_shouldGenerateUniqueCorrelationIds() {
        // When
        eventPublisher.publishCommentCreatedEvent(commentCreatedEvent);
        eventPublisher.publishCommentCreatedEvent(commentCreatedEvent);

        // Then
        ArgumentCaptor<CorrelationData> correlationCaptor = ArgumentCaptor.forClass(CorrelationData.class);
        verify(rabbitTemplate, times(2)).convertAndSend(
                any(String.class), any(String.class), any(CommentCreatedEvent.class), correlationCaptor.capture()
        );

        var correlationIds = correlationCaptor.getAllValues();
        assertThat(correlationIds).hasSize(2);
        assertThat(correlationIds.get(0).getId()).isNotEqualTo(correlationIds.get(1).getId());
        assertThat(correlationIds.get(0).getId()).isNotNull();
        assertThat(correlationIds.get(1).getId()).isNotNull();
    }

    @Test
    void publishNewsCreatedEvent_withCommentCreatedEvent_shouldMapCorrectExchangeAndRoutingKey() {
        // Given
        CommentCreatedEvent commentWithParent = new CommentCreatedEvent(
                3L,
                "Reply comment",
                LocalDateTime.now(),
                "replyuser",
                1L,
                "Test News",
                2L, // parent comment ID
                LocalDateTime.now()
        );

        // When
        eventPublisher.publishCommentCreatedEvent(commentWithParent);

        // Then
        verify(rabbitTemplate).convertAndSend(
                eq("exchange.comments"),
                eq("comments.created"),
                eq(commentWithParent),
                any(CorrelationData.class)
        );
    }

    @Test
    void allPublishMethods_shouldUseCorrectConfigurationProperties() {
        // When
        eventPublisher.publishNewsCreatedEvent(newsCreatedEvent);
        eventPublisher.publishNewsUpdatedEvent(newsUpdatedEvent);
        eventPublisher.publishCommentCreatedEvent(commentCreatedEvent);

        // Then - Verify all configuration properties were accessed correctly
        // Each publish method calls getRabbitMq() twice (for exchange and routing key)
        verify(properties, times(6)).getRabbitMq();
        verify(rabbitMq, times(6)).getExchanges();
        verify(exchanges, times(4)).getNews(); // 2 calls per news method × 2 methods
        verify(exchanges, times(2)).getComments(); // 2 calls per comment method × 1 method
        verify(newsExchange, times(2)).getExchange();
        verify(newsExchange, times(2)).getBinding();
        verify(newsBinding, times(1)).getCreated();
        verify(newsBinding, times(1)).getUpdated();
        verify(newsCreated, times(1)).getKey();
        verify(newsUpdated, times(1)).getKey();
        verify(commentsExchange, times(1)).getExchange();
        verify(commentsExchange, times(1)).getBinding();
        verify(commentsBinding, times(1)).getCreated();
        verify(commentsCreated, times(1)).getKey();
    }
}