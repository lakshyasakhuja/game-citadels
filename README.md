# Citadels Game — Java Implementation

This repository contains my **INFO1113 (Object Oriented Programming)** project from the **University of Sydney (Semester 2)**.

The project is a **complete Java implementation of the board game *Citadels***, developed strictly according to the official rulebook. The game supports full gameplay logic, character abilities, turn sequencing, scoring, and persistence, and includes **comprehensive JUnit testing** to validate correctness and robustness.

---

## 📘 Project Overview

At a high level, this project recreates the full **Citadels card game experience** in software form.

Players assume different character roles each round, collect resources, construct districts, and strategically interact with other players through character abilities. The game continues across multiple rounds until an end condition is reached, after which final scores are calculated according to the official rules.

This project emphasises **object-oriented design**, **rule-based logic**, and **test-driven verification**, mirroring real-world software engineering practices.

---

## 🌍 Project Motivation & Challenge

Citadels is a **rules-heavy strategy game** with complex interactions between characters, players, and game state. Translating these mechanics into code required:

* Careful interpretation of the official rulebook
* Correct sequencing of turns and character actions
* Handling special abilities and edge cases
* Maintaining consistency across rounds and phases
* Ensuring correctness through automated testing

The goal was not just to “make it run”, but to **faithfully model the game logic** while keeping the code modular, readable, and maintainable.

---

## 🧠 Game Logic & Design

The system is built using a **strong object-oriented architecture**, with clear separation of responsibilities:

* **Game / GameState** manages rounds, phases, and overall flow
* **Player** encapsulates player state, resources, and built districts
* **Character** classes implement role-specific abilities
* **District** classes represent buildable cards and scoring logic
* **Command handling** manages player input and actions
* **AI logic** supports automated players
* **Save / Load functionality** allows game persistence

Each component mirrors a real-world game entity, making the design intuitive and extensible.

---

## 🎮 Gameplay Features

* Full implementation of Citadels rules and phases
* Character selection and turn-order resolution
* Role-based abilities (Assassin, Thief, Magician, Architect, Warlord, etc.)
* Resource management (gold, cards, districts)
* District construction rules and restrictions
* End-game detection and final scoring
* Optional AI-controlled players
* Save and load game state support

---

## 🧪 Testing & Verification

A significant part of this project focused on **automated testing using JUnit**.

Testing covers:

* Core game flow and round progression
* Character abilities and their effects
* Edge cases in turn execution
* Save and load functionality
* Error handling and invalid actions

JUnit tests were used to **increase code reliability**, catch regressions, and ensure that rule logic behaves exactly as intended.

---

## 📂 Repository Structure

```text
src/
├── main/
│   └── java/
│       └── citadels/
│           ├── Game.java
│           ├── GameState.java
│           ├── Player.java
│           ├── Character.java
│           ├── District.java
│           ├── AIPlayer.java
│           ├── CommandHandler.java
│           └── ...
│
├── test/
│   └── java/
│       └── citadels/
│           ├── GameTest.java
│           ├── PlayerTest.java
│           ├── CommandHandlerTest.java
│           └── ...
```

Gradle is used for dependency management and test execution.

---

## 🧠 Key Learnings

* Translating complex real-world rules into executable logic
* Designing large object-oriented systems in Java
* Managing game state across multiple rounds and players
* Writing meaningful unit tests with JUnit
* Debugging and validating edge cases
* Structuring large projects for maintainability

---

## 🛠 Tools & Technologies

* **Java**
* **Gradle**
* **JUnit**
* **Object-Oriented Programming**
* **Game Logic & State Management**
* **Automated Testing**

---

## 📝 Significance

This project represents one of my **most comprehensive software engineering builds**.

It required:

* Deep understanding of object-oriented design
* Careful rule interpretation and implementation
* Strong testing discipline

Implementing a full strategy board game with tests reinforced industry-level practices and significantly improved my confidence in designing and validating large-scale systems.

---

## 🎓 Course Information

* **University:** The University of Sydney
* **Unit:** INFO1113 – Object Oriented Programming
* **Assessment Type:** Multi-Part Programming Assignment
* **Semester:** Semester 2
