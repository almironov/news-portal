package com.dev.news.newsportal.event;

import com.dev.news.newsportal.entity.Comment;
import org.springframework.context.ApplicationEvent;

/**
 * Spring application event published when a comment is created.
 * This event is published within the transaction and handled by @TransactionalEventListener.
 */
public class CommentCreatedApplicationEvent extends ApplicationEvent {

    private final Comment comment;

    public CommentCreatedApplicationEvent(Object source, Comment comment) {
        super(source);
        this.comment = comment;
    }

    public Comment getComment() {
        return comment;
    }
}