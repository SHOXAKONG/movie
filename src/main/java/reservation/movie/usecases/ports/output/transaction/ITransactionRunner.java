package reservation.movie.usecases.ports.output.transaction;

/**
 * Runs several repository calls as one atomic unit. If the work throws, everything it did is rolled back.
 */
public interface ITransactionRunner {

    void run(Runnable work);
}
