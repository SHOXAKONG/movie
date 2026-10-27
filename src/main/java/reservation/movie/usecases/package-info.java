/**
 * Layer 2 — Application Business Rules.
 *
 * <p>Use cases orchestrate entities to fulfil application-specific goals. They talk to the
 * outside world only through the port interfaces declared in {@code ports.input} and
 * {@code ports.output}. Classes here may depend on {@code entities} only — never on
 * {@code adapters}, {@code frameworks}, {@code main}, Spring or JPA.
 */
package reservation.movie.usecases;
