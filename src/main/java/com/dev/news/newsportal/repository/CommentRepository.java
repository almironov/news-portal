package com.dev.news.newsportal.repository;

import com.dev.news.newsportal.entity.Comment;
import com.dev.news.newsportal.entity.News;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    
    List<Comment> findByNews(News news);
    
    List<Comment> findByNewsOrderByCreationDateDesc(News news);
    
    List<Comment> findByParentCommentIsNull();
    
    List<Comment> findByParentComment(Comment parentComment);
    
    List<Comment> findByAuthorNickname(String authorNickname);
    
    long countByNews(News news);
}