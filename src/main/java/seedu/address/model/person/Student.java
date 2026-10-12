package seedu.address.model.person;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
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

    // Identity fields
    private final Email email;

    // Data fields
    private final Address address;
    private final Set<Tag> tags = new HashSet<>();

    private final Set<Subject> subjects;
    private final Optional<SchoolingLevel> schoolingLevel;
    private final Optional<Guardian> guardianContact;
    private final Optional<WeeklyLessonSlot> weeklyLessonSlot;
    private final Optional<HourlyRate> hourlyRate;

    /**
     * Every field must be present and not null.
     */
    public Student(Name name, Phone phone, Email email, Address address, Set<Tag> tags) {
        this(name, phone, email, address, tags, Set.of(), Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty());
    }

    /** Creates a student with all contact fields and optional tuition details. */
    public Student(Name name, Phone phone, Email email, Address address, Set<Tag> tags,
            Set<Subject> subjects, Optional<SchoolingLevel> schoolingLevel,
            Optional<Guardian> guardianContact, Optional<WeeklyLessonSlot> weeklyLessonSlot,
            Optional<HourlyRate> hourlyRate) {
        super(name, phone);
        requireAllNonNull(email, address, tags, subjects, schoolingLevel,
                guardianContact, weeklyLessonSlot, hourlyRate);
        this.email = email;
        this.address = address;
        this.tags.addAll(tags);
        subjects.forEach(Objects::requireNonNull);
        this.subjects = Collections.unmodifiableSet(new LinkedHashSet<>(subjects));
        this.schoolingLevel = schoolingLevel;
        this.guardianContact = guardianContact;
        this.weeklyLessonSlot = weeklyLessonSlot;
        this.hourlyRate = hourlyRate;
    }

    public Set<Subject> getSubjects() {
        return subjects;
    }

    public Optional<SchoolingLevel> getSchoolingLevel() {
        return schoolingLevel;
    }

    public Optional<Guardian> getGuardian() {
        return guardianContact;
    }

    public Optional<WeeklyLessonSlot> getWeeklyLessonSlot() {
        return weeklyLessonSlot;
    }

    public Optional<HourlyRate> getHourlyRate() {
        return hourlyRate;
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
     * Returns true if both persons have the same identity and data fields.
     * This defines a stronger notion of equality between two persons.
     */
    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Student otherPerson)) {
            return false;
        }

        return super.equals(otherPerson)
                && email.equals(otherPerson.email)
                && address.equals(otherPerson.address)
                && tags.equals(otherPerson.tags)
                && subjects.equals(otherPerson.subjects)
                && schoolingLevel.equals(otherPerson.schoolingLevel)
                && guardianContact.equals(otherPerson.guardianContact)
                && weeklyLessonSlot.equals(otherPerson.weeklyLessonSlot)
                && hourlyRate.equals(otherPerson.hourlyRate);
    }

    @Override
    public int hashCode() {
        // use this method for custom fields hashing instead of implementing your own
        return Objects.hash(super.hashCode(), email, address, tags, subjects, schoolingLevel, guardianContact,
                weeklyLessonSlot, hourlyRate);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("name", getName())
                .add("phone", getPhone())
                .add("email", email)
                .add("address", address)
                .add("tags", tags)
                .add("subjects", subjects)
                .add("schoolingLevel", schoolingLevel)
                .add("guardianContact", guardianContact)
                .add("weeklyLessonSlot", weeklyLessonSlot)
                .add("hourlyRate", hourlyRate)
                .toString();
    }

}
