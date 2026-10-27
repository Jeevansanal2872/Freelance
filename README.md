# Freelance . — Premium Desktop Freelance Marketplace

A high-performance desktop freelance marketplace built entirely in **Java Swing** with a **modern minimalist design system** (acrylic glass effects, clean `#00B2FF` palette, and fluid typography) backed by a robust **Java 25 & Spring Boot 3** REST API.

---

## 🎨 Design System & Aesthetics

- **Color Palette**:
  - **Electric Cyan/Blue**: `#00B2FF` (Primary accent, focus rings, tag badges, progress fills)
  - **Pure White**: `#FFFFFF` (Surface cards and contrast text)
  - **Near-Black / Obsidian**: `#000000` / `#0C0D12` / `#1D1D1F` (Dark hero banners, buttons, typography)
  - **Neutral Grays**: `#F5F5F7` (Canvas background), `#E8E8ED` (Subtle dividers), `#86868B` (Secondary captions)
- **Glassmorphism & Materials**:
  - Translucent frosted glass panels (`GlassPanel`) with anti-aliased curved borders and layered soft drop shadows.
  - Dark glass hero banners (`GradientPanel`) with deep obsidian gradients.
  - Glowing `#00B2FF` status indicator dots and skill tags (`#00B2FF` translucent glass pill badges).
  - Modern typography hierarchy with anti-aliasing enabled globally.

---

## Architecture

```
┌────────────────────────────────────────────────────────┐
│               Swing Desktop Application                │
│        (Modern UI/UX, Glass Cards, Fluid Nav)          │
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

---

## Features

1. **User Authentication & Instant Demo Login**
   - BCrypt password hashing.
   - Quick one-click demo credentials:
     - **Demo Client**: `client@nexus.demo` / `password123`
     - **Demo Freelancer**: `Jeevan@engineer.demo` / `password123`
   - Guest browsing without initial login.

2. **Marketplace & Browse Jobs**
   - Instant search across job titles, descriptions, and skills.
   - Category filter pills (*Engineering*, *Design*, *AI & Data*, *Marketing*, *Writing*).
   - Glass cards with budget badges, status indicators, and skills tags.

3. **Job Detail & Hiring Flow**
   - Complete project overview and deliverables breakdown.
   - Freelancer proposal submission (Bid amount, estimated days, cover letter).
   - Client proposal review: "Hire Freelancer" (auto-creates active contract) or "Reject".
   - **AI Resume Compatibility Analyzer**: evaluate resume match against project qualifications.

4. **Client Job Publisher**
   - Live real-time preview card updating dynamically as you compose the listing.
   - Instant publishing to the live marketplace.

5. **Contracts & Engagements Tracker**
   - Lifecycle management: *Active*, *Completed*, *Cancelled*.
   - Client completion sign-off with feedback notes and payment release.
   - Cancel contract flow with reason logging.

6. **Interactive Dashboard & Profile Strength**
   - Frosted glass metric cards: Active Jobs, Proposals Sent, Active Contracts, Completed Projects.
   - Dynamic profile strength progress bar with live percentage calculation.
   - Comprehensive profile editor (Hourly rate, bio, portfolio, education, languages, GitHub).

---

## How to Run

### Run the Application (Spring Boot + Swing UI)

Ensure Java 25+ and Maven are available, then run:

```bash
cd java-app
mvn spring-boot:run
```

*The application starts the backend on `http://localhost:8080`, seeds demo marketplace data, and automatically launches the modern Swing GUI window.*

### Run Standalone Swing GUI

If the Spring Boot server is already running:

```bash
cd java-app
mvn compile exec:java -Dexec.mainClass="com.freelance.ui.MainFrame"
```

### Run All Unit Tests (60 Test Cases)

```bash
cd java-app
mvn test
```

---

## Tech Stack

- **Desktop UI**: Java Swing (Custom glass components, `Graphics2D`, anti-aliased rendering)
- **Backend API**: Java 25, Spring Boot 3.5, Spring Data JPA, Spring Security (BCrypt)
- **Database**: H2 In-Memory Database (with H2 Console at `http://localhost:8080/h2-console`)
- **Testing**: JUnit 5, Mockito (60 test cases, 100% passing)
