# PROMPTS.md — AI Prompt Log

Record every prompt you send to the AI **starting from Slice 2**.  
Slice 1 is done manually — no AI. See `README.md` for the prompt template structure.

> **Rules:**
> - One prompt (+ follow-ups) per slice
> - Use the structured template from the README — free-form prompts are not allowed
> - Do not back-fill this file after the lab

---

## Example — How a good S1 prompt looks (for reference only, S1 is done manually)

```
CONTEXT
Spring Boot 3.x, Spring Security 6, Java 17+, jjwt 0.12.x. No other libraries.
Package layout:
  com.esd.lab2
  ├── config/       SecurityConfig.java, AdminSeeder.java
  ├── controller/   CourseController.java
  ├── service/      CourseService.java
  ├── dto/          CourseRequest.java, CourseResponse.java
  ├── entity/       User.java, Role.java (enum), Course.java
  ├── repository/   UserRepository.java, CourseRepository.java
  ├── exception/    ApiError.java, GlobalExceptionHandler.java,
  │                 DuplicateResourceException.java, InvalidCredentialsException.java,
  │                 ResourceNotFoundException.java
  └── security/     JsonAuthenticationEntryPoint.java, JsonAccessDeniedHandler.java


TASK
Implement only POST /api/auth/signup.
Request: SignupRequest { email (valid email, required), password (required, 8–64 chars),
         fullName (required, not blank, max 100) }.
Response: 201 with UserResponse { id, email, fullName, role, createdAt }.
The new user's role is always STUDENT, even if the request contains a role field.
Store email trimmed and lowercased. Hash the password with the existing PasswordEncoder bean.
If the email already exists, throw DuplicateResourceException.

ALLOWED TO CREATE OR CHANGE
dto/SignupRequest.java, dto/UserResponse.java,
service/AuthService.java (signup method only),
controller/AuthController.java (signup endpoint only)

RULES
- Do not create or modify any file not listed above.
- Use the existing exceptions in com.esd.lab2.exception. Do not create new error classes.
- Use DTOs for request and response. Never return an entity from a controller.
- Use jakarta.validation annotations for input validation.
- Use only APIs that exist in Spring Security 6 and jjwt 0.12. No deprecated methods.
- If something is unclear, add a TODO comment instead of guessing.

OUTPUT
Each file in full, with its path. Then a list of anything you assumed.
```

---

## Slice 2 — Login & JWT (S2)

**Prompt 1:**

```
CONTEXT
Spring Boot 3.x, Spring Security 6, Java 17+, jjwt 0.12.x.
Package layout:
  com.esd.lab2
  ├── config/       SecurityConfig.java, AdminSeeder.java
  ├── controller/   AuthController.java, CourseController.java
  ├── service/      AuthService.java, CourseService.java
  ├── dto/          SignupRequest.java, UserResponse.java, CourseRequest.java, CourseResponse.java
  ├── entity/       User.java, Role.java (enum), Course.java
  ├── repository/   UserRepository.java, CourseRepository.java
  ├── exception/    ApiError.java, GlobalExceptionHandler.java,
  │                 DuplicateResourceException.java, InvalidCredentialsException.java,
  │                 ResourceNotFoundException.java
  └── security/     JsonAuthenticationEntryPoint.java, JsonAccessDeniedHandler.java

Existing code:
- UserRepository has findByEmail(email)
- Injected PasswordEncoder bean (use passwordEncoder.matches)
- User entity has getEmail(), getPasswordHash(), getRole()
- application.yml has jwt.secret (Base64-encoded, min 32 bytes) and jwt.expiration-seconds (3600)

TASK
Implement POST /api/auth/login and JWT generation based on the README steps:

1. Create LoginRequest record with @NotBlank and @Email on email, and @NotBlank on password.
2. Create LoginResponse record with fields accessToken (String), tokenType ("Bearer"), and expiresIn (long seconds matching configured expiration).
3. Create security/JwtService.java:
   - Read jwt.secret and jwt.expiration-seconds from @Value.
   - Implement generateToken(User user) using JJWT 0.12 API.
   - Claims: sub = email, role, jti = random UUID, iat, and exp.
   - Sign with HMAC-SHA256 using the configured secret.
   - Do NOT implement token parsing yet (that belongs to S3).
4. In AuthService.java:
   - Inject JwtService.
   - Implement login(LoginRequest): look up user by trimmed lowercase email, verify password with passwordEncoder.matches().
   - If user is not found or password does not match, throw InvalidCredentialsException with message "Invalid email or password".
   - Return LoginResponse with the generated token.
   - Do not use AuthenticationManager or UserDetailsService.
5. In AuthController.java:
   - Add POST /api/auth/login with @Valid @RequestBody, call authService.login, and return 200 OK.

ALLOWED TO CREATE OR CHANGE
dto/LoginRequest.java,
dto/LoginResponse.java,
security/JwtService.java (generateToken method only),
service/AuthService.java (login method and JwtService injection only),
controller/AuthController.java (login endpoint only)

RULES
- Do not modify SecurityConfig or create any filter yet.
- Use existing exceptions from com.esd.lab2.exception (InvalidCredentialsException).
- Error message must be identical ("Invalid email or password") whether email is wrong or password is wrong.
- Never include password or password hash in token claims.
- The jti claim must be a random UUID, not the database id.
- Use current jjwt 0.12 modern API (.subject(), .claim(), .id(), .issuedAt(), .expiration(), .signWith(key)). Do not use deprecated setSubject(), setExpiration(), or signWith(SignatureAlgorithm, ...).
- Output full files with their file paths.

OUTPUT
Each file in full, with its path. Then a list of anything you assumed.
```

