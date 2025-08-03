package com.dev.news.newsportal.service;

import com.dev.news.newsportal.dto.request.CommentRequestDto;
import com.dev.news.newsportal.dto.response.CommentListItemDto;
import com.dev.news.newsportal.dto.response.CommentResponseDto;
import com.dev.news.newsportal.entity.Comment;
import com.dev.news.newsportal.entity.News;
import com.dev.news.newsportal.exception.ResourceNotFoundException;
import com.dev.news.newsportal.mapper.CommentMapper;
import com.dev.news.newsportal.repository.CommentRepository;
import com.dev.news.newsportal.repository.NewsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
class CommentServiceImpl implements CommentService {
    
    private final CommentRepository commentRepository;
    private final NewsRepository newsRepository;
    private final CommentMapper commentMapper;
    
    CommentServiceImpl(CommentRepository commentRepository, 
                      NewsRepository newsRepository, 
                      CommentMapper commentMapper) {
        this.commentRepository = commentRepository;
        this.newsRepository = newsRepository;
        this.commentMapper = commentMapper;
    }
    
    @Override
    @Transactional(readOnly = true)
    public CommentResponseDto findById(Long id) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", id));
        return commentMapper.toResponseDto(comment);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<CommentListItemDto> findByNews(Long newsId) {
        News news = newsRepository.findById(newsId)
                .orElseThrow(() -> new ResourceNotFoundException("News", "id", newsId));
        
        return commentMapper.toListItemDtoList(commentRepository.findByNewsOrderByCreationDateDesc(news));
    }
    
    @Override
    public CommentResponseDto create(CommentRequestDto dto) {
        // Verify that the news exists
        News news = newsRepository.findById(dto.getNewsId())
                .orElseThrow(() -> new ResourceNotFoundException("News", "id", dto.getNewsId()));
        
        // Verify that the parent comment exists if provided
        Comment parentComment = null;
        if (dto.getParentCommentId() != null) {
            parentComment = commentRepository.findById(dto.getParentCommentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", dto.getParentCommentId()));
            
            // Verify that the parent comment belongs to the same news
            if (!parentComment.getNews().getId().equals(dto.getNewsId())) {
                throw new IllegalArgumentException("Parent comment does not belong to the specified news");
            }
        }
        
        Comment comment = commentMapper.toEntity(dto);
        comment.setNews(news);
        comment.setParentComment(parentComment);
        
        Comment savedComment = commentRepository.save(comment);
        return commentMapper.toResponseDto(savedComment);
    }
    
    @Override
    public CommentResponseDto update(Long id, CommentRequestDto dto) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", id));
        
        // Only update the text, keep the other fields as they are
        comment.setText(dto.getText());
        
        Comment updatedComment = commentRepository.save(comment);
        return commentMapper.toResponseDto(updatedComment);
    }
    
    @Override
    public void delete(Long id) {
        if (!commentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Comment", "id", id);
        }
        commentRepository.deleteById(id);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<CommentListItemDto> findReplies(Long parentCommentId) {
        Comment parentComment = commentRepository.findById(parentCommentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", parentCommentId));
        
        return commentMapper.toListItemDtoList(commentRepository.findByParentComment(parentComment));
    }
}