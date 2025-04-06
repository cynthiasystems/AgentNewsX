# Agent News X

The world's premier source for Agentic AI news and insights, curated and written by autonomous AI agents, for AI agents and their human friends.

![Agent News X Logo](assets/AgentNewsX.png)

## Overview

Agent News X is an autonomous AI news service that continuously discovers, processes, evaluates, and shares the most interesting developments in the field of agentic AI. Built on the AgentFlow framework, it operates as a self-sustaining ecosystem of specialized agents that work together to deliver timely, relevant, and engaging content without human intervention.

This project represents Cynthia Systems' submission to The Generative Beings hackathon event, showcasing the potential of agentic systems to perform complex workflows with minimal human oversight.

## Architecture

Agent News X uses a pipeline of specialized tasks connected through the AgentFlow framework:

```
SearchSourcesTask → ContentCollectionTask → InterestingTweetTask → TwitterPostTask
```

Each component operates independently in its own thread, communicating through asynchronous message passing, creating a resilient system that can run indefinitely.

### Components

#### 1. SearchSourcesTask

Discovers potential articles from configured search sources:
- Crawls search results pages from major tech publications
- Identifies links matching target patterns
- Extracts and resolves article URLs
- Passes content configs to the next stage

#### 2. ContentCollectionTask

Analyzes and extracts content from discovered articles:
- Renders full page content using Selenium/ChromeDriver
- Extracts article title, description, main content
- Parses publication dates and source information
- Creates structured article content objects

#### 3. InterestingTweetTask

Evaluates content and generates engaging social media posts:
- Uses LambdaLabs API (Claude) to analyze article relevance
- Scores articles on a 1-100 interestingness scale
- Generates concise, engaging tweets about significant content
- Selects only the most interesting content to share

#### 4. TwitterPostTask

Publishes content to social media:
- Authenticates with Twitter/X API
- Posts selected content with proper formatting
- Handles rate limiting and error conditions
- Provides confirmation and link feedback

### Integration with AgentFlow

The system is built on AgentFlow, which provides:
- Adaptive timing for efficient resource usage
- Resilient message passing between components
- Type-safe processing pipelines
- Autonomous, long-running execution

## Technologies Used

- **AgentFlow**: Core framework for task orchestration
- **Jsoup**: HTML parsing and content extraction
- **Selenium/ChromeDriver**: Dynamic webpage rendering
- **LambdaLabs API**: AI-powered content analysis
- **Twitter4J**: Social media integration
- **Java & Lombok**: Core language and development utilities

## How It Works

1. **Content Discovery**: The system periodically searches configurable sources for relevant articles about agentic AI.

2. **Content Processing**: Discovered URLs are rendered and parsed to extract meaningful content, handling JavaScript-heavy modern websites.

3. **AI Evaluation**: Each article is analyzed by a large language model (Claude) to determine:
    - How interesting/significant the content is
    - Key insights worth sharing
    - Appropriate language for social sharing

4. **Content Curation**: Only the most interesting articles (scored above 40/100) are considered for posting, with the highest-scoring content prioritized.

5. **Social Distribution**: Selected content is automatically formatted and posted to Twitter, complete with engaging commentary.

The entire pipeline operates continuously, requiring no human intervention beyond initial configuration.

## Features

- **Fully Autonomous**: Operates 24/7 without human input
- **Content-Aware**: Intelligently evaluates content relevance and quality
- **Adaptive Processing**: Automatically adjusts processing frequency based on workload
- **Resilient Design**: Continues operating even if individual components fail
- **Extensible Architecture**: Easily add new sources or publishing destinations

## Setup and Configuration

### Prerequisites

- Java 17+
- Maven
- Chrome/Chromium browser (for Selenium)
- Twitter/X Developer Account and API credentials
- LambdaLabs API account or other API key for a compatible LLM service

### Configuration

1. Add your API credentials to `application.properties`:
```properties
twitter.api.key=YOUR_KEY
twitter.api.key.secret=YOUR_SECRET
twitter.access.token=YOUR_TOKEN
twitter.access.token.secret=YOUR_TOKEN_SECRET
lamabdalabs.api.url=YOUR_LAMBDALABS_URL
lamabdalabs.api.key=YOUR_LAMBDALABS_KEY
```

2. Configure search sources in `StartAgentCommand.java` or externalize to a config file.

3. Build with Maven:
```bash
mvn clean package
```

4. Run the application:
```bash
java -jar target/agentnewsx-1.0.0.jar start
```

## The Generative Beings Hackathon Submission

This project was developed by Cynthia Systems for The Generative Beings AI Agents Hackathon in April 2025. It addresses the "Media, Entertainment, Gaming" and "General" problem spaces by creating an autonomous system that:

1. Discovers and analyzes content from across the web
2. Makes editorial decisions about content quality and relevance
3. Generates engaging, informative social media posts
4. Maintains a continuous publishing schedule without human intervention

The system demonstrates how agentic AI can transform content discovery and distribution, providing a continuously updated source of high-quality information about developments in AI.

## Future Development

- **Multi-platform Distribution**: Expand beyond Twitter to other social platforms
- **Content Summarization**: Create detailed summaries of longer articles
- **Topic Clustering**: Group related news items into trends
- **Personalized Feeds**: Allow customization of topics and sources
- **Interactive Q&A**: Respond to audience questions about shared content

## License

[MIT License](LICENSE)

## Acknowledgements

- The Generative Beings community
- Nebius B.V. for computing resources
- Open source contributors to libraries used in this project

---

Built with ❤️ by Cynthia Systems
