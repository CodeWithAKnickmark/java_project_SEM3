# java_project_SEM3
# Email Inbox Manager

A console-based email inbox built in Java as a Data Structures and Algorithms (DSA) project.

| | |
|---|---|
| **Name** | Pranav P |
| **ERP ID** | RU-25-10987 |
| **Semester** | 3 |
| **Problem Statement** | 72 |
| **Project Name** | Email Inbox Manager |
| **University** | Rungta International Skills University, School of Computer Science and Engineering |

## Problem Statement

Create a console email inbox. Store incoming emails in a priority queue based on importance and time, use a stack for recently opened emails, hashing for email-ID lookup, and sorting to display messages by sender or date. Higher importance is shown first; ties are ordered by arrival time. Data file: `inbox.txt`.

## Data Structures Used

| Data structure | Used for |
|---|---|
| `PriorityQueue<Email>` | Inbox order: High, then Normal, then Low. Equal priority goes by arrival time (oldest first) |
| `Stack<String>` | IDs of recently opened emails (latest on top) |
| `HashMap<String, Email>` | Fast email-ID lookup (open, search, mark as read) |
| `ArrayList` + `Comparator` | Sorting emails by sender or by date |
| File handling | Load from `inbox.txt` at startup, save on exit |

## Features

1. Receive a new email (sender and subject are required; priority is High, Normal or Low)
2. Open an email by ID
3. Open the next priority email
4. Open the most recently received email
5. Search an email by ID
6. Sort emails by sender
7. Sort emails by date
8. Mark an email as read
9. Display the inbox in priority order
10. View recently opened emails
11. Save and exit

Other behaviour:
- Empty inbox is handled with clear messages.
- Invalid input (unknown ID, empty sender or subject, wrong priority) is handled without crashing.
- Read/unread status is saved to `inbox.txt` on exit and restored at startup.

## How to Run

Requires JDK 21 (or any recent JDK).

```bash
javac Main.java
java Main
```

`inbox.txt` is created in the folder you run the program from when you choose option 11 (Save and exit).

## Test Cases

| # | Scenario | Input | Expected output |
|---|---|---|---|
| 1 | Normal input | Receive an email from admin with High priority | `Email received: EM101 \| admin \| High` |
| 2 | Boundary / minimum input | Inbox contains only EM101; open recent email | `Opened: EM101 \| admin` |
| 3 | Invalid input | Search for email EM999 | `Email EM999 not found.` |
| 4 | Different valid scenario | Inbox has Normal and High priority emails; open next email | The High-priority email is displayed first |

## Sample Run (Test Case 1)

```
Enter your choice: 1
---------- RECEIVE EMAIL ----------
Enter sender: admin
Enter subject: Server maintenance
Enter message: Scheduled downtime tonight at 11 PM.
Enter priority (High/Normal/Low): High
Email received: EM101 | admin | High
```

## Project Files

```
.
├── Main.java    # complete source code
├── inbox.txt    # saved inbox data (created on exit)
└── README.md
```

## Data File Format (`inbox.txt`)

One email per line, separated by `|`. Sender, subject and message are Base64-encoded so special characters cannot break the file.

```
# id|sender(base64)|subject(base64)|priority|arrivalTime|arrivalOrder|read|body(base64)
```