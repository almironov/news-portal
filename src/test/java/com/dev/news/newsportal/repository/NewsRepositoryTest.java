package com.dev.news.newsportal.repository;

import com.dev.news.newsportal.entity.News;
import com.dev.news.newsportal.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class NewsRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private NewsRepository newsRepository;

    private User author;
    private LocalDateTime now;

    @BeforeEach
    public void setup() {
        // Create a user to be the author of news
        author = User.builder()
                .nickname("newsauthor")
                .email("author@example.com")
                .role("REGISTERED_USER")
                .build();
        
        entityManager.persist(author);
        entityManager.flush();
        
        now = LocalDateTime.now();
    }

    @Test
    public void testCreateAndFindById() {
        // Create a news item
        News news = News.builder()
                .title("Test News Title")
                .text("This is a test news article content.")
                .imageUrl("https://example.com/image.jpg")
                .author(author)
                .build();

        // Save the news
        News savedNews = newsRepository.save(news);

        // Find the news by ID
        Optional<News> foundNews = newsRepository.findById(savedNews.getId());

        // Assert that the news was found and has the correct properties
        assertThat(foundNews).isPresent();
        assertThat(foundNews.get().getTitle()).isEqualTo("Test News Title");
        assertThat(foundNews.get().getText()).isEqualTo("This is a test news article content.");
        assertThat(foundNews.get().getImageUrl()).isEqualTo("https://example.com/image.jpg");
        assertThat(foundNews.get().getAuthor().getId()).isEqualTo(author.getId());
        assertThat(foundNews.get().getCreationDate()).isNotNull();
    }

    @Test
    public void testFindByAuthor() {
        // Create multiple news items by the same author
        News news1 = News.builder()
                .title("First News")
                .text("First news content")
                .author(author)
                .build();

        News news2 = News.builder()
                .title("Second News")
                .text("Second news content")
                .author(author)
                .build();

        entityManager.persist(news1);
        entityManager.persist(news2);
        entityManager.flush();

        // Find news by author
        List<News> newsList = newsRepository.findByAuthor(author);

        // Assert that both news items were found
        assertThat(newsList).hasSize(2);
        assertThat(newsList).extracting(News::getTitle).containsExactlyInAnyOrder("First News", "Second News");
    }

    @Test
    public void testFindByTitleContainingIgnoreCase() {
        // Create news items with different titles
        News news1 = News.builder()
                .title("Breaking News: Important Event")
                .text("Content about important event")
                .author(author)
                .build();

        News news2 = News.builder()
                .title("Daily news update")
                .text("Daily updates")
                .author(author)
                .build();

        entityManager.persist(news1);
        entityManager.persist(news2);
        entityManager.flush();

        // Find news by title containing "news" (case insensitive)
        List<News> newsList = newsRepository.findByTitleContainingIgnoreCase("news");

        // Assert that both news items were found
        assertThat(newsList).hasSize(2);
        
        // Find news by title containing "important" (case insensitive)
        newsList = newsRepository.findByTitleContainingIgnoreCase("important");
        
        // Assert that only the first news item was found
        assertThat(newsList).hasSize(1);
        assertThat(newsList.get(0).getTitle()).isEqualTo("Breaking News: Important Event");
    }

    @Test
    public void testFindByCreationDateBetween() {
        // Create news items with different creation dates
        LocalDateTime yesterday = now.minusDays(1);
        LocalDateTime tomorrow = now.plusDays(1);
        
        News news1 = News.builder()
                .title("Old News")
                .text("Old content")
                .author(author)
                .creationDate(yesterday)
                .build();

        News news2 = News.builder()
                .title("Recent News")
                .text("Recent content")
                .author(author)
                .creationDate(now)
                .build();

        News news3 = News.builder()
                .title("Future News")
                .text("Future content")
                .author(author)
                .creationDate(tomorrow)
                .build();

        entityManager.persist(news1);
        entityManager.persist(news2);
        entityManager.persist(news3);
        entityManager.flush();

        // Find news created between yesterday and now
        List<News> newsList = newsRepository.findByCreationDateBetween(yesterday, now);

        // Assert that two news items were found
        assertThat(newsList).hasSize(2);
        assertThat(newsList).extracting(News::getTitle).containsExactlyInAnyOrder("Old News", "Recent News");
    }

    @Test
    public void testUpdateNews() {
        // Create a news item
        News news = News.builder()
                .title("Original Title")
                .text("Original content")
                .author(author)
                .build();

        // Save the news
        News savedNews = newsRepository.save(news);

        // Update the news
        savedNews.setTitle("Updated Title");
        savedNews.setText("Updated content");
        newsRepository.save(savedNews);

        // Find the news by ID
        Optional<News> foundNews = newsRepository.findById(savedNews.getId());

        // Assert that the news was updated
        assertThat(foundNews).isPresent();
        assertThat(foundNews.get().getTitle()).isEqualTo("Updated Title");
        assertThat(foundNews.get().getText()).isEqualTo("Updated content");
    }

    @Test
    public void testDeleteNews() {
        // Create a news item
        News news = News.builder()
                .title("News to Delete")
                .text("Content to delete")
                .author(author)
                .build();

        // Save the news
        News savedNews = newsRepository.save(news);

        // Delete the news
        newsRepository.delete(savedNews);

        // Try to find the news by ID
        Optional<News> foundNews = newsRepository.findById(savedNews.getId());

        // Assert that the news was deleted
        assertThat(foundNews).isEmpty();
    }

    @Test
    public void testFindByAuthorOrderByCreationDateDesc() {
        // Create multiple news items by the same author with different creation dates
        LocalDateTime older = now.minusDays(2);
        LocalDateTime old = now.minusDays(1);
        
        News news1 = News.builder()
                .title("Oldest News")
                .text("Oldest content")
                .author(author)
                .creationDate(older)
                .build();

        News news2 = News.builder()
                .title("Old News")
                .text("Old content")
                .author(author)
                .creationDate(old)
                .build();

        News news3 = News.builder()
                .title("Recent News")
                .text("Recent content")
                .author(author)
                .creationDate(now)
                .build();

        entityManager.persist(news1);
        entityManager.persist(news2);
        entityManager.persist(news3);
        entityManager.flush();

        // Find news by author ordered by creation date (newest first)
        List<News> newsList = newsRepository.findByAuthorOrderByCreationDateDesc(author);

        // Assert that the news items are in the correct order
        assertThat(newsList).hasSize(3);
        assertThat(newsList.get(0).getTitle()).isEqualTo("Recent News");
        assertThat(newsList.get(1).getTitle()).isEqualTo("Old News");
        assertThat(newsList.get(2).getTitle()).isEqualTo("Oldest News");
    }

    @Test
    public void testFindAllWithPagination() {
        // Create multiple news items (more than one page worth)
        for (int i = 1; i <= 15; i++) {
            News news = News.builder()
                    .title("News " + i)
                    .text("Content " + i)
                    .author(author)
                    .creationDate(now.plusHours(i))
                    .build();
            entityManager.persist(news);
        }
        entityManager.flush();

        // Create a Pageable object for the first page with 5 items per page
        Pageable firstPageWithFiveItems = PageRequest.of(0, 5);
        
        // Get the first page
        Page<News> firstPage = newsRepository.findAll(firstPageWithFiveItems);
        
        // Assert pagination metadata
        assertThat(firstPage.getContent()).hasSize(5);
        assertThat(firstPage.getTotalElements()).isEqualTo(15);
        assertThat(firstPage.getTotalPages()).isEqualTo(3);
        assertThat(firstPage.getNumber()).isEqualTo(0);
        
        // Get the second page
        Pageable secondPageWithFiveItems = PageRequest.of(1, 5);
        Page<News> secondPage = newsRepository.findAll(secondPageWithFiveItems);
        
        // Assert second page
        assertThat(secondPage.getContent()).hasSize(5);
        assertThat(secondPage.getNumber()).isEqualTo(1);
        
        // Get the third page
        Pageable thirdPageWithFiveItems = PageRequest.of(2, 5);
        Page<News> thirdPage = newsRepository.findAll(thirdPageWithFiveItems);
        
        // Assert third page
        assertThat(thirdPage.getContent()).hasSize(5);
        assertThat(thirdPage.getNumber()).isEqualTo(2);
        
        // Verify that all pages contain different items
        assertThat(firstPage.getContent())
                .extracting(News::getTitle)
                .doesNotContainAnyElementsOf(
                        secondPage.getContent().stream().map(News::getTitle).toList()
                );
        
        assertThat(secondPage.getContent())
                .extracting(News::getTitle)
                .doesNotContainAnyElementsOf(
                        thirdPage.getContent().stream().map(News::getTitle).toList()
                );
    }

    @Test
    public void testFindAllWithPaginationAndSorting() {
        // Create multiple news items with different creation dates
        LocalDateTime baseTime = now;
        
        for (int i = 1; i <= 10; i++) {
            News news = News.builder()
                    .title("News " + i)
                    .text("Content " + i)
                    .author(author)
                    .creationDate(baseTime.plusHours(i))
                    .build();
            entityManager.persist(news);
        }
        entityManager.flush();

        // Create a Pageable object with sorting by creation date in descending order
        Pageable pageWithSorting = PageRequest.of(0, 5, Sort.by("creationDate").descending());
        
        // Get the page
        Page<News> page = newsRepository.findAll(pageWithSorting);
        
        // Assert pagination metadata
        assertThat(page.getContent()).hasSize(5);
        assertThat(page.getTotalElements()).isEqualTo(10);
        assertThat(page.getTotalPages()).isEqualTo(2);
        
        // Assert that the items are sorted by creation date in descending order
        // (newest first, which means the highest number in our test data)
        assertThat(page.getContent().get(0).getTitle()).isEqualTo("News 10");
        assertThat(page.getContent().get(1).getTitle()).isEqualTo("News 9");
        assertThat(page.getContent().get(2).getTitle()).isEqualTo("News 8");
        assertThat(page.getContent().get(3).getTitle()).isEqualTo("News 7");
        assertThat(page.getContent().get(4).getTitle()).isEqualTo("News 6");
        
        // Get the second page
        Pageable secondPageWithSorting = PageRequest.of(1, 5, Sort.by("creationDate").descending());
        Page<News> secondPage = newsRepository.findAll(secondPageWithSorting);
        
        // Assert second page content
        assertThat(secondPage.getContent()).hasSize(5);
        assertThat(secondPage.getContent().get(0).getTitle()).isEqualTo("News 5");
        assertThat(secondPage.getContent().get(1).getTitle()).isEqualTo("News 4");
        assertThat(secondPage.getContent().get(2).getTitle()).isEqualTo("News 3");
        assertThat(secondPage.getContent().get(3).getTitle()).isEqualTo("News 2");
        assertThat(secondPage.getContent().get(4).getTitle()).isEqualTo("News 1");
    }
}