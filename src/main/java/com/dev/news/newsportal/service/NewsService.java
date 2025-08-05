package com.dev.news.newsportal.service;

import com.dev.news.newsportal.model.NewsModel;

import java.util.List;

public interface NewsService {

    NewsModel findById(Long id);

    List<NewsModel> findAll();

    NewsModel create(NewsModel newsModel);

    NewsModel update(Long id, NewsModel newsModel);

    void delete(Long id);

    List<NewsModel> findByAuthor(Long authorId);

    List<NewsModel> findByTitle(String titlePart);
}