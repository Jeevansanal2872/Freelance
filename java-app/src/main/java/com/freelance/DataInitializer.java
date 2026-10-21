package com.freelance;

import com.freelance.contract.Contract;
import com.freelance.contract.ContractRepository;
import com.freelance.job.Job;
import com.freelance.job.JobRepository;
import com.freelance.proposal.Proposal;
import com.freelance.proposal.ProposalRepository;
import com.freelance.user.User;
import com.freelance.user.UserRepository;
import com.freelance.user.UserRole;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * DataInitializer — Seeds high-quality demo data on startup.
 * Populates authentic Upwork-style jobs across Design, AI, Engineering, Writing, and Marketing.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepo;
    private final JobRepository jobRepo;
    private final ProposalRepository proposalRepo;
    private final ContractRepository contractRepo;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(
            UserRepository userRepo,
            JobRepository jobRepo,
            ProposalRepository proposalRepo,
            ContractRepository contractRepo,
            PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.jobRepo = jobRepo;
        this.proposalRepo = proposalRepo;
        this.contractRepo = contractRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepo.count() > 0) return; // Already seeded

        String encodedPass = passwordEncoder.encode("password123");

        // ── 1. Demo Client ─────────────────────────────────────────────────────
        User client = new User("Nexus Global Studios", "client@nexus.demo", encodedPass, UserRole.CLIENT);
        client.setFirstName("Alexander");
        client.setLastName("Vance");
        client.setBio("Building transcendent software experiences that inspire humanity. We partner with elite independent creators who obsess over design, performance, and craftsmanship.");
        client.setSkills("Product Vision, Design Systems, Strategic Leadership, Enterprise Standards");
        client.setPortfolioUrl("https://nexusstudios.demo");
        client.setEducation("Stanford University");
        userRepo.save(client);

        // ── 2. Demo Freelancers ────────────────────────────────────────────────
        User Jeevan = new User("Jeevan Chen", "Jeevan@engineer.demo", encodedPass, UserRole.FREELANCER);
        Jeevan.setFirstName("Jeevan");
        Jeevan.setLastName("Chen");
        Jeevan.setBio("Staff Software Engineer with 9+ years architecting high-throughput Java microservices, Spring Boot backends, Kafka event pipelines, and distributed cloud infrastructure.");
        Jeevan.setSkills("Java, Spring Boot, Microservices, Redis, Kafka, Docker, Kubernetes, AWS, PostgreSQL");
        Jeevan.setHourlyRate(135.0);
        Jeevan.setPortfolioUrl("https://Jeevanchen.dev");
        Jeevan.setEducation("M.S. Computer Science, Stanford University");
        Jeevan.setLanguages("English, Mandarin");
        Jeevan.setHoursPerWeek(35);
        Jeevan.setGithubUrl("https://github.com/Jeevanchen");
        userRepo.save(Jeevan);

        User marcus = new User("Marcus Vance", "marcus@design.demo", encodedPass, UserRole.FREELANCER);
        marcus.setFirstName("Marcus");
        marcus.setLastName("Vance");
        marcus.setBio("Lead UI/UX Architect crafting minimalist, tactile glass interfaces following modern Human Interface Guidelines. Specializing in high-fidelity Figma design systems and micro-interactions.");
        marcus.setSkills("Figma, Modern HIG, UI/UX Architecture, Design Systems, Typography, Wireframing, Prototyping");
        marcus.setHourlyRate(120.0);
        marcus.setPortfolioUrl("https://marcusvance.design");
        marcus.setEducation("B.A. Graphic Design, RISD");
        marcus.setLanguages("English, French");
        marcus.setHoursPerWeek(40);
        userRepo.save(marcus);

        // ── 3. Authentic Upwork-Style Jobs (12 Listings) ───────────────────────

        // ── ENGINEERING & DEVELOPER ───────────────────────────────────────────
        Job job1 = new Job(
            client.getId(),
            "Senior Distributed Systems Architect",
            "We are architecting the next-generation global data pipeline. Looking for an expert in Java 17+, low-latency REST APIs, and event-driven architectures to design our resilient core backbone. Must have extensive experience with Redis caching and Kafka streaming.",
            8500.0,
            "Engineering",
            "Java, Spring Boot, Microservices, Redis, Kafka, Docker",
            LocalDateTime.now().plusWeeks(4)
        );
        jobRepo.save(job1);

        Job job2 = new Job(
            client.getId(),
            "Lead Spring Boot Microservices & Cloud Infrastructure Engineer",
            "Seeking an experienced backend engineer to architect and deploy production microservices. You will establish zero-downtime CI/CD workflows, configure Kubernetes deployments on AWS EKS, and secure APIs with OAuth2/JWT.",
            10200.0,
            "Engineering",
            "Java, Spring Boot, Kubernetes, AWS, Docker, PostgreSQL, REST",
            LocalDateTime.now().plusWeeks(6)
        );
        jobRepo.save(job2);

        Job job3 = new Job(
            client.getId(),
            "Full-Stack Engineer - Modern Java 21, Spring Boot & React / Next.js Migration",
            "We are upgrading legacy enterprise services to Java 21 Virtual Threads and modern Spring Boot 3. Looking for an engineer who writes elegant code, enforces rigorous unit test coverage, and understands clean architecture.",
            7400.0,
            "Engineering",
            "Java, Spring Boot, REST, JUnit, Git, Testing",
            LocalDateTime.now().plusWeeks(4)
        );
        jobRepo.save(job3);

        Job job4 = new Job(
            client.getId(),
            "Senior Database Performance Tuning & SQL Optimization Specialist",
            "Analyze and optimize slow-running SQL queries, schema indexing, and connection pool bottlenecks across high-traffic PostgreSQL and MySQL clusters. You will implement query profiling and partition large datasets.",
            5800.0,
            "Engineering",
            "SQL, PostgreSQL, MySQL, Database Tuning, Query Optimization",
            LocalDateTime.now().plusWeeks(3)
        );
        jobRepo.save(job4);

        // ── DESIGN ────────────────────────────────────────────────────────────
        Job job5 = new Job(
            client.getId(),
            "Modern Glass UI/UX Design System Specialist",
            "Crafting a bespoke desktop workspace with translucent acrylic aesthetics, subtle depth shadows, and minimalist typography. Need an expert in modern Human Interface Guidelines and Figma.",
            6200.0,
            "Design",
            "Figma, UI/UX, Design Systems, Modern HIG, Typography",
            LocalDateTime.now().plusWeeks(3)
        );
        jobRepo.save(job5);

        Job job6 = new Job(
            client.getId(),
            "Senior Product Designer - Next-Gen Mobile Design Language & Figma Components",
            "Design a complete component library in Figma for our flagship mobile application. Deliverables include dark and light mode tokens, fluid gestures, haptic feedback states, and interactive prototypes adhering to modern HIG.",
            7500.0,
            "Design",
            "Figma, Mobile UI/UX, Design Systems, Prototyping",
            LocalDateTime.now().plusWeeks(4)
        );
        jobRepo.save(job6);

        Job job7 = new Job(
            client.getId(),
            "3D Motion Graphics & Interactive WebGL Experience Designer",
            "Design interactive 3D product showcase elements and micro-motion interactions for our web and desktop presentations. Seeking proficiency in Spline, Blender, Three.js, and premium product reveals.",
            6400.0,
            "Design",
            "3D Design, Blender, Spline, UI Motion, Figma",
            LocalDateTime.now().plusWeeks(5)
        );
        jobRepo.save(job7);

        // ── AI & DATA ─────────────────────────────────────────────────────────
        Job job8 = new Job(
            client.getId(),
            "AI / LLM Integration & Inference Optimization Specialist",
            "Implement high-performance local & cloud inference pipelines for resume matching and document intelligence. Focus on speed, precision, Google Gemini API integration, and graceful fallbacks.",
            9000.0,
            "AI & Data",
            "Python, AI, LLMs, Google Gemini, Spring Boot, REST",
            LocalDateTime.now().plusWeeks(5)
        );
        jobRepo.save(job8);

        Job job9 = new Job(
            client.getId(),
            "Claude AI Prompt Engineer & Multi-Agent Workflow Architect",
            "We need an expert to design autonomous AI workflows using Claude 3.5 Sonnet and Gemini. You will construct reliable prompt evaluations, function calling hooks, and structured JSON parsing pipelines for enterprise automation.",
            8200.0,
            "AI & Data",
            "Prompt Engineering, AI Agents, Python, JSON, LLMs, LangChain",
            LocalDateTime.now().plusWeeks(4)
        );
        jobRepo.save(job9);

        Job job10 = new Job(
            client.getId(),
            "Computer Vision & Multimodal Document Intelligence Pipeline",
            "Build an end-to-end multimodal pipeline to extract structured data from unstructured resumes, invoices, and contracts. Must incorporate OCR preprocessing, layout analysis, and confidence scoring.",
            7800.0,
            "AI & Data",
            "Computer Vision, Python, PyTorch, OCR, NLP, Machine Learning",
            LocalDateTime.now().plusWeeks(5)
        );
        jobRepo.save(job10);

        // ── MARKETING ─────────────────────────────────────────────────────────
        Job job11 = new Job(
            client.getId(),
            "Brand Narrative & Global Studio Launch Strategy",
            "Define the tone, core messaging, and creative launch story for an independent high-craft developer tooling suite. Deliver a multi-channel go-to-market narrative, PR angle, and customer acquisition playbook.",
            3800.0,
            "Marketing",
            "Brand Strategy, Copywriting, Creative Direction, Go-To-Market",
            LocalDateTime.now().plusWeeks(2)
        );
        jobRepo.save(job11);

        // ── WRITING ───────────────────────────────────────────────────────────
        Job job12 = new Job(
            client.getId(),
            "Lead Technical Content Writer & Developer SDK Documentation",
            "Author comprehensive, publication-quality documentation for our core REST APIs, client SDKs, and developer portal. Must produce clear code samples, architectural diagrams, and interactive tutorial walkthroughs.",
            4500.0,
            "Writing",
            "Technical Writing, API Documentation, Markdown, Developer Guides",
            LocalDateTime.now().plusWeeks(3)
        );
        jobRepo.save(job12);

        // ── 4. Seed Realistic Proposals ────────────────────────────────────────
        Proposal prop1 = new Proposal(
            job1.getId(),
            Jeevan.getId(),
            "I have 9+ years architecting enterprise Java and Spring Boot backends capable of tens of thousands of requests per second. I'd love to deliver this robust backbone with comprehensive unit tests and clean documentation.",
            8200.0,
            24
        );
        prop1.accept();
        proposalRepo.save(prop1);

        Proposal prop2 = new Proposal(
            job5.getId(),
            marcus.getId(),
            "Minimalist glass aesthetics are my primary focus. I will construct a cohesive token library, translucent materials, and fluid interactions that make the interface feel extraordinary.",
            6000.0,
            18
        );
        proposalRepo.save(prop2);

        // ── 5. Seed Active Contract ───────────────────────────────────────────
        Contract contract = new Contract(
            job1.getId(),
            client.getId(),
            Jeevan.getId(),
            prop1.getId(),
            8200.0
        );
        contractRepo.save(contract);
    }
}
