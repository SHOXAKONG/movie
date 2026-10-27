/**
 * Layer 3 — Interface Adapters.
 *
 * <p>Converts data between the shape the use cases want and the shape the frameworks want.
 * <ul>
 *   <li>{@code controllers} — turn framework input into use case request models</li>
 *   <li>{@code presenters}  — implement Output Ports and build view models</li>
 *   <li>{@code gateways}    — implement repository interfaces from {@code usecases.ports.output}</li>
 * </ul>
 * May depend on {@code usecases} and {@code entities}, never on {@code frameworks} or {@code main}.
 */
package reservation.movie.adapters;
