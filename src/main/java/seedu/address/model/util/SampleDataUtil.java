package seedu.address.model.util;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import seedu.address.model.AddressBook;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.person.Address;
import seedu.address.model.person.Email;
import seedu.address.model.person.Guardian;
import seedu.address.model.person.HourlyRate;
import seedu.address.model.person.Lesson;
import seedu.address.model.person.LessonLocation;
import seedu.address.model.person.LessonMode;
import seedu.address.model.person.Name;
import seedu.address.model.person.Phone;
import seedu.address.model.person.School;
import seedu.address.model.person.SchoolingLevel;
import seedu.address.model.person.Student;
import seedu.address.model.person.Subject;
import seedu.address.model.person.WeeklyLessonSlot;
import seedu.address.model.tag.Tag;

/**
 * Contains utility methods for populating {@code AddressBook} with sample data.
 * The samples cover every level group, give every school student a school, mix physical and virtual lessons,
 * include two siblings who share a guardian, and contain exactly one pair of overlapping lessons (Hafiz and Priya
 * on Wednesday).
 */
public class SampleDataUtil {
    private static final String HOME = "Student's home";
    private static final String ZOOM = "Zoom";

    public static Student[] getSamplePersons() {
        Guardian limMeiLing = new Guardian(new Name("Lim Mei Ling"), new Phone("91827364"));
        String limAddress = "Blk 512 Tampines Street 51, #08-112";

        return new Student[] {
            new Student(new Name("Chloe Lim"), new Phone("82736451"), new Email("chloelim@example.com"),
                new Address(limAddress), getTagSet(),
                school("Tampines Primary School"), level("P5"), Optional.of(limMeiLing), List.of(
                    lesson("Mathematics", DayOfWeek.TUESDAY, "16:00", "17:30", "40", HOME, LessonMode.PHYSICAL),
                    lesson("Science", DayOfWeek.THURSDAY, "16:00", "17:30", "40", ZOOM, LessonMode.VIRTUAL)),
                false, List.of()),
            new Student(new Name("Ethan Lim"), new Phone("83647592"), new Email("ethanlim@example.com"),
                new Address(limAddress), getTagSet(),
                school("Tampines Secondary School"), level("Sec 1"), Optional.of(limMeiLing), List.of(
                    lesson("English", DayOfWeek.SATURDAY, "10:00", "11:30", "45", HOME, LessonMode.PHYSICAL)),
                false, List.of()),
            new Student(new Name("Isaac Ng"), new Phone("85291746"), new Email("isaacng@example.com"),
                new Address("Blk 208 Bishan Street 23, #05-41"), getTagSet("newstudent"),
                school("Ai Tong School"), level("P5"), guardian("Ng Siew Ling", "96372815"), List.of(
                    lesson("Mathematics", DayOfWeek.MONDAY, "17:00", "18:30", "38",
                        "Bishan Public Library", LessonMode.PHYSICAL)),
                false, List.of()),
            new Student(new Name("Muhammad Hafiz bin Rahman"), new Phone("87452136"), new Email("hafiz@example.com"),
                new Address("Blk 681 Woodlands Avenue 6, #12-207"), getTagSet(),
                school("Woodlands Ring Secondary School"),
                level("Sec 3"), guardian("Rahman bin Osman", "98124573"), List.of(
                    lesson("Additional Mathematics", DayOfWeek.WEDNESDAY, "19:00", "20:30", "55",
                        HOME, LessonMode.PHYSICAL),
                    lesson("Physics", DayOfWeek.FRIDAY, "19:00", "20:30", "55", "Google Meet", LessonMode.VIRTUAL)),
                false, List.of()),
            new Student(new Name("Priya Ramasamy"), new Phone("81936274"), new Email("priyar@example.com"),
                new Address("Blk 45 Jurong West Street 42, #03-88"), getTagSet(),
                school("Jurong Secondary School"), level("Sec 3"), guardian("Ramasamy Krishnan", "92736184"), List.of(
                    lesson("Mathematics", DayOfWeek.WEDNESDAY, "20:00", "21:30", "50", ZOOM, LessonMode.VIRTUAL),
                    lesson("Chemistry", DayOfWeek.SATURDAY, "14:00", "15:30", "55", HOME, LessonMode.PHYSICAL)),
                false, List.of()),
            new Student(new Name("Ryan Goh Jun Jie"), new Phone("90817263"), new Email("ryangoh@example.com"),
                new Address("Blk 123 Ang Mo Kio Avenue 3, #10-25"), getTagSet("examprep"),
                school("Anderson Serangoon Junior College"),
                level("JC1"), guardian("Goh Siew Hwa", "97162538"), List.of(
                    lesson("H2 Mathematics", DayOfWeek.MONDAY, "19:30", "21:30", "80", HOME, LessonMode.PHYSICAL),
                    lesson("H2 Physics", DayOfWeek.THURSDAY, "19:30", "21:00", "80", ZOOM, LessonMode.VIRTUAL),
                    lesson("General Paper", DayOfWeek.SUNDAY, "10:00", "11:30", "75", ZOOM, LessonMode.VIRTUAL)),
                false, List.of()),
            new Student(new Name("Nur Aisyah binte Ismail"), new Phone("88263915"), new Email("aisyah@example.com"),
                new Address("Blk 302 Punggol Field, #14-56"), getTagSet(),
                school("Eunoia Junior College"), level("JC1"), guardian("Ismail bin Hassan", "93847261"), List.of(
                    lesson("H2 Chemistry", DayOfWeek.TUESDAY, "19:00", "21:00", "80",
                        "Punggol Regional Library", LessonMode.PHYSICAL)),
                false, List.of()),
            new Student(new Name("Sarah Wong"), new Phone("96283714"), new Email("sarahwong@example.com"),
                new Address("Blk 9 Toa Payoh Lorong 8, #02-17"), getTagSet("adult"),
                Optional.empty(), Optional.empty(), Optional.empty(), List.of(
                    lesson("Conversational Mandarin", DayOfWeek.SATURDAY, "16:00", "17:30", "60",
                        ZOOM, LessonMode.VIRTUAL)),
                false, List.of())
        };
    }

    public static ReadOnlyAddressBook getSampleAddressBook() {
        AddressBook sampleAb = new AddressBook();
        for (Student samplePerson : getSamplePersons()) {
            sampleAb.addPerson(samplePerson);
        }
        return sampleAb;
    }

    /**
     * Returns a tag set containing the list of strings given.
     */
    public static Set<Tag> getTagSet(String... strings) {
        return Arrays.stream(strings)
                .map(Tag::new)
                .collect(Collectors.toSet());
    }

    private static Optional<School> school(String school) {
        return Optional.of(new School(school));
    }

    private static Optional<SchoolingLevel> level(String level) {
        return Optional.of(new SchoolingLevel(level));
    }

    private static Optional<Guardian> guardian(String name, String phone) {
        return Optional.of(new Guardian(new Name(name), new Phone(phone)));
    }

    /** Returns a lesson with times in HH:mm and a whole-dollar SGD hourly rate. */
    private static Lesson lesson(String subject, DayOfWeek day, String start, String end, String rate,
            String location, LessonMode mode) {
        return new Lesson(new Subject(subject),
                new WeeklyLessonSlot(day, LocalTime.parse(start), LocalTime.parse(end)),
                new HourlyRate(new BigDecimal(rate)), Optional.of(new LessonLocation(location)), Optional.of(mode));
    }
}
