package com.dev.news.newsportal.controller;

import com.dev.news.newsportal.dto.request.NewsRequestDto;
import com.dev.news.newsportal.dto.response.NewsListItemDto;
import com.dev.news.newsportal.dto.response.NewsResponseDto;
import com.dev.news.newsportal.service.NewsService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/news")
class NewsController {
    
    private final NewsService newsService;
    
    NewsController(NewsService newsService) {
        this.newsService = newsService;
    }
    
    @GetMapping
    ResponseEntity<List<NewsListItemDto>> getAllNews() {
        return ResponseEntity.ok(newsService.findAll());
    }
    
    @GetMapping("/{id}")
    ResponseEntity<NewsResponseDto> getNewsById(@PathVariable Long id) {
        return ResponseEntity.ok(newsService.findById(id));
    }
    
    @PostMapping
    ResponseEntity<NewsResponseDto> createNews(@Valid @RequestBody NewsRequestDto newsRequestDto) {
        NewsResponseDto createdNews = newsService.create(newsRequestDto);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdNews.getId())
                .toUri();
        return ResponseEntity.created(location).body(createdNews);
    }
    
    @PutMapping("/{id}")
    ResponseEntity<NewsResponseDto> updateNews(
            @PathVariable Long id,
            @Valid @RequestBody NewsRequestDto newsRequestDto) {
        return ResponseEntity.ok(newsService.update(id, newsRequestDto));
    }
    
    @DeleteMapping("/{id}")
    ResponseEntity<Void> deleteNews(@PathVariable Long id) {
        newsService.delete(id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/author/{authorId}")
    ResponseEntity<List<NewsListItemDto>> getNewsByAuthor(@PathVariable Long authorId) {
        return ResponseEntity.ok(newsService.findByAuthor(authorId));
    }
    
    @GetMapping("/search")
    ResponseEntity<List<NewsListItemDto>> searchNewsByTitle(@RequestParam String title) {
        return ResponseEntity.ok(newsService.findByTitle(title));
    }
}