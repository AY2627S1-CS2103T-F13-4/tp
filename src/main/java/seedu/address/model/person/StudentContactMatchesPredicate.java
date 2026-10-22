package seedu.address.model.person;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

/** Matches optional name and guardian substrings together with existing student search criteria. */
public final class StudentContactMatchesPredicate implements Predicate<Student> {
    private final Optional<String> name;
    private final Optional<String> guardian;
    private final Predicate<Student> additionalCriteria;

    /** Creates a search requiring every supplied criterion, ignoring letter case in contact text. */
    public StudentContactMatchesPredicate(Optional<String> name, Optional<String> guardian,
            Predicate<Student> additionalCriteria) {
        this.name = Objects.requireNonNull(name).map(value -> value.toLowerCase(Locale.ROOT));
        this.guardian = Objects.requireNonNull(guardian).map(value -> value.toLowerCase(Locale.ROOT));
        this.additionalCriteria = Objects.requireNonNull(additionalCriteria);
    }

    @Override
    public boolean test(Student student) {
        return name.map(text -> contains(student.getName().fullName, text)).orElse(true)
                && guardian.map(text -> student.getGuardian()
                        .map(contact -> contains(contact.getName().fullName, text)
                                || contains(contact.getPhone().value, text))
                        .orElse(false)).orElse(true)
                && additionalCriteria.test(student);
    }

    private boolean contains(String value, String text) {
        return value.toLowerCase(Locale.ROOT).contains(text);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof StudentContactMatchesPredicate predicate
                && name.equals(predicate.name)
                && guardian.equals(predicate.guardian)
                && additionalCriteria.equals(predicate.additionalCriteria);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, guardian, additionalCriteria);
    }
}
