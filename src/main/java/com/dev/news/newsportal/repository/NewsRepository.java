package com.dev.news.newsportal.repository;

import com.dev.news.newsportal.entity.News;
import com.dev.news.newsportal.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NewsRepository extends JpaRepository<News, Long> {
    
    List<News> findByAuthor(User author);
    
    List<News> findByTitleContainingIgnoreCase(String titlePart);
    
    List<News> findByCreationDateBetween(LocalDateTime start, LocalDateTime end);
    
    List<News> findByAuthorOrderByCreationDateDesc(User author);
}