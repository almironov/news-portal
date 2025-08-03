package com.dev.news.newsportal.controller;

import com.dev.news.newsportal.dto.request.CommentRequestDto;
import com.dev.news.newsportal.dto.response.CommentListItemDto;
import com.dev.news.newsportal.dto.response.CommentResponseDto;
import com.dev.news.newsportal.service.CommentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/comments")
class CommentController {
    
    private final CommentService commentService;
    
    CommentController(CommentService commentService) {
        this.commentService = commentService;
    }
    
    @GetMapping("/{id}")
    ResponseEntity<CommentResponseDto> getCommentById(@PathVariable Long id) {
        return ResponseEntity.ok(commentService.findById(id));
    }
    
    @GetMapping("/news/{newsId}")
    ResponseEntity<List<CommentListItemDto>> getCommentsByNews(@PathVariable Long newsId) {
        return ResponseEntity.ok(commentService.findByNews(newsId));
    }
    
    @PostMapping
    ResponseEntity<CommentResponseDto> createComment(@Valid @RequestBody CommentRequestDto commentRequestDto) {
        CommentResponseDto createdComment = commentService.create(commentRequestDto);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdComment.getId())
                .toUri();
        return ResponseEntity.created(location).body(createdComment);
    }
    
    @PutMapping("/{id}")
    ResponseEntity<CommentResponseDto> updateComment(
            @PathVariable Long id,
            @Valid @RequestBody CommentRequestDto commentRequestDto) {
        return ResponseEntity.ok(commentService.update(id, commentRequestDto));
    }
    
    @DeleteMapping("/{id}")
    ResponseEntity<Void> deleteComment(@PathVariable Long id) {
        commentService.delete(id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/{id}/replies")
    ResponseEntity<List<CommentListItemDto>> getCommentReplies(@PathVariable Long id) {
        return ResponseEntity.ok(commentService.findReplies(id));
    }
}