**Follow-up (if any):**

```
(paste here)
```

---

## Slice 3 — JWT Filter & Protected API (S3)

**Prompt 1:**

```
CONTEXT
Spring Boot 3.x, Spring Security 6, Java 17+, jjwt 0.12.x.
Package layout:
  com.esd.lab2
  ├── config/       SecurityConfig.java, AdminSeeder.java
  ├── controller/   AuthController.java, CourseController.java
  ├── service/      AuthService.java, CourseService.java
  ├── dto/          SignupRequest.java, UserResponse.java,
  │                 LoginRequest.java, LoginResponse.java,
  │                 CourseRequest.java, CourseResponse.java
  ├── entity/       User.java, Role.java (enum), Course.java
  ├── repository/   UserRepository.java, CourseRepository.java
  ├── exception/    ApiError.java, GlobalExceptionHandler.java, ResourceNotFoundException.java
  └── security/     JwtService.java,
                    JsonAuthenticationEntryPoint.java, JsonAccessDeniedHandler.java

Existing code to use:
- JwtService from S2 (already has signing key and generateToken)
- JsonAuthenticationEntryPoint (writes ApiError for 401)
- JsonAccessDeniedHandler (writes ApiError for 403)
- UserRepository has findByEmail(email)
- UserResponse record: { id, email, fullName, role, createdAt }

TASK
Implement Slice 3: JWT Filter, Spring Security configuration, and the current user endpoint (contract rows P1 and P2).

1. In security/JwtService.java:
   - Add parseToken(String token) method that verifies signature and expiration, and extracts Claims using JJWT 0.12 parser.

2. Create security/JwtAuthenticationFilter.java:
   - Extends OncePerRequestFilter.
   - Extracts token from "Authorization: Bearer <token>" header.
   - If header is missing or does not start with "Bearer ", continue filter chain.
   - If token is present, parse claims using jwtService.parseToken(token), extract email (subject) and role, and set UsernamePasswordAuthenticationToken in SecurityContextHolder with authority "ROLE_" + role.
   - If token parsing fails (bad signature, expired, or malformed), clear SecurityContext and continue filter chain. Do not write a response directly from the filter.

3. In config/SecurityConfig.java:
   - Replace the temporary filterChain bean using Spring Security 6 lambda DSL.
   - Disable CSRF and set session creation policy to STATELESS.
   - Public endpoints (permitAll):
     - POST /api/auth/signup
     - POST /api/auth/login
     - GET /api/courses (GET only, POST /api/courses remains protected)
   - All other endpoints require authentication (anyRequest().authenticated()).
   - Wire existing JsonAuthenticationEntryPoint and JsonAccessDeniedHandler in exceptionHandling.
   - Add JwtAuthenticationFilter before UsernamePasswordAuthenticationFilter.
   - Do not add UserDetailsService, AuthenticationManager, or CORS config.

4. Create controller/UserController.java:
   - Add GET /api/users/me endpoint requiring authentication.
   - Read email from Authentication principal (authentication.getName()).
   - Fetch User from UserRepository by email and return UserResponse (200 OK). Throw ResourceNotFoundException if not found.

ALLOWED TO CREATE OR CHANGE
security/JwtService.java (add parseToken method),
security/JwtAuthenticationFilter.java,
config/SecurityConfig.java (replace filterChain bean only),
controller/UserController.java

RULES
- Use Spring Security 6 style only (SecurityFilterChain, lambda DSL, requestMatchers, authorizeHttpRequests). No deprecated methods (antMatchers, authorizeRequests, .and()).
- permitAll must strictly cover only POST /api/auth/signup, POST /api/auth/login, and GET /api/courses.
- Filter must not write responses on error—it must clear context and let the filter chain proceed so JsonAuthenticationEntryPoint handles 401.
- Authorities must use "ROLE_" prefix.
- GET /api/users/me must return UserResponse DTO, never the entity.
- Output each file in full with its path.

OUTPUT
Each file in full, with its path. Then a list of anything you assumed.
```

