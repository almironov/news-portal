package com.dev.news.newsportal.controller;

import com.dev.news.newsportal.api.comments.CommentsApi;
import com.dev.news.newsportal.api.model.comments.CommentListItem;
import com.dev.news.newsportal.api.model.comments.CommentRequest;
import com.dev.news.newsportal.api.model.comments.CommentResponse;
import com.dev.news.newsportal.mapper.api.CommentApiMapper;
import com.dev.news.newsportal.model.CommentModel;
import com.dev.news.newsportal.service.CommentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
class CommentController implements CommentsApi {

    private final CommentService commentService;
    private final CommentApiMapper commentApiMapper;

    CommentController(CommentService commentService, CommentApiMapper commentApiMapper) {
        this.commentService = commentService;
        this.commentApiMapper = commentApiMapper;
    }

    @Override
    public ResponseEntity<CommentResponse> getCommentById(Long id) {
        CommentModel commentModel = commentService.findById(id);
        CommentResponse commentResponse = commentApiMapper.toResponse(commentModel);
        return ResponseEntity.ok(commentResponse);
    }

    @Override
    public ResponseEntity<List<CommentListItem>> getCommentsByNews(Long newsId) {
        List<CommentModel> commentModels = commentService.findByNews(newsId);
        List<CommentListItem> commentListItems = commentApiMapper.toListItemList(commentModels);
        return ResponseEntity.ok(commentListItems);
    }

    @Override
    public ResponseEntity<CommentResponse> createComment(CommentRequest commentRequest) {
        // Convert DTO to domain model
        CommentModel commentModel = commentApiMapper.toModel(commentRequest);

        // Create comment
        CommentModel createdCommentModel = commentService.create(commentModel);

        // Convert back to DTO
        CommentResponse commentResponse = commentApiMapper.toResponse(createdCommentModel);

        // Create location header
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(commentResponse.getId())
                .toUri();

        return ResponseEntity.created(location).body(commentResponse);
    }

    @Override
    public ResponseEntity<CommentResponse> updateComment(Long id, CommentRequest commentRequest) {
        // Convert DTO to domain model
        CommentModel commentModel = commentApiMapper.toModel(commentRequest);

        // Update comment
        CommentModel updatedCommentModel = commentService.update(id, commentModel);

        // Convert back to DTO
        CommentResponse commentResponse = commentApiMapper.toResponse(updatedCommentModel);

        return ResponseEntity.ok(commentResponse);
    }

    @Override
    public ResponseEntity<Void> deleteComment(Long id) {
        commentService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<List<CommentListItem>> getCommentReplies(Long id) {
        List<CommentModel> commentModels = commentService.findReplies(id);
        List<CommentListItem> commentListItems = commentApiMapper.toListItemList(commentModels);
        return ResponseEntity.ok(commentListItems);
    }
}