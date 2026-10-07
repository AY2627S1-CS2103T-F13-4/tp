package seedu.address.storage;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import seedu.address.commons.exceptions.IllegalValueException;
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

/**
 * Jackson-friendly version of {@link Person}.
 */
class JsonAdaptedPerson {

    public static final String MISSING_FIELD_MESSAGE_FORMAT = "Person's %s field is missing!";

    private final String name;
    private final String phone;
    private final String email;
    private final String address;
    private final List<JsonAdaptedTag> tags = new ArrayList<>();

    @JsonDeserialize(using = StrictStringDeserializer.class)
    private final String subject;
    @JsonDeserialize(using = StrictStringDeserializer.class)
    private final String schoolingLevel;
    private final JsonAdaptedGuardianContact guardianContact;
    private final JsonAdaptedWeeklyLessonSlot weeklyLessonSlot;
    @JsonDeserialize(using = StrictStringDeserializer.class)
    private final String hourlyRate;

    /** Retains compatibility with existing adapter callers. */
    public JsonAdaptedPerson(String name, String phone, String email, String address, List<JsonAdaptedTag> tags) {
        this(name, phone, email, address, tags, null, null, null, null, null);
    }

    /**
     * Constructs a {@code JsonAdaptedPerson} with the given person details.
     */
    @JsonCreator
    public JsonAdaptedPerson(@JsonProperty("name") String name, @JsonProperty("phone") String phone,
            @JsonProperty("email") String email, @JsonProperty("address") String address,
            @JsonProperty("tags") List<JsonAdaptedTag> tags, @JsonProperty("subject") String subject,
            @JsonProperty("schoolingLevel") String schoolingLevel,
            @JsonProperty("guardianContact") JsonAdaptedGuardianContact guardianContact,
            @JsonProperty("weeklyLessonSlot") JsonAdaptedWeeklyLessonSlot weeklyLessonSlot,
            @JsonProperty("hourlyRate") String hourlyRate) {
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.subject = subject;
        this.schoolingLevel = schoolingLevel;
        this.guardianContact = guardianContact;
        this.weeklyLessonSlot = weeklyLessonSlot;
        this.hourlyRate = hourlyRate;
        if (tags != null) {
            this.tags.addAll(tags);
        }
    }

    /**
     * Converts a given {@code Person} into this class for Jackson use.
     */
    public JsonAdaptedPerson(Person source) {
        name = source.getName().fullName;
        phone = source.getPhone().value;
        email = source.getEmail().value;
        address = source.getAddress().value;
        subject = source.getSubject().map(Subject::toString).orElse(null);
        schoolingLevel = source.getSchoolingLevel().map(SchoolingLevel::toString).orElse(null);
        guardianContact = source.getGuardianContact().map(JsonAdaptedGuardianContact::new).orElse(null);
        weeklyLessonSlot = source.getWeeklyLessonSlot().map(JsonAdaptedWeeklyLessonSlot::new).orElse(null);
        hourlyRate = source.getHourlyRate().map(HourlyRate::toString).orElse(null);
        tags.addAll(source.getTags().stream()
                .map(JsonAdaptedTag::new)
                .collect(Collectors.toList()));
    }

    /**
     * Converts this Jackson-friendly adapted person object into the model's {@code Person} object.
     *
     * @throws IllegalValueException if there were any data constraints violated in the adapted person.
     */
    public Person toModelType() throws IllegalValueException {
        final List<Tag> personTags = new ArrayList<>();
        for (JsonAdaptedTag tag : tags) {
            personTags.add(tag.toModelType());
        }

        if (name == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, Name.class.getSimpleName()));
        }
        if (!Name.isValidName(name)) {
            throw new IllegalValueException(Name.MESSAGE_CONSTRAINTS);
        }
        final Name modelName = new Name(name);

        if (phone == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, Phone.class.getSimpleName()));
        }
        if (!Phone.isValidPhone(phone)) {
            throw new IllegalValueException(Phone.MESSAGE_CONSTRAINTS);
        }
        final Phone modelPhone = new Phone(phone);

        if (email == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, Email.class.getSimpleName()));
        }
        if (!Email.isValidEmail(email)) {
            throw new IllegalValueException(Email.MESSAGE_CONSTRAINTS);
        }
        final Email modelEmail = new Email(email);

        if (address == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, Address.class.getSimpleName()));
        }
        if (!Address.isValidAddress(address)) {
            throw new IllegalValueException(Address.MESSAGE_CONSTRAINTS);
        }
        final Address modelAddress = new Address(address);

        final Set<Tag> modelTags = new HashSet<>(personTags);
        try {
            Optional<Subject> modelSubject = Optional.ofNullable(subject).map(Subject::new);
            Optional<SchoolingLevel> modelLevel = Optional.ofNullable(schoolingLevel).map(SchoolingLevel::new);
            Optional<GuardianContact> modelGuardian = guardianContact == null ? Optional.empty()
                    : Optional.of(guardianContact.toModelType());
            Optional<WeeklyLessonSlot> modelSlot = weeklyLessonSlot == null ? Optional.empty()
                    : Optional.of(weeklyLessonSlot.toModelType());
            if (hourlyRate != null && !hourlyRate.matches("[0-9]+(\\.[0-9]{1,2})?")) {
                throw new IllegalValueException(HourlyRate.MESSAGE_CONSTRAINTS);
            }
            Optional<HourlyRate> modelRate = Optional.ofNullable(hourlyRate)
                    .map(value -> new HourlyRate(new BigDecimal(value)));
            return new Person(modelName, modelPhone, modelEmail, modelAddress, modelTags,
                    modelSubject, modelLevel, modelGuardian, modelSlot, modelRate);
        } catch (IllegalArgumentException e) {
            throw new IllegalValueException(e.getMessage());
        }
    }

}
