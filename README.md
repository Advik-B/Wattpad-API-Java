
# Wattpad API - Java Client

![Wattpad API](https://img.shields.io/badge/Wattpad-API-orange?style=for-the-badge)
![Java](https://img.shields.io/badge/Java-17+-blue?style=for-the-badge)
![License](https://img.shields.io/badge/License-MIT-green?style=for-the-badge)

A powerful and easy-to-use Java client for interacting with Wattpad's API. This library allows developers to fetch stories, parts, and metadata from Wattpad, enabling seamless integration into your Java applications.

---

## 🌟 Features

- Fetch Wattpad stories by ID or part ID.
- Retrieve metadata such as title, author, description, tags, and cover images.
- Render story parts with support for text and images.
- Built-in caching for optimized performance.
- Fully customizable client configuration.

---

## 📦 Installation

### Prerequisites
- Java 17 or higher
- Maven or Gradle for dependency management

### Using Maven
Add the following dependency to your `pom.xml`:
```xml
<dependency>
    <groupId>dev.advik</groupId>
    <artifactId>wattpad-api</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Using Gradle
Add the following to your `build.gradle`:
```gradle
implementation 'dev.advik:wattpad-api:1.0.0'
```

---

## 🚀 Usage

### Quick Start
Here's an example of how to use the Wattpad API client to fetch and render a story:

```java
import dev.advik.wattpad.WattpadClient;
import dev.advik.wattpad.models.Story;

public class Main {
    public static void main(String[] args) {
        WattpadClient client = new WattpadClient.Builder().useCache(true).build();
        long storyId = 336166598L; // Example story ID

        try {
            Story story = Story.fromId(storyId, client);
            System.out.println("Title: " + story.getTitle());
            System.out.println("Author: " + story.getAuthor().getName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

### Rendering Story Parts
The library supports rendering story parts with text and images:
```java
RenderedPage renderedPage = part.renderWith(client);
for (HTMLContent content : renderedPage.getContentStack()) {
    if (content.getType() == HTMLContent.Type.TEXT) {
        System.out.println(content.getTextData());
    } else if (content.getType() == HTMLContent.Type.IMAGE) {
        System.out.println("[IMAGE: " + content.getImageUrl() + "]");
    }
}
```

---

## 📖 Examples

### Fetching a Story by ID
```java
Story story = Story.fromId(336166598L, client);
System.out.println("Title: " + story.getTitle());
System.out.println("Description: " + story.getDescription());
```

### Fetching Metadata
```java
System.out.println("Author: " + story.getAuthor().getName());
System.out.println("Tags: " + story.getTags());
System.out.println("Cover URL: " + story.getCoverUrl());
```

### Clearing Cache
```java
client.clearCache();
System.out.println("Cache cleared.");
```

---

## 🛠️ Development

### Building the Project
Clone the repository and build the project using Maven:
```bash
git clone https://github.com/your-username/Wattpad-API.git
cd Wattpad-API
mvn clean install
```

### Running the Demo
Run the `Main.java` file to see the API in action:
```bash
java -cp target/wattpad-api-1.0.0.jar dev.advik.Main
```

---

## 🤝 Contributing

Contributions are welcome! Please follow these steps:
1. Fork the repository.
2. Create a new branch for your feature or bugfix.
3. Commit your changes and push them to your fork.
4. Submit a pull request.

---

## 📜 License

This project is licensed under the [MIT License](LICENSE.txt).

---

## 💬 Contact

For questions or support, feel free to reach out:
- **Author**: Advik
- **Email**: advik@example.com
- **GitHub**: [github.com/your-username](https://github.com/your-username)

---

## 🌐 Links

- [Wattpad](https://www.wattpad.com)
- [API Documentation](https://www.wattpad.com/api-docs)
- [GitHub Repository](https://github.com/your-username/Wattpad-API)
