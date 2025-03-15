# Code Review Server

## Overview
Code Review Server is a service that automates code reviews for pull requests using AI. It integrates with version control systems (VCS) like GitHub, GitLab, or Bitbucket to fetch pull request information and generate AI-powered code reviews.

## Purpose
The main purpose of this application is to:
- Provide automated code reviews for pull requests
- Integrate with different version control systems
- Analyze code diffs and generate constructive feedback
- Help developers identify issues, improve code quality, and follow best practices

## Features
- Fetches pull request information from various VCS providers
- Extracts code diffs using JGit
- Generates AI-powered code reviews with:
    - Overall assessment
    - Key issues or concerns
    - Style and best practice suggestions
    - Security considerations

## Architecture
The application follows a service-oriented architecture with:
- VCS integration services (`VCSService` interface)
- Git diff extraction services (`GitDiffService` interface and `GitDiffServiceImpl` implementation)
- AI review generation services (`ReviewService` and `AiReviewProvider`)

## Technology Stack
- Kotlin
- Spring Boot
- JGit for Git operations
- AI-powered code review provider