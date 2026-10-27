package reservation.movie.frameworks.db.transaction;

import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import reservation.movie.usecases.ports.output.transaction.ITransactionRunner;

@Component
public class SpringTransactionRunner implements ITransactionRunner {

    private final TransactionTemplate template;

    public SpringTransactionRunner(PlatformTransactionManager transactionManager) {
        this.template = new TransactionTemplate(transactionManager);
    }

    @Override
    public void run(Runnable work) {
        template.executeWithoutResult(status -> work.run());
    }
}
