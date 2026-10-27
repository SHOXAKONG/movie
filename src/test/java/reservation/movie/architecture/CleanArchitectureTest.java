package reservation.movie.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.simpleNameEndingWith;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Enforces the Dependency Rule: source code dependencies point only inwards.
 * frameworks/main -> adapters -> usecases -> entities
 */
@AnalyzeClasses(packages = "reservation.movie", importOptions = ImportOption.DoNotIncludeTests.class)
@SuppressWarnings("unused") // the @ArchTest fields are discovered and run reflectively by ArchUnit
class CleanArchitectureTest {

    @ArchTest
    static final ArchRule entitiesDependOnNothing = noClasses()
            .that().resideInAPackage("reservation.movie.entities..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "reservation.movie.usecases..", "reservation.movie.adapters..",
                    "reservation.movie.frameworks..", "reservation.movie.main..");

    @ArchTest
    static final ArchRule usecasesDependOnlyOnEntities = noClasses()
            .that().resideInAPackage("reservation.movie.usecases..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "reservation.movie.adapters..", "reservation.movie.frameworks..", "reservation.movie.main..");

    @ArchTest
    static final ArchRule adaptersDoNotKnowFrameworks = noClasses()
            .that().resideInAPackage("reservation.movie.adapters..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "reservation.movie.frameworks..", "reservation.movie.main..");

    @ArchTest
    static final ArchRule frameworksDoNotKnowMain = noClasses()
            .that().resideInAPackage("reservation.movie.frameworks..")
            .should().dependOnClassesThat().resideInAPackage("reservation.movie.main..");

    @ArchTest
    static final ArchRule innerLayersAreFrameworkFree = noClasses()
            .that().resideInAnyPackage("reservation.movie.entities..", "reservation.movie.usecases..",
                    "reservation.movie.adapters..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "org.springframework..", "jakarta.persistence..", "lombok..");

    /** The HTTP contract lives in adapters.controllers.request; use case input models stay internal. */
    @ArchTest
    static final ArchRule webDoesNotBindUseCaseRequests = noClasses()
            .that().resideInAPackage("reservation.movie.frameworks.web..")
            .should().dependOnClassesThat(resideInAPackage("reservation.movie.usecases..")
                    .and(simpleNameEndingWith("Request")));
}
