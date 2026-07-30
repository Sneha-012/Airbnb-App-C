# Airbnb Clone – Backend API

A Spring Boot backend for a property rental and booking platform, inspired by Airbnb. Built as a personal project to practice real-world backend architecture, JPA relationships, and REST API design.

## Features
- Property, Room, and per-day Inventory management with dynamic pricing
- Date-range availability search (checks every night of a stay, not just single days)
- Booking and Guest management
- DTO-based request/response contracts (no entity leakage)
- Centralized global exception handling with consistent error responses
- Input validation with Jakarta Bean Validation
- Stripe integration for payments *(in progress)*
- Spring Security with JWT authentication *(in progress)*

## Tech Stack
Java · Spring Boot · Spring Data JPA · Hibernate · PostgreSQL · Maven · Lombok · ModelMapper

## Architecture
Controller → Service → Repository, with a clean separation between entities and DTOs to keep the API contract independent of the database schema
