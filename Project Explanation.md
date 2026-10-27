# Freelance . — The Complete Project Guide & Architecture

---

## 1. The Human Story & Why We Built Freelance .

Every year, thousands of students, self-taught creators, and early-career developers try to find their first freelance projects. But when they open platforms like Upwork or Fiverr, they immediately face roadblocks:
- **Pay-to-Apply Paywalls:** Upwork charges for "Connects" ($0.15 each, with jobs requiring up to 16 connects per bid). A talented student with no spare cash is blocked from submitting their resume.
- **Heavy Commission Fees:** Fiverr automatically takes 20% of whatever a freelancer earns, while flooding the market with saturated "gigs" where people compete in a race to the bottom.
- **Web App Fatigue:** Modern web apps have become bloated. Opening multiple 500MB browser tabs with laggy spinners and slow menus makes searching for work feel exhausting.
- **The Proposal Black Hole:** Freelancers apply blindly to jobs without knowing if their resume matches what the client actually needs. Clients, in turn, are bombarded by hundreds of generic, copy-pasted spam proposals.

We built **Freelance .** to solve these real-world problems:
1. **100% Free to Apply:** Zero bidding fees, zero token paywalls. True merit and skill always come first.
2. **A Native Desktop Experience:** Built completely in pure Java Swing with a modern minimalist design system. It launches in 2 seconds, renders at 60 FPS, and consumes a fraction of the RAM of a web browser.
3. **Helpful AI Matchmaker:** Powered by Google Gemini 1.5 Flash, it acts like a friendly senior mentor who reviews your resume against the specific job, gives an honest compatibility score, and drafts a warm, natural proposal opening.
4. **Zero-Setup SQL Persistence:** An embedded H2 database boots automatically inside the app, creating tables and loading 12 realistic marketplace opportunities across Design, AI, and Engineering.

---

## 2. A Day in the Life: How Real People Use the App

### The Client Experience (Alexander Vance, Nexus Global Studios)
1. **Posting Work without Friction:** Alexander opens `Post Job`, enters his project scope, budget, and required tools. As he types, a live card preview renders on the right side of the screen so he sees exactly how applicants will view his post.
2. **Reviewing Quality Proposals:** Instead of hundreds of spam bots, Alexander reviews proposals with clear bid amounts, realistic timelines, and candidate bios.
3. **One-Click Hiring:** Alexander clicks "Hire" on the best candidate. The job status immediately transitions from `OPEN` to `CLOSED`, and an active contract is created.
4. **Inspecting & Approving:** Once the deliverable is completed, Alexander inspects the work, clicks "Complete & Pay", and leaves a completion note.

### The Freelancer Experience ( Jeevan S , Graphic Designer)
1. **Browsing with Zero Lag:** Jeevan searches opportunities across Engineering, AI, Design, and Marketing using real-time typing filters and category pill buttons.
2. **Checking Fit Upfront:** On an interesting Spring Boot migration listing, Jeevan clicks "Check My Resume". The Gemini AI reviews her background and awards an honest 88% match.
3. **One-Click Pitch Insertion:** The AI generates a natural opening message highlighting Jeevan's real skills. Jeevan clicks "Insert Pitch into Proposal", sets her bid to $7,400, and submits.
4. **Tracking Contracts:** Jeevan tracks active deliverables on her dashboard, marks milestones, and receives client sign-offs.

---

## 3. Executive Summary & Architecture Overview

```
┌────────────────────────────────────────────────────────┐
│               Swing Desktop Application                │
│       (Modern UI/UX, Glass Cards, Fluid Navigation)    │
│                       MainFrame                        │
│   ┌───────────────┬─────────────────┬──────────────┐   │
│   │ Browse Jobs   │ Job Detail      │ Post Job     │   │
│   ├───────────────┼─────────────────┼──────────────┤   │
│   │ Dashboard     │ My Proposals    │ Contracts    │   │
│   ├───────────────┴─────────────────┴──────────────┤   │
│   │ Profile & AI Resume Compatibility              │   │
│   └────────────────────────────────────────────────┘   │
└──────────────────────────┬─────────────────────────────┘
                           │ HTTP REST (JSON)
                           ▼
┌────────────────────────────────────────────────────────┐
│            Spring Boot 3 + H2 Database API             │
│            Port 8080 (Java 25, JPA, BCrypt)            │
└────────────────────────────────────────────────────────┘
```

### 3.1 Architecture Highlights
1. **Frontend**: Pure Java Swing (`MainFrame`, `BrowseJobsPanel`, `JobDetailPanel`, `PostJobPanel`, `DashboardPanel`, `MyContractsPanel`, `MyProposalsPanel`, `ProfilePanel`).
2. **Communication**: `ApiClient.java` makes HTTP calls (`GET`, `POST`, `PUT`, `DELETE`) to the Spring Boot REST endpoints on `http://localhost:8080`.
3. **Threading**: Heavy network calls execute asynchronously via `SwingWorker` threads, keeping the Event Dispatch Thread (EDT) responsive and fluid.
4. **Backend API**: Spring Boot 3 controllers (`UserController`, `JobController`, `ProposalController`, `ContractController`, `ResumeCompatibilityController`).
5. **Persistence**: Spring Data JPA repositories (`UserRepository`, `JobRepository`, `ProposalRepository`, `ContractRepository`) backed by an embedded H2 SQL database (`jdbc:h2:mem:freelancedb`).
6. **Artificial Intelligence**: Google Gemini 1.5 Flash API integration for resume semantic parsing and match analysis, backed by a fault-tolerant word-bounded regex heuristic engine.

---

## 4. How to Run the Project

```bash
cd java-app
mvn spring-boot:run
```

- **Demo Client**: `client@nexus.demo` / `password123`
- **Demo Freelancer**: `Jeevan@engineer.demo` / `password123`
- **H2 SQL Database Console**: `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:freelancedb`, User: `sa`, Password: *blank*)
