/**
 * Layer 1 — Enterprise Business Rules.
 *
 * <p>Entities hold the most general, highest-level rules of the movie reservation domain.
 * They are plain Java: no Spring, no JPA, no Jackson. Classes in this package must never
 * import from {@code usecases}, {@code adapters}, {@code frameworks} or {@code main}.
 */
package reservation.movie.entities;
