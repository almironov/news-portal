package com.dev.news.newsportal.service;

import com.dev.news.newsportal.entity.Comment;
import com.dev.news.newsportal.entity.News;
import com.dev.news.newsportal.exception.ResourceNotFoundException;
import com.dev.news.newsportal.mapper.entity.CommentEntityMapper;
import com.dev.news.newsportal.model.CommentModel;
import com.dev.news.newsportal.repository.CommentRepository;
import com.dev.news.newsportal.repository.NewsRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional
class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final NewsRepository newsRepository;
    private final CommentEntityMapper commentEntityMapper;

    CommentServiceImpl(CommentRepository commentRepository,
                       NewsRepository newsRepository,
                       CommentEntityMapper commentEntityMapper) {
        this.commentRepository = commentRepository;
        this.newsRepository = newsRepository;
        this.commentEntityMapper = commentEntityMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public CommentModel findById(Long id) {
        log.debug("Finding comment by id: {}", id);
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Comment not found with id: {}", id);
                    return new ResourceNotFoundException("Comment", "id", id);
                });
        log.info("Successfully retrieved comment with id: {}", id);
        return commentEntityMapper.toModel(comment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentModel> findByNews(Long newsId) {
        log.debug("Finding comments by news id: {}", newsId);
        News news = newsRepository.findById(newsId)
                .orElseThrow(() -> {
                    log.warn("News not found with id: {} when searching for comments", newsId);
                    return new ResourceNotFoundException("News", "id", newsId);
                });

        List<Comment> comments = commentRepository.findByNewsOrderByCreationDateDesc(news);
        log.info("Successfully retrieved {} comments for news id: {}", comments.size(), newsId);
        return commentEntityMapper.toModelList(comments);
    }

    @Override
    public CommentModel create(CommentModel commentModel) {
        log.debug("Creating new comment for news id: {} with parent comment id: {}", 
                commentModel.getNewsId(), commentModel.getParentCommentId());
        
        // Verify that the news exists
        News news = newsRepository.findById(commentModel.getNewsId())
                .orElseThrow(() -> {
                    log.error("News not found with id: {} when creating comment", commentModel.getNewsId());
                    return new ResourceNotFoundException("News", "id", commentModel.getNewsId());
                });

        // Verify that the parent comment exists if provided
        Comment parentComment = null;
        if (commentModel.getParentCommentId() != null) {
            log.debug("Validating parent comment with id: {}", commentModel.getParentCommentId());
            parentComment = commentRepository.findById(commentModel.getParentCommentId())
                    .orElseThrow(() -> {
                        log.error("Parent comment not found with id: {} when creating comment", commentModel.getParentCommentId());
                        return new ResourceNotFoundException("Comment", "id", commentModel.getParentCommentId());
                    });

            // Verify that the parent comment belongs to the same news
            if (!parentComment.getNews().getId().equals(commentModel.getNewsId())) {
                log.warn("Parent comment {} does not belong to news {}", 
                        commentModel.getParentCommentId(), commentModel.getNewsId());
                throw new IllegalArgumentException("Parent comment does not belong to the specified news");
            }
        }

        // Convert domain model to entity
        Comment comment = commentEntityMapper.toEntity(commentModel);
        comment.setNews(news);
        comment.setParentComment(parentComment);
        comment.setId(null); // Ensure it's a new entity

        // Save entity
        Comment savedComment = commentRepository.save(comment);
        log.info("Successfully created comment with id: {} for news id: {}", savedComment.getId(), commentModel.getNewsId());

        // Convert back to domain model and return
        return commentEntityMapper.toModel(savedComment);
    }

    @Override
    public CommentModel update(Long id, CommentModel commentModel) {
        log.debug("Updating comment with id: {}", id);
        
        // Find existing comment entity
        Comment existingComment = commentRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Comment not found with id: {} for update", id);
                    return new ResourceNotFoundException("Comment", "id", id);
                });

        // Use CommentEntityMapper to update properties, preserving id, creationDate, news, parentComment, authorNickname
        commentEntityMapper.updateEntity(existingComment, commentModel);

        // Save updated entity
        Comment updatedComment = commentRepository.save(existingComment);
        log.info("Successfully updated comment with id: {}", updatedComment.getId());

        // Convert back to domain model and return
        return commentEntityMapper.toModel(updatedComment);
    }

    @Override
    public void delete(Long id) {
        log.debug("Deleting comment with id: {}", id);
        if (!commentRepository.existsById(id)) {
            log.warn("Comment not found with id: {} for deletion", id);
            throw new ResourceNotFoundException("Comment", "id", id);
        }
        commentRepository.deleteById(id);
        log.info("Successfully deleted comment with id: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentModel> findReplies(Long parentCommentId) {
        log.debug("Finding replies for parent comment id: {}", parentCommentId);
        Comment parentComment = commentRepository.findById(parentCommentId)
                .orElseThrow(() -> {
                    log.warn("Parent comment not found with id: {} when searching for replies", parentCommentId);
                    return new ResourceNotFoundException("Comment", "id", parentCommentId);
                });

        List<Comment> replies = commentRepository.findByParentComment(parentComment);
        log.info("Successfully retrieved {} replies for parent comment id: {}", replies.size(), parentCommentId);
        return commentEntityMapper.toModelList(replies);
    }
}