# REVIEW.md — AI Output Review Log

Complete **one review entry per slice** before moving to the next.  


---

## Slice 1 — Signup (S1)

### A. Bucket Check
List every class and method the AI produced. Tag each one:  
`Controller` / `Service` / `Repository` / `DTO` / `Security` / `Config` / `Exception`

| File / Method | Tag | In Allowed List? |
|---|---|---|
| `dto/SignupRequest.java` | DTO | Yes |
| `dto/UserResponse.java` | DTO | Yes |
| `service/AuthService.java` (`signup`) | Service | Yes |
| `controller/AuthController.java` (`signup`) | Controller | Yes |

> **Important:** Anything **not** in the allowed list → **reject, re-prompt**. Don't keep it because it works.

### B. Contract Check
Compare against API contract row A1 line by line.

| Check | Pass / Fail |
|---|---|
| Path is `POST /api/auth/signup` | Pass |
| Returns **201** | Pass |
| Response shape: `{ id, email, fullName, role, createdAt }` | Pass |
| No `passwordHash` field in response | Pass |
| Role in response is always `STUDENT` | Pass |

### C. Validation & Errors

| Check | Pass / Fail |
|---|---|
| `@Valid` on controller parameter | Pass |
| `@Email`, `@NotBlank`, `@Size` on DTO fields | Pass |
| Duplicate email throws `DuplicateResourceException` | Pass |
| No new error/response wrapper class invented | Pass |
| No stack trace or exception message leaked to client | Pass |

### D. Security Specifics

| Check | Pass / Fail |
|---|---|
| Password hashed via injected `PasswordEncoder` (not `new BCryptPasswordEncoder()`) | Pass |
| `SignupRequest` has no `role` field, or it is ignored | Pass |
| Duplicate check uses normalised (lowercased/trimmed) email | Pass |
| AI did **not** touch `SecurityConfig`, add JWT class, or add `UserDetailsService` | Pass |

### E. Understanding Questions
*Answer in your own words (1–2 lines each)*

1. Where exactly is the password hashed, and why is the same password stored as a different string for two users?

   > *Your answer:* In `AuthService.signup()` using `passwordEncoder.encode()`. It's stored differently because BCrypt generates a random salt for each hash, so even identical passwords produce different hashes.

2. What would happen if `SignupRequest` accepted a `role` field and the AI used it?

   > *Your answer:* Anyone signing up could send `"role": "ADMIN"` in the request body and make themselves an admin (privilege escalation).

3. Which class turns `DuplicateResourceException` into a 409 response?

   > *Your answer:* `GlobalExceptionHandler`. It has an `@ExceptionHandler(DuplicateResourceException.class)` method that returns a 409 (CONFLICT) `ApiError`.

### F. Decision

| File | Decision | Reason (if modified/rejected) |
|---|---|---|
| `SignupRequest.java` | Accepted | Validations (@NotBlank, @Email, @Size) are correct and role field is omitted. |
| `UserResponse.java` | Accepted | Matches expected response fields and leaves out passwordHash. |
| `AuthService.java` | Accepted | Uses injected PasswordEncoder, normalises email, and forces Role.STUDENT. |
| `AuthController.java` | Accepted | Handles POST /api/auth/signup with @Valid and returns 201 Created. |

---

## Slice 2 — Login & JWT (S2)

### A. Bucket Check

| File / Method | Tag | In Allowed List? |
|---|---|---|
| `dto/LoginRequest.java` | DTO | Yes |
| `dto/LoginResponse.java` | DTO | Yes |
| `security/JwtService.java` (`generateToken`, `getExpirationSeconds`) | Security | Yes |
| `service/AuthService.java` (`login`) | Service | Yes |
| `controller/AuthController.java` (`login`) | Controller | Yes |

### B. Contract Check

| Check | Pass / Fail |
|---|---|
| Path is `POST /api/auth/login` | Pass |
| Returns **200** | Pass |
| Response: `{ accessToken, tokenType: "Bearer", expiresIn }` | Pass |
| `expiresIn` matches configured value (not hardcoded) | Pass |

### C. Validation & Errors

| Check | Pass / Fail |
|---|---|
| Same 401 message for unknown email and wrong password | Pass |
| No `try/catch` swallowing exceptions silently | Pass |

### D. Security Specifics

| Check | Pass / Fail |
|---|---|
| JWT secret comes from `@Value`, no string literal secret | Pass |
| Uses jjwt 0.12 API: `.subject()`, `.claim()`, `.id()`, `.issuedAt()`, `.expiration()`, `.signWith(key)` | Pass |
| Rejects deprecated: `setSubject`, `setExpiration`, `signWith(SignatureAlgorithm.HS256, secret)`, `Jwts.parserBuilder()` | Pass |
| `jti` is a random UUID (not the user id) | Pass |
| No password or hash in token claims | Pass |
| Parse/validate method **not** written yet (that is S3) | Pass |

### E. Understanding Questions

