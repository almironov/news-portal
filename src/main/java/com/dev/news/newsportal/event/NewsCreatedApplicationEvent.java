package com.dev.news.newsportal.event;

import com.dev.news.newsportal.entity.News;
import org.springframework.context.ApplicationEvent;

/**
 * Spring application event published when a news article is created.
 * This event is published within the transaction and handled by @TransactionalEventListener.
 */
public class NewsCreatedApplicationEvent extends ApplicationEvent {

    private final News news;

    public NewsCreatedApplicationEvent(Object source, News news) {
        super(source);
        this.news = news;
    }

    public News getNews() {
        return news;
    }
}