package seedu.address.testutil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import seedu.address.model.person.Address;
import seedu.address.model.person.Email;
import seedu.address.model.person.Guardian;
import seedu.address.model.person.Lesson;
import seedu.address.model.person.Name;
import seedu.address.model.person.Phone;
import seedu.address.model.person.ProgressNote;
import seedu.address.model.person.SchoolingLevel;
import seedu.address.model.person.Student;
import seedu.address.model.tag.Tag;
import seedu.address.model.util.SampleDataUtil;

/**
 * A utility class to help with building Student objects.
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
    private Optional<SchoolingLevel> schoolingLevel = Optional.empty();
    private Optional<Guardian> guardianContact = Optional.empty();
    private List<Lesson> lessons = new ArrayList<>();
    private List<ProgressNote> notes = new ArrayList<>();

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
    public PersonBuilder(Student personToCopy) {
        name = personToCopy.getName();
        phone = personToCopy.getPhone();
        email = personToCopy.getEmail();
        address = personToCopy.getAddress();
        tags = new HashSet<>(personToCopy.getTags());
        schoolingLevel = personToCopy.getSchoolingLevel();
        guardianContact = personToCopy.getGuardian();
        lessons = new ArrayList<>(personToCopy.getLessons());
        notes = new ArrayList<>(personToCopy.getNotes());
    }

    /**
     * Sets the {@code Name} of the {@code Student} that we are building.
     */
    public PersonBuilder withName(String name) {
        this.name = new Name(name);
        return this;
    }

    /**
     * Parses the {@code tags} into a {@code Set<Tag>} and sets it to the {@code Student} that we are building.
     */
    public PersonBuilder withTags(String ... tags) {
        this.tags = SampleDataUtil.getTagSet(tags);
        return this;
    }

    /**
     * Sets the {@code Address} of the {@code Student} that we are building.
     */
    public PersonBuilder withAddress(String address) {
        this.address = new Address(address);
        return this;
    }

    /**
     * Sets the {@code Phone} of the {@code Student} that we are building.
     */
    public PersonBuilder withPhone(String phone) {
        this.phone = new Phone(phone);
        return this;
    }

    /**
     * Sets the {@code Email} of the {@code Student} that we are building.
     */
    public PersonBuilder withEmail(String email) {
        this.email = new Email(email);
        return this;
    }

    /** Sets the progress notes of the {@code Student} that we are building. */
    public PersonBuilder withNotes(ProgressNote... values) {
        notes = new ArrayList<>(Arrays.asList(values));
        return this;
    }

    /** Sets the lessons of the {@code Student} that we are building, in the given order. */
    public PersonBuilder withLessons(Lesson... values) {
        lessons = new ArrayList<>(Arrays.asList(values));
        return this;
    }

    /** Sets the optional schoolingLevel; null clears it. */
    public PersonBuilder withSchoolingLevel(SchoolingLevel value) {
        schoolingLevel = Optional.ofNullable(value);
        return this;
    }

    /** Sets the optional guardianContact; null clears it. */
    public PersonBuilder withGuardian(Guardian value) {
        guardianContact = Optional.ofNullable(value);
        return this;
    }

    /** Builds an immutable student record. */
    public Student build() {
        return new Student(name, phone, email, address, tags, schoolingLevel, guardianContact, lessons, notes);
    }

}