1. Why must the error message be identical for "no such email" and "wrong password"?

   > *Your answer:* If the messages differ, an attacker can figure out which emails are registered on the platform (user enumeration). Keeping them identical prevents this.

2. Who can read the claims inside your JWT? Who can change them?

   > *Your answer:* Anyone with the token can decode and read the claims because it's just Base64URL-encoded. But no one can tamper with them without breaking the HMAC signature, since only the server has the secret key.

3. What does `jti` give us that `sub` doesn't? (Think about S5.)

   > *Your answer:* `sub` is just the user's email, so blocking it would log out the user from all devices. `jti` is a unique ID for that specific token, allowing us to blacklist just that single session on logout in S5.

### F. Decision

| File | Decision | Reason |
|---|---|---|
| `LoginRequest.java` | Accepted | Has correct @NotBlank and @Email annotations. |
| `LoginResponse.java` | Accepted | Matches required contract fields (accessToken, tokenType, expiresIn). |
| `JwtService.java` | Accepted | Uses modern jjwt 0.12 API, injects secret via @Value, generates random UUID jti, and didn't write parse logic yet. |
| `AuthService.java` | Accepted | Normalises email, throws identical 401 error message for bad email/password, uses configured expiry. |
| `AuthController.java` | Accepted | Maps POST /api/auth/login with @Valid and returns 200 OK. |

---

## Slice 3 — JWT Filter & Protected API (S3)

### A. Bucket Check

| File / Method | Tag | In Allowed List? |
|---|---|---|
| `security/JwtService.java` (`parseToken`) | Security | Yes |
| `security/JwtAuthenticationFilter.java` | Security | Yes |
| `config/SecurityConfig.java` (`filterChain`) | Config | Yes |
| `controller/UserController.java` (`getCurrentUser`) | Controller | Yes |

### B. Contract Check

| Check | Pass / Fail |
|---|---|
| `GET /api/courses` works **without** token | Pass |
| `GET /api/users/me` returns **401** with no token | Pass |
| `GET /api/users/me` returns **200** with valid token | Pass |
| 401 body is an `ApiError` JSON object | Pass |

### C. Validation & Errors

| Check | Pass / Fail |
|---|---|
| Filter does not write a response on error — continues chain | Pass |
| Filter clears SecurityContext on bad token | Pass |

### D. Security Specifics

| Check | Pass / Fail |
|---|---|
| Spring Security 6 style: lambda DSL, `requestMatchers`, `authorizeHttpRequests` | Pass |
| Rejected: `WebSecurityConfigurerAdapter`, `antMatchers`, `authorizeRequests`, `.and()` | Pass |
| `permitAll` covers only the three specific public endpoints | Pass |
| `GET /api/courses` is public but `POST /api/courses` is NOT (watch for broad matcher) | Pass |
| Signature + expiry verified with `Jwts.parser().verifyWith(key).build().parseSignedClaims(token)` | Pass |
| Authorities use `ROLE_` prefix | Pass |
| Entry point and access-denied handler wired via `exceptionHandling(...)` | Pass |
| No `UserDetailsService`, `AuthenticationManager`, sessions or CORS added | Pass |
| `GET /api/users/me` returns `UserResponse`, not the entity | Pass |

### E. Understanding Questions

1. Why is CSRF protection disabled here, and when would disabling it be wrong?

   > *Your answer:* The API is stateless and uses Bearer tokens in headers, which browsers don't send automatically. Disabling CSRF would be dangerous if we used cookie-based sessions, since browsers attach cookies automatically to cross-site requests.

2. Walk through what happens to a request with an expired token, step by step, until the client sees 401.

   > *Your answer:* In `JwtAuthenticationFilter`, `parseToken()` throws `ExpiredJwtException`. The filter catches it, clears the security context, and continues the chain. Since the request is unauthenticated, Spring Security triggers `JsonAuthenticationEntryPoint`, returning 401.

3. Why don't 401/403 errors from the filter chain reach `GlobalExceptionHandler`?

   > *Your answer:* `GlobalExceptionHandler` only catches exceptions thrown inside controllers. Security filters execute before requests reach the DispatcherServlet or controllers, so filter errors are handled by `AuthenticationEntryPoint` and `AccessDeniedHandler`.

### F. Decision

| File | Decision | Reason |
|---|---|---|
| `JwtService.java` | Accepted | Added parseToken using modern jjwt 0.12 parser with verifyWith. |
| `JwtAuthenticationFilter.java` | Accepted | Correctly checks Bearer token, sets ROLE_ prefix, clears context on error, and doesn't write direct responses. |
| `SecurityConfig.java` | Accepted | Uses Spring Security 6 lambda DSL, correctly limits permitAll to the 3 public endpoints, and wires handlers. |
| `UserController.java` | Accepted | Properly maps GET /api/users/me, reads email from authentication, and returns UserResponse DTO. |

---

## Slice 4 — Role Restriction (S4)

### A. Bucket Check

| File / Method | Tag | In Allowed List? |
|---|---|---|
| `config/SecurityConfig.java` (`filterChain`) | Config | Yes |

### B. Contract Check

