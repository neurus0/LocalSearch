# LOCALSEARCH
### *File-Based Offline Document Search Engine*

[![Java 17+](https://img.shields.io/badge/Java-17%2B-blue.svg)](https://openjdk.org/)
[![GUI-Swing](https://img.shields.io/badge/GUI-Java%20Swing-orange.svg)]()
[![Build-Maven](https://img.shields.io/badge/Build-Maven-green.svg)]()
[![Offline](https://img.shields.io/badge/100%25-Offline-purple.svg)]()

**LocalSearch** is a fast, lightweight, 100% offline desktop search engine designed for personal files, source code, and technical documentation. It indexes local files, constructs an in-memory inverted index with positional postings, computes academic TF-IDF relevance scores, and renders an intuitive dark developer-tool interface in Java Swing.

---

## Table of Contents
1. [Project Overview](#project-overview)
2. [Key Features](#key-features)
3. [Architecture & Design](#architecture--design)
4. [Supported File Types](#supported-file-types)
5. [Installation & Requirements](#installation--requirements)
6. [Build & Execution](#build--execution)
7. [Search Modes & Syntax](#search-modes--syntax)
8. [Ranking & Scoring Model](#ranking--scoring-model)
9. [Step-by-Step Demo Scenario](#step-by-step-demo-scenario)
10. [Project Structure](#project-structure)
11. [Testing](#testing)
12. [Limitations & Future Roadmap](#limitations--future-roadmap)

---

## 1. Project Overview

Finding specific code snippets, notes, and documents across local project directories often requires tedious manual browsing or slow grep commands. **LocalSearch** provides instant full-text document retrieval on your desktop without requiring internet connectivity, cloud APIs, databases, or web servers.

---

## 2. Key Features

- **Blazing Fast Inverted Indexing**: Indexes terms with occurrence frequencies and word positions for instant retrieval.
- **Academic TF-IDF Relevance Ranking**: Ranks results dynamically based on term frequency, inverse document frequency, document length normalization, query coverage, and title match boosts.
- **Comprehensive Search Operators**:
  - **Keyword Search**: `packet routing`
  - **Boolean AND**: `packet AND routing` (or `packet + routing`)
  - **Boolean OR**: `packet OR routing` (or `packet | routing`)
  - **Positional Phrase Search**: `"packet switching"`
- **Highlighted Search Snippets**: Automatically extracts the most relevant text excerpt and highlights matching terms.
- **Automatic Change & Modification Detection**: Detects modified or deleted files upon launch using SHA-256 hashes and file timestamps.
- **Document Management**: Inspect indexed files, re-index modified files, or remove files from the index (without deleting them from disk).
- **Index Persistence**: Safely saves and reloads index data to disk (`index/index.dat`) between application restarts.
- **Sleek Dark Developer UI**: Custom styled Java Swing interface with vector icons, smooth scrollbars, result cards, and statistics widgets.
- **Keyboard Shortcuts**:
  - `Ctrl + F`: Focus search field
  - `Ctrl + O`: Open Add Files dialog
  - `Ctrl + R`: Check for modified/missing files
  - `Enter`: Submit search
  - `Esc`: Clear search / return to search view

---

## 3. Architecture & Design

LocalSearch follows a modular layered architecture:

- **`com.localsearch.model`**: Core domain entities (`Document`, `IndexEntry`, `SearchResult`, `SearchQuery`, `IndexStats`).
- **`com.localsearch.core`**: Algorithmic engine (`SearchEngine`, `Indexer`, `QueryProcessor`, `Ranker`, `Tokenizer`, `StopWordFilter`).
- **`com.localsearch.index`**: Inverted index data structures and atomic disk serialization (`InvertedIndex`, `IndexStorage`).
- **`com.localsearch.storage`**: File system readers, SHA-256 hash generators, and configuration settings (`DocumentStorage`, `AppSettings`).
- **`com.localsearch.ui`**: Modern Swing components (`MainWindow`, `SearchPanel`, `ResultsPanel`, `DocumentPanel`, `AddFilesDialog`, `SettingsDialog`, `AboutDialog`, `UITheme`, `IconFactory`).
- **`com.localsearch.util`**: String formatters, snippet extractors, HTML sanitizers, and date utilities (`FileUtils`, `TextUtils`, `DateUtils`).

For detailed mathematical algorithms and sequence diagrams, refer to [ARCHITECTURE.md](file:///c:/Users/rajde/OneDrive/Desktop/RAJDEEEP%20MAITY/projects/LocalSearch/LocalSearch/docs/ARCHITECTURE.md).

---

## 4. Supported File Types

LocalSearch indexes pure text-based documents:

| Extension | Description |
|---|---|
| `.txt` | Plain text documents & technical notes |
| `.md` | Markdown documentation |
| `.java` | Java source code |
| `.csv` | Comma-separated values data |
| `.log` | Application & system log files |
| `.xml` | Extensible Markup Language configuration files |
| `.json` | JavaScript Object Notation data files |
| `.html` | Web markup documents (indexed as text) |
| `.css` | Cascading Style Sheets |

---

## 5. Installation & Requirements

- **Java Development Kit (JDK)**: Java 17 or newer (Temurin / Adoptium / Oracle OpenJDK).
- **Maven**: 3.6+ (Maven wrapper scripts `mvn.cmd` and `mvnw` are included).
- **Operating System**: Windows, macOS, or Linux.

---

## 6. Build & Execution

### Building the Executable JAR
To clean, compile, run all 20 unit/integration tests, and package the application:

```bash
mvn clean package
```

*(On Windows without global Maven installed, run `.\mvn.cmd clean package`)*

### Running the Application
Launch the standalone executable JAR:

```bash
java -jar target/LocalSearch.jar
```

Or compile and run directly with Maven:

```bash
mvn compile exec:java -Dexec.mainClass="com.localsearch.Main"
```

---

## 7. Search Modes & Syntax

| Mode | Syntax Example | Behavior |
|---|---|---|
| **Keyword** | `packet routing` | Matches documents containing any query term, ranked by coverage and TF-IDF. |
| **Boolean AND** | `packet AND routing`<br>`packet + routing` | Returns only documents containing **all** specified terms. |
| **Boolean OR** | `packet OR routing`<br>`packet \| routing` | Returns documents containing **either** term. |
| **Exact Phrase** | `"packet switching"` | Returns only documents where the terms appear in **exact sequential order**. |
| **Single Term** | `concurrency` | Instant lookup for a specific term. |

---

## 8. Ranking & Scoring Model

Relevance percentages ($15\% - 99\%$) are calculated from an academic TF-IDF and BM25-inspired scoring formula:

1. **Sublinear Term Frequency**: $w_{tf} = 1 + \ln(tf)$
2. **Probabilistic IDF**: $IDF = \ln\left(1 + \frac{N - df + 0.5}{df + 0.5}\right)$
3. **Document Length Normalization**: $L_{norm} = 0.75 + 0.25 \cdot \frac{\text{length}}{\text{avgLength}}$
4. **Exact Phrase Boost**: $+4.0$ per consecutive phrase occurrence
5. **Filename Boost**: $+3.5$ bonus if search terms appear in the document's file name
6. **Query Term Coverage**: Multiplier between $0.7\times$ and $1.3\times$ rewarding documents matching a higher percentage of the search terms.

---

## 9. Step-by-Step Demo Scenario

1. **Start the Application**:
   ```bash
   java -jar target/LocalSearch.jar
   ```
2. **Add Sample Documents**:
   - Click **`+ Add Files`** or press `Ctrl + O`.
   - Select the `documents/` directory in the project root.
   - The responsive indexing dialog will display progress and index all 10 sample files.
3. **Perform Keyword Search**:
   - Type `packet routing` and press `Enter`.
   - Results show `Computer_Networks.txt` at the top (~90%+ relevance) with highlighted snippets.
4. **Test Boolean AND**:
   - Type `packet AND routing`.
   - Only documents containing both terms are returned.
5. **Test Boolean OR**:
   - Type `packet OR routing`.
   - Documents containing either term are returned.
6. **Test Exact Phrase Search**:
   - Type `"packet switching"`.
   - Notice positional indexing guarantees exact phrase matching.
7. **Filter by File Type & Sort**:
   - Change filter from `All Files` to `TXT`.
   - Change sort from `Relevance` to `File Name` or `File Size`.
8. **Open Original Document**:
   - Click the file name or **Open** button to open the file in your OS default editor.
9. **Test Automatic Change Detection**:
   - Modify one of the files in `documents/` using any text editor.
   - Return to LocalSearch and press `Ctrl + R` or click **Check for Changes**.
   - The document status will update to **`Needs re-indexing`**.
   - Click **Re-index Selected** to update the index.
10. **Test Index Persistence**:
    - Close the application.
    - Restart with `java -jar target/LocalSearch.jar`.
    - All documents, term postings, and settings are restored instantly.

---

## 10. Project Structure

```
LocalSearch/
├── documents/                       # 10 rich sample technical text documents
│   ├── Computer_Architecture.txt
│   ├── Computer_Networks.txt
│   ├── Operating_Systems.txt
│   ├── Java_Programming.txt
│   ├── Database_Systems.txt
│   ├── Python_Development.txt
│   ├── Cybersecurity_Fundamentals.txt
│   ├── Cloud_Computing.txt
│   ├── Data_Structures_Algorithms.txt
│   └── Software_Engineering.txt
├── index/                           # Persisted index and settings storage
│   ├── index.dat
│   └── settings.properties
├── docs/
│   └── ARCHITECTURE.md              # Detailed algorithm & pipeline docs
├── src/
│   ├── main/java/com/localsearch/
│   │   ├── Main.java                # Application Entrypoint
│   │   ├── core/
│   │   │   ├── Indexer.java         # Background file indexer
│   │   │   ├── QueryProcessor.java  # Query syntax & operator parser
│   │   │   ├── Ranker.java          # TF-IDF relevance scoring engine
│   │   │   ├── SearchEngine.java    # Search coordinator
│   │   │   ├── StopWordFilter.java  # English stop words filter
│   │   │   └── Tokenizer.java       # Positional tech-aware tokenizer
│   │   ├── index/
│   │   │   ├── IndexStorage.java    # Atomic disk serializer
│   │   │   └── InvertedIndex.java   # Core positional inverted index
│   │   ├── model/
│   │   │   ├── Document.java        # Document metadata & status
│   │   │   ├── IndexEntry.java      # Postings & occurrence positions
│   │   │   ├── IndexStats.java      # Vocabulary and entry metrics
│   │   │   ├── SearchQuery.java     # Structured query model
│   │   │   └── SearchResult.java    # Ranked result item
│   │   ├── storage/
│   │   │   ├── AppSettings.java     # User configuration preferences
│   │   │   └── DocumentStorage.java # File I/O & SHA-256 change detector
│   │   ├── ui/
│   │   │   ├── AboutDialog.java     # About project modal
│   │   │   ├── AddFilesDialog.java  # Background indexing progress modal
│   │   │   ├── DocumentPanel.java   # Document catalog manager
│   │   │   ├── IconFactory.java     # Programmatic vector icons
│   │   │   ├── MainWindow.java      # Main application window
│   │   │   ├── ResultsPanel.java    # Search result cards renderer
│   │   │   ├── SearchPanel.java     # Search bar & filter toolbar
│   │   │   ├── SettingsDialog.java  # Settings & index maintenance
│   │   │   └── UITheme.java         # Dark developer theme styling
│   │   └── util/
│   │       ├── DateUtils.java       # Friendly timestamp formatter
│   │       ├── FileUtils.java       # File helpers & desktop launcher
│   │       └── TextUtils.java       # HTML highlighter & snippet generator
│   └── test/java/com/localsearch/   # 20 automated unit & integration tests
│       ├── EndToEndSearchTest.java
│       ├── core/
│       │   ├── QueryProcessorTest.java
│       │   ├── RankerTest.java
│       │   ├── StopWordFilterTest.java
│       │   └── TokenizerTest.java
│       ├── index/
│       │   ├── IndexPersistenceTest.java
│       │   └── InvertedIndexTest.java
│       └── storage/
│           └── DocumentStorageTest.java
├── pom.xml                          # Maven build configuration
└── README.md                        # Complete project documentation
```

---

## 11. Testing

LocalSearch includes 20 comprehensive unit and integration tests verifying all core indexing, searching, phrase matching, TF-IDF ranking, persistence, and change detection logic.

Execute the test suite:
```bash
mvn test
```

---

## 12. Limitations & Future Roadmap

- **Binary File Formats**: PDF, DOCX, and PPTX formats are intentionally omitted in Version 1.0 (requires Apache Tika or PDFBox).
- **Fuzzy / Edit Distance Search**: Future iterations can incorporate Levenshtein distance or Damerau-Levenshtein automata for typo tolerance.
- **Stemming**: Future versions can introduce the Porter Stemmer algorithm for linguistic root word matching (e.g. `routing` $\rightarrow$ `route`).
