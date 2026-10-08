package seedu.address.testutil;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import seedu.address.model.person.Address;
import seedu.address.model.person.Email;
import seedu.address.model.person.GuardianContact;
import seedu.address.model.person.HourlyRate;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.SchoolingLevel;
import seedu.address.model.person.Subject;
import seedu.address.model.person.WeeklyLessonSlot;
import seedu.address.model.tag.Tag;
import seedu.address.model.util.SampleDataUtil;

/**
 * A utility class to help with building Person objects.
 */
public class PersonBuilder {

    public static final String DEFAULT_NAME = "Amy Bee";
    public static final String DEFAULT_PHONE = "85355255";
    public static final String DEFAULT_EMAIL = "amy@gmail.com";
    public static final String DEFAULT_ADDRESS = "123, Jurong West Ave 6, #08-111";

    private Name name;
    private Phone phone;
    private Email email;
    private Address address;
    private Set<Tag> tags;
    private Set<Subject> subjects = Set.of();
    private Optional<SchoolingLevel> schoolingLevel = Optional.empty();
    private Optional<GuardianContact> guardianContact = Optional.empty();
    private Optional<WeeklyLessonSlot> weeklyLessonSlot = Optional.empty();
    private Optional<HourlyRate> hourlyRate = Optional.empty();

    /**
     * Creates a {@code PersonBuilder} with the default details.
     */
    public PersonBuilder() {
        name = new Name(DEFAULT_NAME);
        phone = new Phone(DEFAULT_PHONE);
        email = new Email(DEFAULT_EMAIL);
        address = new Address(DEFAULT_ADDRESS);
        tags = new HashSet<>();
    }

    /**
     * Initializes the PersonBuilder with the data of {@code personToCopy}.
     */
    public PersonBuilder(Person personToCopy) {
        name = personToCopy.getName();
        phone = personToCopy.getPhone();
        email = personToCopy.getEmail();
        address = personToCopy.getAddress();
        tags = new HashSet<>(personToCopy.getTags());
        subjects = personToCopy.getSubjects();
        schoolingLevel = personToCopy.getSchoolingLevel();
        guardianContact = personToCopy.getGuardianContact();
        weeklyLessonSlot = personToCopy.getWeeklyLessonSlot();
        hourlyRate = personToCopy.getHourlyRate();
    }

    /**
     * Sets the {@code Name} of the {@code Person} that we are building.
     */
    public PersonBuilder withName(String name) {
        this.name = new Name(name);
        return this;
    }

    /**
     * Parses the {@code tags} into a {@code Set<Tag>} and sets it to the {@code Person} that we are building.
     */
    public PersonBuilder withTags(String ... tags) {
        this.tags = SampleDataUtil.getTagSet(tags);
        return this;
    }

    /**
     * Sets the {@code Address} of the {@code Person} that we are building.
     */
    public PersonBuilder withAddress(String address) {
        this.address = new Address(address);
        return this;
    }

    /**
     * Sets the {@code Phone} of the {@code Person} that we are building.
     */
    public PersonBuilder withPhone(String phone) {
        this.phone = new Phone(phone);
        return this;
    }

    /**
     * Sets the {@code Email} of the {@code Person} that we are building.
     */
    public PersonBuilder withEmail(String email) {
        this.email = new Email(email);
        return this;
    }

    /** Sets the optional subject; null clears it. */
    public PersonBuilder withSubject(Subject value) {
        subjects = value == null ? Set.of() : Set.of(value);
        return this;
    }

    /** Sets all subjects in input order. */
    public PersonBuilder withSubjects(Subject... values) {
        subjects = new LinkedHashSet<>(Arrays.asList(values));
        return this;
    }

    /** Sets the optional schoolingLevel; null clears it. */
    public PersonBuilder withSchoolingLevel(SchoolingLevel value) {
        schoolingLevel = Optional.ofNullable(value);
        return this;
    }

    /** Sets the optional guardianContact; null clears it. */
    public PersonBuilder withGuardianContact(GuardianContact value) {
        guardianContact = Optional.ofNullable(value);
        return this;
    }

    /** Sets the optional weeklyLessonSlot; null clears it. */
    public PersonBuilder withWeeklyLessonSlot(WeeklyLessonSlot value) {
        weeklyLessonSlot = Optional.ofNullable(value);
        return this;
    }

    /** Sets the optional hourlyRate; null clears it. */
    public PersonBuilder withHourlyRate(HourlyRate value) {
        hourlyRate = Optional.ofNullable(value);
        return this;
    }

    /** Builds an immutable student record. */
    public Person build() {
        return new Person(name, phone, email, address, tags, subjects, schoolingLevel, guardianContact,
                weeklyLessonSlot, hourlyRate);
    }

}
