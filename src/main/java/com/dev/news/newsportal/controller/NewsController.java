package com.dev.news.newsportal.controller;

import com.dev.news.newsportal.api.model.news.NewsListItem;
import com.dev.news.newsportal.api.model.news.NewsRequest;
import com.dev.news.newsportal.api.model.news.NewsResponse;
import com.dev.news.newsportal.api.model.news.PagedNewsListResponse;
import com.dev.news.newsportal.api.news.NewsApi;
import com.dev.news.newsportal.mapper.api.NewsApiMapper;
import com.dev.news.newsportal.model.NewsModel;
import com.dev.news.newsportal.model.UserModel;
import com.dev.news.newsportal.service.NewsService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@RestController
class NewsController implements NewsApi {

    private final NewsService newsService;
    private final NewsApiMapper newsApiMapper;

    NewsController(NewsService newsService, NewsApiMapper newsApiMapper) {
        this.newsService = newsService;
        this.newsApiMapper = newsApiMapper;
    }

    @Override
    public ResponseEntity<PagedNewsListResponse> getAllNews(Integer page, Integer size, List<String> sort) {
        // Create Pageable with default values if parameters are null
        int pageNumber = page != null ? page : 0;
        int pageSize = size != null ? size : 10;
        
        // Create Sort object from sort parameters
        Sort sortObj = Sort.by(Sort.Direction.DESC, "creationDate"); // default sort
        if (sort != null && !sort.isEmpty()) {
            List<Sort.Order> orders = new ArrayList<>();
            for (String sortParam : sort) {
                String[] parts = sortParam.split(",");
                String property = parts[0].trim();
                Sort.Direction direction = parts.length > 1 && "asc".equalsIgnoreCase(parts[1].trim()) 
                        ? Sort.Direction.ASC : Sort.Direction.DESC;
                orders.add(new Sort.Order(direction, property));
            }
            sortObj = Sort.by(orders);
        }
        
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sortObj);
        Page<NewsModel> newsPage = newsService.findAll(pageable);
        PagedNewsListResponse response = newsApiMapper.toPagedResponse(newsPage);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<NewsResponse> getNewsById(Long id) {
        NewsModel newsModel = newsService.findById(id);
        NewsResponse newsResponse = newsApiMapper.toResponse(newsModel);
        return ResponseEntity.ok(newsResponse);
    }

    @Override
    public ResponseEntity<NewsResponse> createNews(NewsRequest newsRequest) {
        // Convert DTO to domain model
        NewsModel newsModel = newsApiMapper.toModel(newsRequest);

        // Set author from authorId
        UserModel author = UserModel.builder()
                .id(newsRequest.getAuthorId())
                .build();
        newsModel.setAuthor(author);

        // Create news
        NewsModel createdNewsModel = newsService.create(newsModel);

        // Convert back to DTO
        NewsResponse newsResponse = newsApiMapper.toResponse(createdNewsModel);

        // Create location header
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(newsResponse.getId())
                .toUri();

        return ResponseEntity.created(location).body(newsResponse);
    }

    @Override
    public ResponseEntity<NewsResponse> updateNews(Long id, NewsRequest newsRequest) {
        // Convert DTO to domain model
        NewsModel newsModel = newsApiMapper.toModel(newsRequest);

        // Set author from authorId
        UserModel author = UserModel.builder()
                .id(newsRequest.getAuthorId())
                .build();
        newsModel.setAuthor(author);

        // Update news
        NewsModel updatedNewsModel = newsService.update(id, newsModel);

        // Convert back to DTO
        NewsResponse newsResponse = newsApiMapper.toResponse(updatedNewsModel);

        return ResponseEntity.ok(newsResponse);
    }

    @Override
    public ResponseEntity<Void> deleteNews(Long id) {
        newsService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<List<NewsListItem>> getNewsByAuthor(Long authorId) {
        List<NewsModel> newsModels = newsService.findByAuthor(authorId);
        List<NewsListItem> newsListItems = newsApiMapper.toListItemList(newsModels);
        return ResponseEntity.ok(newsListItems);
    }

    @Override
    public ResponseEntity<List<NewsListItem>> searchNewsByTitle(String title) {
        List<NewsModel> newsModels = newsService.findByTitle(title);
        List<NewsListItem> newsListItems = newsApiMapper.toListItemList(newsModels);
        return ResponseEntity.ok(newsListItems);
    }
}