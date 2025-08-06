package com.dev.news.newsportal.suite;

import org.junit.platform.suite.api.IncludeClassNamePatterns;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

/**
 * JUnit 5 test suite that runs all tests in the News Portal application.
 * This suite can be used to execute all tests at once for comprehensive testing.
 * 
 * Usage:
 * - Run this class to execute all tests
 * - Useful for CI/CD pipelines and comprehensive test runs
 * - Provides organized test execution with proper reporting
 */
@Suite
@SuiteDisplayName("News Portal - All Tests Suite")
@SelectPackages({
    "com.dev.news.newsportal.service",
    "com.dev.news.newsportal.repository", 
    "com.dev.news.newsportal.controller",
    "com.dev.news.newsportal.mapper"
})
@IncludeClassNamePatterns(".*Test.*")
public class AllTestsSuite {
    // This class serves as a test suite runner
    // No implementation needed - JUnit Platform handles execution
}