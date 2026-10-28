package seedu.address.model.person;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
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
    private final List<ProgressNote> notes;
    private final List<FollowUp> followUps;

    private final boolean isArchived;

    /**
     * Creates a student with the given contact fields, no tuition details and no lessons.
     * Every field must be present and not null.
     */
    public Student(Name name, Phone phone, Email email, Address address, Set<Tag> tags) {
        this(name, phone, email, address, tags, Optional.empty(), Optional.empty(),
                Optional.empty(), List.of(), false, List.of());
    }

    /**
     * Creates a student with all contact fields, an optional school, optional tuition details, lessons, archive status
     * and progress notes, and no follow-ups. The notes are kept oldest first.
     *
     * @throws IllegalArgumentException if {@code lessons} contains duplicates.
     */
    public Student(Name name, Phone phone, Email email, Address address, Set<Tag> tags,
            Optional<School> school, Optional<SchoolingLevel> schoolingLevel, Optional<Guardian> guardian,
            List<Lesson> lessons, boolean isArchived, List<ProgressNote> notes) {
        this(name, phone, email, address, tags, school, schoolingLevel, guardian, lessons, isArchived, notes,
                List.of());
    }

    /**
     * Creates a student with all contact fields, an optional school, optional tuition details, lessons, archive status,
     * progress notes and outstanding guardian follow-ups. The notes are kept oldest first; follow-ups keep the given
     * order.
     *
     * @throws IllegalArgumentException if {@code lessons} contains duplicates.
     */
    public Student(Name name, Phone phone, Email email, Address address, Set<Tag> tags,
            Optional<School> school, Optional<SchoolingLevel> schoolingLevel, Optional<Guardian> guardian,
            List<Lesson> lessons, boolean isArchived, List<ProgressNote> notes, List<FollowUp> followUps) {
        super(name, phone);
        requireAllNonNull(email, address, tags, school, schoolingLevel, guardian, lessons, notes, followUps);
        lessons.forEach(Objects::requireNonNull);
        notes.forEach(Objects::requireNonNull);
        followUps.forEach(Objects::requireNonNull);
        if (new HashSet<>(lessons).size() != lessons.size()) {
            throw new IllegalArgumentException(MESSAGE_DUPLICATE_LESSONS);
        }
        this.email = email;
        this.address = address;
        this.tags.addAll(tags);
        this.school = school;
        this.schoolingLevel = schoolingLevel;
        this.guardian = guardian;
        this.lessons = List.copyOf(lessons);
        this.isArchived = isArchived;
        this.notes = notes.stream().sorted(ProgressNote.OLDEST_FIRST).toList();
        this.followUps = List.copyOf(followUps);
    }

    /** Returns an unmodifiable list of this student's lessons, which may be empty. */
    public List<Lesson> getLessons() {
        return lessons;
    }

    /** Returns an unmodifiable list of this student's progress notes, oldest first, which may be empty. */
    public List<ProgressNote> getNotes() {
        return notes;
    }

    /** Returns an unmodifiable list of this student's outstanding guardian follow-ups, which may be empty. */
    public List<FollowUp> getFollowUps() {
        return followUps;
    }

    /** Returns true if this student has at least one outstanding guardian follow-up. */
    public boolean hasFollowUps() {
        return !followUps.isEmpty();
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

    /**
     * Returns the order students are listed in: by stage, then by level (P1 before P2), then alphabetically by name.
     * "P5" and "Primary 5" are the same level.
     * Unrecognised levels sort alphabetically after the numbered ones; students with no level
     * come last.
     */
    public static Comparator<Student> byLevelThenName() {
        return Comparator.comparing(Student::getStage)
                .thenComparingInt(student ->
                        student.schoolingLevel.map(SchoolingLevel::number).orElse(Integer.MAX_VALUE))
                .thenComparing(student -> student.schoolingLevel.isEmpty())
                .thenComparing(student -> student.schoolingLevel
                        .filter(level -> level.number() == Integer.MAX_VALUE)
                        .map(level -> level.value.toLowerCase(Locale.ROOT)).orElse(""))
                .thenComparing(student -> student.getName().fullName.toLowerCase(Locale.ROOT));
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

    public boolean isArchived() {
        return this.isArchived;
    }

    /** Returns a copy of this student with the given archive status and all other details unchanged. */
    public Student withArchived(boolean isArchived) {
        return new Student(getName(), getPhone(), email, address, tags, school, schoolingLevel, guardian, lessons,
                isArchived, notes, followUps);
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
                && lessons.equals(otherStudent.lessons)
                && isArchived == otherStudent.isArchived
                && notes.equals(otherStudent.notes)
                && followUps.equals(otherStudent.followUps);
    }

    @Override
    public int hashCode() {
        // use this method for custom fields hashing instead of implementing your own
        return Objects.hash(super.hashCode(), email, address, tags, school, schoolingLevel, guardian,
                lessons, isArchived, notes, followUps);
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
                .add("isArchived", isArchived)
                .add("notes", notes)
                .add("followUps", followUps)
                .toString();
    }

}
