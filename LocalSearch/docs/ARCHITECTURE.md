# LocalSearch Architecture & Algorithm Documentation

LocalSearch is a high-performance, offline, desktop document search engine built in Java 17+ and Java Swing. It features an Inverted Index with positional postings, TF-IDF relevance scoring, multi-mode Boolean and Phrase query processing, and persistent index storage.

---

## 1. High-Level System Architecture

```
+-------------------------------------------------------------+
|                     Swing Desktop GUI                       |
|  (MainWindow, SearchPanel, ResultsPanel, DocumentPanel)     |
+------------------------------+------------------------------+
                               |
                               v
+-------------------------------------------------------------+
|                      Search Engine                          |
|         (com.localsearch.core.SearchEngine)                 |
+--------------+-------------------------------+--------------+
               |                               |
               v                               v
+-------------------------------+ +---------------------------+
|        Query Processor        | |          Ranker           |
| (Tokenization, Modes, Stops)  | | (TF-IDF, Coverage, Boost) |
+-------------------------------+ +---------------------------+
               |                               ^
               v                               |
+----------------------------------------------+--------------+
|                     Inverted Index                          |
|          (Terms -> Documents, Frequencies, Positions)       |
+------------------------------+------------------------------+
                               |
                               v
+-------------------------------------------------------------+
|                Persistent Index Storage                     |
|           (index/index.dat & settings.properties)           |
+-------------------------------------------------------------+
```

---

## 2. Document Indexing Pipeline

When documents or folders are added to LocalSearch, the background `Indexer` executes the following sequence:

```
Physical File (.txt, .md, .java, .csv, .log, .xml, .json, .html, .css)
   │
   ▼
[1] File Validation & Access Check (DocumentStorage)
   │
   ▼
[2] SHA-256 Checksum Calculation & Metadata Extraction (ID, size, timestamps)
   │
   ▼
[3] Full-Text UTF-8 Ingestion (Files.readString)
   │
   ▼
[4] Tokenization with Position Tracking (Tokenizer)
       - Preserves technical keywords (e.g. TCP/IP, C++, .NET, HashMap, JSON)
       - Normalizes to lowercase
       - Records 0-based token index positions for phrase matching
   │
   ▼
[5] Inverted Index Ingestion (InvertedIndex)
       - Term -> Map<DocId, Posting(frequency, [positions])>
       - Updates document length & vocabulary statistics
   │
   ▼
[6] Atomic Disk Persistence (IndexStorage)
       - Serializes index graph to temporary file and atomically commits
```

---

## 3. Search & Retrieval Pipeline

When a user submits a search query in the search bar:

```
Raw User Input (e.g. "packet switching", packet AND routing, packet OR routing)
   │
   ▼
[1] QueryProcessor
       - Identifies query type: KEYWORD | AND | OR | PHRASE
       - Strips syntax / quotes and extracts search tokens
       - Applies StopWordFilter (if enabled in settings)
   │
   ▼
[2] Inverted Index Candidate Retrieval
       - KEYWORD / OR: Union of all document postings matching any term
       - AND: Intersection of document postings matching all terms
       - PHRASE: Positional verification across posting position lists (p, p+1, p+2)
   │
   ▼
[3] Document Scoring & Relevance Ranking (Ranker)
       - Calculates TF-IDF score for each candidate document:
           TF = 1 + ln(term_frequency)
           IDF = ln(1 + (N - df + 0.5) / (df + 0.5))
       - Applies Document Length Normalization:
           norm = 0.75 + 0.25 * (doc_length / avg_doc_length)
       - File Name Match Bonus: +3.5 points per filename match
       - Exact Phrase Bonus: +4.0 points per exact sequential match
       - Query Term Coverage Multiplier: (0.7 + 0.6 * coverage_ratio)
       - Normalizes relevance percentages (15% to 99%)
   │
   ▼
[4] Dynamic Snippet Generation & Highlighting (TextUtils)
       - Extracts relevant context window around first/strongest match
       - Injects CSS styled HTML tags around matched terms
   │
   ▼
[5] Filtering & Sorting
       - Filters by file extension (.txt, .md, .java, etc.)
       - Sorts by Relevance (desc), File Name (A-Z), Last Modified (newest), or Size (desc)
       - Enforces max result cap
   │
   ▼
[6] Swing Results Rendering (ResultsPanel)
```

---

## 4. Relevance Ranking Algorithm Details

### Mathematical Formulation

For a query $Q$ with terms $\{t_1, t_2, \dots, t_k\}$ and document $D$:

1. **Sublinear Term Frequency Weight ($w_{tf}$)**:
   $$w_{tf}(t, D) = 1 + \ln(tf(t, D)) \quad \text{for } tf > 0$$

2. **Probabilistic Inverse Document Frequency ($IDF$)**:
   $$IDF(t) = \ln\left(1 + \frac{N - df(t) + 0.5}{df(t) + 0.5}\right)$$
   Where $N$ is total indexed documents and $df(t)$ is document frequency of term $t$.

3. **Length Normalization ($L_{norm}$)**:
   $$L_{norm}(D) = 0.75 + 0.25 \cdot \frac{|D|}{\text{avgDocLength}}$$

4. **Base Term Score ($S_{terms}$)**:
   $$S_{terms}(D, Q) = \sum_{t \in Q \cap D} \frac{w_{tf}(t, D) \cdot IDF(t)}{L_{norm}(D)}$$

5. **Boosts**:
   - **Phrase Boost ($B_{phrase}$)**: $+4.0 \times \text{phraseOccurrences}(D, Q)$
   - **Title Boost ($B_{title}$)**: $+3.5$ for each query term found in file name.

6. **Query Term Coverage ($C_{query}$)**:
   $$C_{query} = \frac{|Q \cap D|}{|Q|}$$
   $$\text{FinalScore}(D, Q) = \left(S_{terms} + B_{phrase} + B_{title}\right) \times \left(0.7 + 0.6 \cdot C_{query}\right)$$

7. **Percentage Normalization**:
   $$\text{Percentage}(D) = \min\left(99, \max\left(15, \text{round}\left(30 + \frac{\text{Score}(D)}{\max_{d} \text{Score}(d)} \cdot 65\right)\right)\right)$$

---

## 5. Concurrency & Performance Design

- **Thread-Safe Inverted Index**: All internal data structures in `InvertedIndex` utilize `ConcurrentHashMap` with synchronized modifications for atomic write operations.
- **SwingWorker Background Execution**: File system indexing, recursive folder scanning, and modification checks execute on background worker threads, preventing Swing Event Dispatch Thread (EDT) freezes.
- **Atomic Index Persistence**: Temporary file writes followed by atomic file replacement (`StandardCopyOption.ATOMIC_MOVE`) protect index files against corruption during abrupt application termination.
- **Lazy Content Reading**: Only candidate documents returned by index lookups are read from disk for snippet generation, ensuring sub-10ms search latencies across thousands of files.