| Check | Pass / Fail |
|---|---|
| `POST /api/courses` with no token → **401** | Pass |
| `POST /api/courses` with STUDENT token → **403** | Pass |
| `POST /api/courses` with ADMIN token → **201** | Pass |
| 403 body is an `ApiError` JSON object | Pass |

### D. Security Specifics

| Check | Pass / Fail |
|---|---|
| Role naming consistent: `hasRole("ADMIN")` ↔ authority `ROLE_ADMIN` | Pass |
| If `@PreAuthorize` used: `@EnableMethodSecurity` is present | Pass |
| Rule placed **before** `anyRequest().authenticated()` if in SecurityConfig | Pass |
| Diff is small — AI did not rewrite the whole config | Pass |

### E. Understanding Questions

1. A STUDENT calls `POST /api/courses`. Why does the server return 403 and not 401?

   > *Your answer:* 401 means unauthenticated (identity unknown), while 403 means forbidden (identity is known, but lacks permission). The student is logged in, but isn't an ADMIN.

2. Where does the role in the JWT come from, and can the client change it?

   > *Your answer:* It comes from the database during login. The client cannot tamper with it because changing the payload breaks the HMAC signature.

### F. Decision

| File | Decision | Reason |
|---|---|---|
| `SecurityConfig.java` | Accepted | Added hasRole("ADMIN") for POST /api/courses right before anyRequest().authenticated() with a 1-line diff. |

---

## Slice 5 — Logout (S5) *(stretch)*

### A. Bucket Check

| File / Method | Tag | In Allowed List? |
|---|---|---|
| `security/TokenDenylist.java` | Security | Yes |
| `security/JwtAuthenticationFilter.java` (`doFilterInternal`) | Security | Yes |
| `controller/AuthController.java` (`logout`) | Controller | Yes |

### B. Contract Check

| Check | Pass / Fail |
|---|---|
| `POST /api/auth/logout` returns **204** | Pass |
| Same token returns **401** after logout | Pass |
| Fresh login after logout returns a new working token | Pass |

### D. Security Specifics

| Check | Pass / Fail |
|---|---|
| Logout adds entry to server-side denylist (not just tells client to delete token) | Pass |
| Denylist keyed by **jti**, not email | Pass |
| Map is `ConcurrentHashMap` (thread-safe) | Pass |
| Logout endpoint is NOT under `permitAll` | Pass |
| Filter checks the denylist and treats denylisted jti as invalid | Pass |
| (Bonus) Expired entries are removed from the map | Pass |

### E. Understanding Questions

1. Why does logout need server-side state if JWT is "stateless"?

   > *Your answer:* JWTs remain valid until their expiration date. Without recording revoked tokens on the server, there's no way to invalidate a token before it expires naturally.

2. What happens to the denylist when the server restarts? What if there are two server instances?

   > *Your answer:* An in-memory map clears on restart, making logged-out tokens valid again. Across multiple instances, memory isn't shared, so a shared cache like Redis would be needed in production.

3. Why store the expiry time with each jti?

   > *Your answer:* To clean up expired entries from the map. Once a token expires on its own, we don't need to track it anymore, preventing the denylist from growing infinitely.

### F. Decision

| File | Decision | Reason |
|---|---|---|
| `TokenDenylist.java` | Accepted | Thread-safe ConcurrentHashMap keyed by jti, with cleanExpired() logic. |
| `JwtAuthenticationFilter.java` | Accepted | Checks tokenDenylist.isRevoked() and clears security context on match. |
| `AuthController.java` | Accepted | POST /api/auth/logout extracts jti/expiry, revokes token, and returns 204. |

---

## No-AI Change Tasks

**Files changed:**

| File | What changed and why |
|---|---|
| `application.yml` | Set `jwt.expiration-seconds` to 900 (15 min). |
| `CourseController.java` | Added `GET /{id}` calling `courseService.findById()`. |
| `UpdateUserRequest.java` | Created DTO for `PUT /me` with validated `fullName`. |
| `UserController.java` | Added `PUT /me` to update current user's `fullName`. |
| `SignupRequest.java` | Added `@Pattern` on password requiring at least one digit. |
| `SecurityConfig.java` | Removed `GET /api/courses` from `permitAll()` so it requires auth. |

**Explanation (your own words):**
> - **Token Expiry:** Set `jwt.expiration-seconds: 900` in `application.yml`; `JwtService` and `LoginResponse` pick it up automatically.
> - **GET /api/courses/{id}:** Added endpoint in `CourseController` delegating to `courseService.findById()` (returns 404 if not found).
> - **PUT /api/users/me:** Created `UpdateUserRequest` DTO and controller method to let logged-in users update only their `fullName`.
> - **Password Validation:** Added `@Pattern` in `SignupRequest` to reject passwords without at least one digit (returns 400 with field error).
> - **Protected Courses:** Removed `GET /api/courses` from `permitAll()` in `SecurityConfig`, so browsing courses now requires auth which breaks contract row P1 and S3 manual tests with a 401.
