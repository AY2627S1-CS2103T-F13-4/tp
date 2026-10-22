package seedu.address.storage;

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
import seedu.address.model.person.Guardian;
import seedu.address.model.person.Lesson;
import seedu.address.model.person.Name;
import seedu.address.model.person.Phone;
import seedu.address.model.person.ProgressNote;
import seedu.address.model.person.School;
import seedu.address.model.person.SchoolingLevel;
import seedu.address.model.person.Student;
import seedu.address.model.tag.Tag;

/**
 * Jackson-friendly version of {@link Student}.
 */
class JsonAdaptedPerson {

    public static final String MISSING_FIELD_MESSAGE_FORMAT = "Student's %s field is missing!";

    private final String name;
    private final String phone;
    private final String email;
    private final String address;
    private final List<JsonAdaptedTag> tags = new ArrayList<>();

    @JsonDeserialize(using = StrictStringDeserializer.class)
    private final String school;
    @JsonDeserialize(using = StrictStringDeserializer.class)
    private final String schoolingLevel;
    private final JsonAdaptedGuardian guardianContact;
    private final List<JsonAdaptedLesson> lessons = new ArrayList<>();
    private final List<JsonAdaptedProgressNote> notes = new ArrayList<>();

    /** Retains compatibility with existing adapter callers. */
    public JsonAdaptedPerson(String name, String phone, String email, String address, List<JsonAdaptedTag> tags) {
        this(name, phone, email, address, tags, null, null, null, null);
    }

    /** Constructs a {@code JsonAdaptedPerson} with the given person details and no notes. */
    public JsonAdaptedPerson(String name, String phone, String email, String address, List<JsonAdaptedTag> tags,
            String school, String schoolingLevel, JsonAdaptedGuardian guardianContact,
            List<JsonAdaptedLesson> lessons) {
        this(name, phone, email, address, tags, school, schoolingLevel, guardianContact, lessons, null);
    }

    /**
     * Constructs a {@code JsonAdaptedPerson} with the given person details.
     * Files saved before notes existed have no {@code notes}, which loads as no notes.
     */
    @JsonCreator
    public JsonAdaptedPerson(@JsonProperty("name") String name, @JsonProperty("phone") String phone,
            @JsonProperty("email") String email, @JsonProperty("address") String address,
            @JsonProperty("tags") List<JsonAdaptedTag> tags,
            @JsonProperty("school") String school,
            @JsonProperty("schoolingLevel") String schoolingLevel,
            @JsonProperty("guardianContact") JsonAdaptedGuardian guardianContact,
            @JsonProperty("lessons") List<JsonAdaptedLesson> lessons,
            @JsonProperty("notes") List<JsonAdaptedProgressNote> notes) {
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.school = school;
        this.schoolingLevel = schoolingLevel;
        this.guardianContact = guardianContact;
        if (tags != null) {
            this.tags.addAll(tags);
        }
        if (lessons != null) {
            this.lessons.addAll(lessons);
        }
        if (notes != null) {
            this.notes.addAll(notes);
        }
    }

    /**
     * Converts a given {@code Student} into this class for Jackson use.
     */
    public JsonAdaptedPerson(Student source) {
        name = source.getName().fullName;
        phone = source.getPhone().value;
        email = source.getEmail().value;
        address = source.getAddress().value;
        school = source.getSchool().map(School::toString).orElse(null);
        schoolingLevel = source.getSchoolingLevel().map(SchoolingLevel::toString).orElse(null);
        guardianContact = source.getGuardian().map(JsonAdaptedGuardian::new).orElse(null);
        lessons.addAll(source.getLessons().stream()
                .map(JsonAdaptedLesson::new)
                .collect(Collectors.toList()));
        notes.addAll(source.getNotes().stream()
                .map(JsonAdaptedProgressNote::new)
                .collect(Collectors.toList()));
        tags.addAll(source.getTags().stream()
                .map(JsonAdaptedTag::new)
                .collect(Collectors.toList()));
    }

    /**
     * Converts this Jackson-friendly adapted person object into the model's {@code Student} object.
     *
     * @throws IllegalValueException if there were any data constraints violated in the adapted person.
     */
    public Student toModelType() throws IllegalValueException {
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
            Optional<School> modelSchool = Optional.ofNullable(school).map(School::new);
            Optional<SchoolingLevel> modelLevel = Optional.ofNullable(schoolingLevel).map(SchoolingLevel::new);
            Optional<Guardian> modelGuardian = guardianContact == null ? Optional.empty()
                    : Optional.of(guardianContact.toModelType());
            final List<Lesson> modelLessons = new ArrayList<>();
            for (JsonAdaptedLesson lesson : lessons) {
                modelLessons.add(lesson.toModelType());
            }
            final List<ProgressNote> modelNotes = new ArrayList<>();
            for (JsonAdaptedProgressNote note : notes) {
                if (note == null) {
                    throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, "note"));
                }
                modelNotes.add(note.toModelType());
            }
            return new Student(modelName, modelPhone, modelEmail, modelAddress, modelTags,
                    modelSchool, modelLevel, modelGuardian, modelLessons, modelNotes);
        } catch (IllegalArgumentException e) {
            throw new IllegalValueException(e.getMessage());
        }
    }

}
