/**
 * Layer 4 — Frameworks &amp; Drivers.
 *
 * <p>The outermost ring: Spring MVC, JPA/Hibernate, Liquibase, third-party clients.
 * Details live here so they can be swapped without touching business rules.
 * <ul>
 *   <li>{@code web}      — Spring REST controllers that delegate to {@code adapters.controllers}</li>
 *   <li>{@code db}       — JPA entities and Spring Data repositories behind {@code adapters.gateways.MovieDataSource}</li>
 *   <li>{@code external} — clients for third-party APIs (payments, email, ...)</li>
 * </ul>
 */
package reservation.movie.frameworks;
