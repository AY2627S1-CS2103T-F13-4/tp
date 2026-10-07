package seedu.address.model.person;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.tag.Tag;

/**
 * Represents a Person in the address book.
 * Guarantees: details are present and not null, field values are validated, immutable.
 */
public class Person {

    // Identity fields
    private final Name name;
    private final Phone phone;
    private final Email email;

    // Data fields
    private final Address address;
    private final Set<Tag> tags = new HashSet<>();

    private final Optional<Subject> subject;
    private final Optional<SchoolingLevel> schoolingLevel;
    private final Optional<GuardianContact> guardianContact;
    private final Optional<WeeklyLessonSlot> weeklyLessonSlot;
    private final Optional<HourlyRate> hourlyRate;

    /**
     * Every field must be present and not null.
     */
    public Person(Name name, Phone phone, Email email, Address address, Set<Tag> tags) {
        this(name, phone, email, address, tags, Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty());
    }

    /** Creates a student with all contact fields and optional tuition details. */
    public Person(Name name, Phone phone, Email email, Address address, Set<Tag> tags,
            Optional<Subject> subject, Optional<SchoolingLevel> schoolingLevel,
            Optional<GuardianContact> guardianContact, Optional<WeeklyLessonSlot> weeklyLessonSlot,
            Optional<HourlyRate> hourlyRate) {
        requireAllNonNull(name, phone, email, address, tags, subject, schoolingLevel,
                guardianContact, weeklyLessonSlot, hourlyRate);
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.tags.addAll(tags);
        this.subject = subject;
        this.schoolingLevel = schoolingLevel;
        this.guardianContact = guardianContact;
        this.weeklyLessonSlot = weeklyLessonSlot;
        this.hourlyRate = hourlyRate;
    }

    public Optional<Subject> getSubject() {
        return subject;
    }

    public Optional<SchoolingLevel> getSchoolingLevel() {
        return schoolingLevel;
    }

    public Optional<GuardianContact> getGuardianContact() {
        return guardianContact;
    }

    public Optional<WeeklyLessonSlot> getWeeklyLessonSlot() {
        return weeklyLessonSlot;
    }

    public Optional<HourlyRate> getHourlyRate() {
        return hourlyRate;
    }

    public Name getName() {
        return name;
    }

    public Phone getPhone() {
        return phone;
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
     * Returns true if both persons have the same name.
     * This defines a weaker notion of equality between two persons.
     */
    public boolean isSamePerson(Person otherPerson) {
        if (otherPerson == this) {
            return true;
        }

        return otherPerson != null
                && otherPerson.getName().equals(getName());
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
        if (!(other instanceof Person otherPerson)) {
            return false;
        }

        return name.equals(otherPerson.name)
                && phone.equals(otherPerson.phone)
                && email.equals(otherPerson.email)
                && address.equals(otherPerson.address)
                && tags.equals(otherPerson.tags)
                && subject.equals(otherPerson.subject)
                && schoolingLevel.equals(otherPerson.schoolingLevel)
                && guardianContact.equals(otherPerson.guardianContact)
                && weeklyLessonSlot.equals(otherPerson.weeklyLessonSlot)
                && hourlyRate.equals(otherPerson.hourlyRate);
    }

    @Override
    public int hashCode() {
        // use this method for custom fields hashing instead of implementing your own
        return Objects.hash(name, phone, email, address, tags, subject, schoolingLevel, guardianContact,
                weeklyLessonSlot, hourlyRate);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("name", name)
                .add("phone", phone)
                .add("email", email)
                .add("address", address)
                .add("tags", tags)
                .add("subject", subject)
                .add("schoolingLevel", schoolingLevel)
                .add("guardianContact", guardianContact)
                .add("weeklyLessonSlot", weeklyLessonSlot)
                .add("hourlyRate", hourlyRate)
                .toString();
    }

}
