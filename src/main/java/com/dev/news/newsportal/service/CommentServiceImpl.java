package com.dev.news.newsportal.service;

import com.dev.news.newsportal.entity.Comment;
import com.dev.news.newsportal.entity.News;
import com.dev.news.newsportal.exception.ResourceNotFoundException;
import com.dev.news.newsportal.mapper.entity.CommentEntityMapper;
import com.dev.news.newsportal.model.CommentModel;
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
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", id));
        return commentEntityMapper.toModel(comment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentModel> findByNews(Long newsId) {
        News news = newsRepository.findById(newsId)
                .orElseThrow(() -> new ResourceNotFoundException("News", "id", newsId));

        List<Comment> comments = commentRepository.findByNewsOrderByCreationDateDesc(news);
        return commentEntityMapper.toModelList(comments);
    }

    @Override
    public CommentModel create(CommentModel commentModel) {
        // Verify that the news exists
        News news = newsRepository.findById(commentModel.getNewsId())
                .orElseThrow(() -> new ResourceNotFoundException("News", "id", commentModel.getNewsId()));

        // Verify that the parent comment exists if provided
        Comment parentComment = null;
        if (commentModel.getParentCommentId() != null) {
            parentComment = commentRepository.findById(commentModel.getParentCommentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", commentModel.getParentCommentId()));

            // Verify that the parent comment belongs to the same news
            if (!parentComment.getNews().getId().equals(commentModel.getNewsId())) {
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

        // Convert back to domain model and return
        return commentEntityMapper.toModel(savedComment);
    }

    @Override
    public CommentModel update(Long id, CommentModel commentModel) {
        // Find existing comment entity
        Comment existingComment = commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", id));

        // Only update the text, keep the other fields as they are
        existingComment.setText(commentModel.getText());

        // Save updated entity
        Comment updatedComment = commentRepository.save(existingComment);

        // Convert back to domain model and return
        return commentEntityMapper.toModel(updatedComment);
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
    public List<CommentModel> findReplies(Long parentCommentId) {
        Comment parentComment = commentRepository.findById(parentCommentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", parentCommentId));

        List<Comment> replies = commentRepository.findByParentComment(parentComment);
        return commentEntityMapper.toModelList(replies);
    }
}