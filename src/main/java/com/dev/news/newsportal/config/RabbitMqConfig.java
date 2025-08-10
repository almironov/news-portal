package com.dev.news.newsportal.config;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ configuration for the News Portal application.
 * Automatically configures exchanges, queues, bindings, and RabbitTemplate for message publishing.
 * All RabbitMQ infrastructure is created automatically on application startup via RabbitAdmin.
 */
@Configuration
@RequiredArgsConstructor
public class RabbitMqConfig {

    private final NewsPortalProperties properties;

    // News Exchange Configuration
    @Bean
    public DirectExchange newsExchange() {
        return new DirectExchange(properties.getRabbitMq().getExchanges().getNews().getExchange());
    }

    // Comments Exchange Configuration
    @Bean
    public DirectExchange commentsExchange() {
        return new DirectExchange(properties.getRabbitMq().getExchanges().getComments().getExchange());
    }

    // Queue Configuration
    @Bean
    public Queue newsCreatedQueue() {
        return new Queue(properties.getRabbitMq().getExchanges().getNews().getBinding().getCreated().getQueue(), true);
    }

    @Bean
    public Queue newsUpdatedQueue() {
        return new Queue(properties.getRabbitMq().getExchanges().getNews().getBinding().getUpdated().getQueue(), true);
    }

    @Bean
    public Queue commentsCreatedQueue() {
        return new Queue(properties.getRabbitMq().getExchanges().getComments().getBinding().getCreated().getQueue(), true);
    }

    // Binding Configuration
    @Bean
    public Binding newsCreatedBinding() {
        return BindingBuilder.bind(newsCreatedQueue())
                .to(newsExchange())
                .with(properties.getRabbitMq().getExchanges().getNews().getBinding().getCreated().getKey());
    }

    @Bean
    public Binding newsUpdatedBinding() {
        return BindingBuilder.bind(newsUpdatedQueue())
                .to(newsExchange())
                .with(properties.getRabbitMq().getExchanges().getNews().getBinding().getUpdated().getKey());
    }

    @Bean
    public Binding commentsCreatedBinding() {
        return BindingBuilder.bind(commentsCreatedQueue())
                .to(commentsExchange())
                .with(properties.getRabbitMq().getExchanges().getComments().getBinding().getCreated().getKey());
    }

    // RabbitAdmin Configuration - automatically declares exchanges, queues, and bindings
    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }

    // RabbitTemplate Configuration
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(new Jackson2JsonMessageConverter());
        template.setMandatory(true);
        return template;
    }
}