package com.dev.news.newsportal.event;

import com.dev.news.newsportal.entity.News;
import org.springframework.context.ApplicationEvent;

/**
 * Spring application event published when a news article is updated.
 * This event is published within the transaction and handled by @TransactionalEventListener.
 */
public class NewsUpdatedApplicationEvent extends ApplicationEvent {

    private final News news;

    public NewsUpdatedApplicationEvent(Object source, News news) {
        super(source);
        this.news = news;
    }

    public News getNews() {
        return news;
    }
}