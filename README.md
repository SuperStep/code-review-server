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

## Installation

### Docker Run
The easiest way to run Code Review Server is using Docker:

```bash
docker run -p 8080:8080 \
  -e BITBUCKET_BASE_URL=https://your-bitbucket-server.com \
  -e BITBUCKET_TOKEN=your_token \
  -e BITBUCKET_PROJECT=your_project_key \
  -e BITBUCKET_REPOSITORY=your_repo_name \
  -e AI_PROVIDER=gemini \
  -e GEMINI_TOKEN=your_gemini_api_token \
  -e GEMINI_MODEL=gemini-2.0-flash \
  superstep/code-review-server:latest
```

### Docker Compose

Alternatively, you can use Docker Compose with the provided compose.yaml file:

```yaml
version: '3'
services:
  code-review-server:
    image: superstep/code-review-server:latest
    ports:
      - "8080:8080"
    environment:
      - BITBUCKET_BASE_URL=https://your-bitbucket-server.com
      - BITBUCKET_TOKEN=your_token
      - BITBUCKET_PROJECT=your_project_key
      - BITBUCKET_REPOSITORY=your_repo_name
      - GEMINI_TOKEN=your_gemini_api_token
  ollama:
    image: ollama/ollama:latest
    ports:
      - "11434:11434"
```

Run with
```bash
docker-compose up -d
```