**Follow-up (if any):**

```
(paste here)
```

---

## Slice 4 — Role Restriction (S4)

**Prompt 1:**

```
CONTEXT
Spring Boot 3.x, Spring Security 6, Java 17+.
Existing code:
- SecurityConfig.java from S3 (has filterChain with requestMatchers and exceptionHandling)
- CourseController.java (has GET /api/courses and POST /api/courses)
- JsonAccessDeniedHandler.java (already wired to return 403 ApiError for forbidden access)
- Authorities in JWT filter are set as "ROLE_" + role (e.g. ROLE_ADMIN, ROLE_STUDENT)

TASK
Implement Slice 4: Restrict course creation to users with role ADMIN (Contract row P3).

1. In config/SecurityConfig.java:
   - Restrict POST /api/courses to callers with role ADMIN using .requestMatchers(HttpMethod.POST, "/api/courses").hasRole("ADMIN").
   - Place this rule immediately before .anyRequest().authenticated().
   - Keep all other existing configurations intact (stateless sessions, CSRF disabled, public matchers, exception handlers, and JWT filter).

ALLOWED TO CREATE OR CHANGE
config/SecurityConfig.java

RULES
- Only modify SecurityConfig.java. Keep changes minimal.
- Use hasRole("ADMIN") (which matches the "ROLE_ADMIN" authority set in S3).
- Place the role restriction rule before anyRequest().authenticated().
- Do not modify CourseController or rewrite the security configuration.
- Output the full file with its path.

OUTPUT
Each file in full, with its path. Then a list of anything you assumed.
```

**Follow-up (if any):**

```
(paste here)
```

---

## Slice 5 — Logout *(stretch, S5)*

**Prompt 1:**

```
CONTEXT
Spring Boot 3.x, Spring Security 6, Java 17+, jjwt 0.12.x.
Existing code:
- JwtService has parseToken(token) returning Claims (claims.getId() is jti, claims.getExpiration() is expiry)
- JwtAuthenticationFilter intercepts requests and parses JWT
- AuthController has signup and login endpoints
- SecurityConfig has POST /api/auth/signup and POST /api/auth/login as permitAll (POST /api/auth/logout is already protected by anyRequest().authenticated())

TASK
Implement Slice 5: Server-side token invalidation / logout (Contract row A3).

1. Create security/TokenDenylist.java:
   - An in-memory thread-safe @Component holding a ConcurrentHashMap<String, Instant> mapping jti -> expiry.
   - revoke(String jti, Instant expiry): adds the jti to the map and cleans up expired entries.
   - isRevoked(String jti): returns true if the jti is present in the map.
   - cleanExpired(): removes entries whose expiry time is before Instant.now().

2. In security/JwtAuthenticationFilter.java:
   - Inject TokenDenylist.
   - After parsing claims, check if tokenDenylist.isRevoked(claims.getId()).
   - If revoked, clear SecurityContext and do not set authentication.

3. In controller/AuthController.java:
   - Add POST /api/auth/logout endpoint (returns 204 No Content).
   - Extract the Bearer token from the Authorization header.
   - Parse claims using jwtService to get jti (claims.getId()) and expiry (claims.getExpiration().toInstant()).
   - Call tokenDenylist.revoke(jti, expiry) and return ResponseEntity.noContent().build().

ALLOWED TO CREATE OR CHANGE
security/TokenDenylist.java,
security/JwtAuthenticationFilter.java,
controller/AuthController.java

RULES
- Logout must change server state by tracking revoked jti on the server (do not just tell the client to delete it).
- Key the denylist by jti, never by email.
- Use thread-safe ConcurrentHashMap.
- Do not add /api/auth/logout to permitAll in SecurityConfig (it must be protected).
- Output full files with their paths.

OUTPUT
Each file in full, with its path. Then a list of anything you assumed.
```

**Follow-up (if any):**

```
(paste here)
```
