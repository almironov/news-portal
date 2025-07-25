package com.dev.news.newsportal.repository;

import com.dev.news.newsportal.entity.Comment;
import com.dev.news.newsportal.entity.News;
import com.dev.news.newsportal.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class CommentRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CommentRepository commentRepository;

    private User author;
    private News news;

    @BeforeEach
    public void setup() {
        // Create a user to be the author of news
        author = User.builder()
                .nickname("newsauthor")
                .email("author@example.com")
                .role("REGISTERED_USER")
                .build();
        
        entityManager.persist(author);
        
        // Create a news item for comments
        news = News.builder()
                .title("Test News")
                .text("Test news content")
                .author(author)
                .build();
        
        entityManager.persist(news);
        entityManager.flush();
    }

    @Test
    public void testCreateAndFindById() {
        // Create a comment
        Comment comment = Comment.builder()
                .text("This is a test comment.")
                .authorNickname("guest")
                .news(news)
                .build();

        // Save the comment
        Comment savedComment = commentRepository.save(comment);

        // Find the comment by ID
        Optional<Comment> foundComment = commentRepository.findById(savedComment.getId());

        // Assert that the comment was found and has the correct properties
        assertThat(foundComment).isPresent();
        assertThat(foundComment.get().getText()).isEqualTo("This is a test comment.");
        assertThat(foundComment.get().getAuthorNickname()).isEqualTo("guest");
        assertThat(foundComment.get().getNews().getId()).isEqualTo(news.getId());
        assertThat(foundComment.get().getCreationDate()).isNotNull();
    }

    @Test
    public void testFindByNews() {
        // Create multiple comments for the same news
        Comment comment1 = Comment.builder()
                .text("First comment")
                .authorNickname("user1")
                .news(news)
                .build();

        Comment comment2 = Comment.builder()
                .text("Second comment")
                .authorNickname("user2")
                .news(news)
                .build();

        entityManager.persist(comment1);
        entityManager.persist(comment2);
        entityManager.flush();

        // Find comments by news
        List<Comment> comments = commentRepository.findByNews(news);

        // Assert that both comments were found
        assertThat(comments).hasSize(2);
        assertThat(comments).extracting(Comment::getText).containsExactlyInAnyOrder("First comment", "Second comment");
    }

    @Test
    public void testFindByNewsOrderByCreationDateDesc() {
        // Create multiple comments for the same news
        Comment comment1 = Comment.builder()
                .text("First comment")
                .authorNickname("user1")
                .news(news)
                .build();

        entityManager.persist(comment1);
        entityManager.flush();
        
        // Add a small delay to ensure different creation timestamps
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Comment comment2 = Comment.builder()
                .text("Second comment")
                .authorNickname("user2")
                .news(news)
                .build();

        entityManager.persist(comment2);
        entityManager.flush();

        // Find comments by news ordered by creation date (newest first)
        List<Comment> comments = commentRepository.findByNewsOrderByCreationDateDesc(news);

        // Assert that the comments are in the correct order
        assertThat(comments).hasSize(2);
        assertThat(comments.get(0).getText()).isEqualTo("Second comment");
        assertThat(comments.get(1).getText()).isEqualTo("First comment");
    }

    @Test
    public void testFindByParentCommentIsNull() {
        // Create root comments (no parent)
        Comment rootComment1 = Comment.builder()
                .text("Root comment 1")
                .authorNickname("user1")
                .news(news)
                .build();

        Comment rootComment2 = Comment.builder()
                .text("Root comment 2")
                .authorNickname("user2")
                .news(news)
                .build();

        entityManager.persist(rootComment1);
        entityManager.persist(rootComment2);
        
        // Create a reply comment
        Comment replyComment = Comment.builder()
                .text("Reply to root comment 1")
                .authorNickname("user3")
                .news(news)
                .parentComment(rootComment1)
                .build();
        
        entityManager.persist(replyComment);
        entityManager.flush();

        // Find root comments
        List<Comment> rootComments = commentRepository.findByParentCommentIsNull();

        // Assert that only the root comments were found
        assertThat(rootComments).hasSize(2);
        assertThat(rootComments).extracting(Comment::getText).containsExactlyInAnyOrder("Root comment 1", "Root comment 2");
    }

    @Test
    public void testFindByParentComment() {
        // Create a root comment
        Comment rootComment = Comment.builder()
                .text("Root comment")
                .authorNickname("user1")
                .news(news)
                .build();

        entityManager.persist(rootComment);
        
        // Create multiple replies to the root comment
        Comment reply1 = Comment.builder()
                .text("First reply")
                .authorNickname("user2")
                .news(news)
                .parentComment(rootComment)
                .build();

        Comment reply2 = Comment.builder()
                .text("Second reply")
                .authorNickname("user3")
                .news(news)
                .parentComment(rootComment)
                .build();
        
        entityManager.persist(reply1);
        entityManager.persist(reply2);
        entityManager.flush();

        // Find replies to the root comment
        List<Comment> replies = commentRepository.findByParentComment(rootComment);

        // Assert that both replies were found
        assertThat(replies).hasSize(2);
        assertThat(replies).extracting(Comment::getText).containsExactlyInAnyOrder("First reply", "Second reply");
    }

    @Test
    public void testFindByAuthorNickname() {
        // Create comments by different authors
        Comment comment1 = Comment.builder()
                .text("Comment by user1")
                .authorNickname("user1")
                .news(news)
                .build();

        Comment comment2 = Comment.builder()
                .text("Another comment by user1")
                .authorNickname("user1")
                .news(news)
                .build();

        Comment comment3 = Comment.builder()
                .text("Comment by user2")
                .authorNickname("user2")
                .news(news)
                .build();
        
        entityManager.persist(comment1);
        entityManager.persist(comment2);
        entityManager.persist(comment3);
        entityManager.flush();

        // Find comments by author nickname
        List<Comment> userComments = commentRepository.findByAuthorNickname("user1");

        // Assert that only comments by user1 were found
        assertThat(userComments).hasSize(2);
        assertThat(userComments).extracting(Comment::getText).containsExactlyInAnyOrder("Comment by user1", "Another comment by user1");
    }

    @Test
    public void testCountByNews() {
        // Create multiple comments for the same news
        Comment comment1 = Comment.builder()
                .text("First comment")
                .authorNickname("user1")
                .news(news)
                .build();

        Comment comment2 = Comment.builder()
                .text("Second comment")
                .authorNickname("user2")
                .news(news)
                .build();

        Comment comment3 = Comment.builder()
                .text("Third comment")
                .authorNickname("user3")
                .news(news)
                .build();
        
        entityManager.persist(comment1);
        entityManager.persist(comment2);
        entityManager.persist(comment3);
        entityManager.flush();

        // Count comments for the news
        long count = commentRepository.countByNews(news);

        // Assert that the count is correct
        assertThat(count).isEqualTo(3);
    }

    @Test
    public void testUpdateComment() {
        // Create a comment
        Comment comment = Comment.builder()
                .text("Original comment")
                .authorNickname("user1")
                .news(news)
                .build();

        // Save the comment
        Comment savedComment = commentRepository.save(comment);

        // Update the comment
        savedComment.setText("Updated comment");
        commentRepository.save(savedComment);

        // Find the comment by ID
        Optional<Comment> foundComment = commentRepository.findById(savedComment.getId());

        // Assert that the comment was updated
        assertThat(foundComment).isPresent();
        assertThat(foundComment.get().getText()).isEqualTo("Updated comment");
    }

    @Test
    public void testDeleteComment() {
        // Create a comment
        Comment comment = Comment.builder()
                .text("Comment to delete")
                .authorNickname("user1")
                .news(news)
                .build();

        // Save the comment
        Comment savedComment = commentRepository.save(comment);

        // Delete the comment
        commentRepository.delete(savedComment);

        // Try to find the comment by ID
        Optional<Comment> foundComment = commentRepository.findById(savedComment.getId());

        // Assert that the comment was deleted
        assertThat(foundComment).isEmpty();
    }

    @Test
    public void testParentChildRelationship() {
        // Create a root comment
        Comment rootComment = Comment.builder()
                .text("Root comment")
                .authorNickname("user1")
                .news(news)
                .build();

        // Save the root comment using the repository
        Comment savedRootComment = commentRepository.save(rootComment);
        
        // Create a reply to the root comment
        Comment replyComment = Comment.builder()
                .text("Reply comment")
                .authorNickname("user2")
                .news(news)
                .parentComment(savedRootComment)
                .build();
        
        // Save the reply comment using the repository
        Comment savedReplyComment = commentRepository.save(replyComment);
        
        // Verify the relationship is correctly established
        List<Comment> replies = commentRepository.findByParentComment(savedRootComment);
        assertThat(replies).hasSize(1);
        assertThat(replies.get(0).getId()).isEqualTo(savedReplyComment.getId());
        assertThat(replies.get(0).getText()).isEqualTo("Reply comment");
        
        // Verify the parent comment reference is correct
        Optional<Comment> foundReply = commentRepository.findById(savedReplyComment.getId());
        assertThat(foundReply).isPresent();
        assertThat(foundReply.get().getParentComment().getId()).isEqualTo(savedRootComment.getId());
    }
}