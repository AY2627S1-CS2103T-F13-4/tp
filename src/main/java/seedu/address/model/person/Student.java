package seedu.address.model.person;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.tag.Tag;

/**
 * Represents a Student in the address book.
 * Guarantees: details are present and not null, field values are validated, immutable.
 */
public class Student extends Person {

    public static final String MESSAGE_DUPLICATE_LESSONS = "A student cannot have duplicate lessons.";

    private final Email email;
    private final Address address;
    private final Set<Tag> tags = new HashSet<>();

    private final Optional<School> school;
    private final Optional<SchoolingLevel> schoolingLevel;
    private final Optional<Guardian> guardian;
    private final List<Lesson> lessons;

    /**
     * Creates a student with the given contact fields, no tuition details and no lessons.
     * Every field must be present and not null.
     */
    public Student(Name name, Phone phone, Email email, Address address, Set<Tag> tags) {
        this(name, phone, email, address, tags, Optional.empty(), Optional.empty(), Optional.empty(), List.of());
    }

    /**
     * Creates a student with all contact fields, an optional school, optional tuition details and lessons.
     *
     * @throws IllegalArgumentException if {@code lessons} contains duplicates.
     */
    public Student(Name name, Phone phone, Email email, Address address, Set<Tag> tags,
            Optional<School> school, Optional<SchoolingLevel> schoolingLevel, Optional<Guardian> guardian,
            List<Lesson> lessons) {
        super(name, phone);
        requireAllNonNull(email, address, tags, school, schoolingLevel, guardian, lessons);
        lessons.forEach(Objects::requireNonNull);
        if (new HashSet<>(lessons).size() != lessons.size()) {
            throw new IllegalArgumentException(MESSAGE_DUPLICATE_LESSONS);
        }
        this.email = email;
        this.address = address;
        this.tags.addAll(tags);
        this.school = school;
        this.schoolingLevel = schoolingLevel;
        this.guardian = guardian;
        this.lessons = Collections.unmodifiableList(new ArrayList<>(lessons));
    }

    /** Returns an unmodifiable list of this student's lessons, which may be empty. */
    public List<Lesson> getLessons() {
        return lessons;
    }

    public Optional<School> getSchool() {
        return school;
    }

    public Optional<SchoolingLevel> getSchoolingLevel() {
        return schoolingLevel;
    }

    /** Returns the school stage this student is listed under; a student with no level is {@code OTHER}. */
    public SchoolingLevel.Stage getStage() {
        return schoolingLevel.map(SchoolingLevel::stage).orElse(SchoolingLevel.Stage.OTHER);
    }

    public Optional<Guardian> getGuardian() {
        return guardian;
    }

    public Email getEmail() {
        return email;
    }

    public Address getAddress() {
        return address;
    }

    /**
     * Returns an immutable tag set, which throws {@code UnsupportedOperationException}
     * if modification is attempted.
     */
    public Set<Tag> getTags() {
        return Collections.unmodifiableSet(tags);
    }

    /**
     * Returns true if both students have the same name, phone and data fields.
     * This defines a stronger notion of equality between two students.
     */
    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Student otherStudent)) {
            return false;
        }

        return super.equals(otherStudent)
                && email.equals(otherStudent.email)
                && address.equals(otherStudent.address)
                && tags.equals(otherStudent.tags)
                && school.equals(otherStudent.school)
                && schoolingLevel.equals(otherStudent.schoolingLevel)
                && guardian.equals(otherStudent.guardian)
                && lessons.equals(otherStudent.lessons);
    }

    @Override
    public int hashCode() {
        // use this method for custom fields hashing instead of implementing your own
        return Objects.hash(super.hashCode(), email, address, tags, school, schoolingLevel, guardian,
                lessons);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("name", getName())
                .add("phone", getPhone())
                .add("email", email)
                .add("address", address)
                .add("tags", tags)
                .add("school", school)
                .add("schoolingLevel", schoolingLevel)
                .add("guardian", guardian)
                .add("lessons", lessons)
                .toString();
    }

}
