package seedu.address.testutil;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.function.Supplier;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.model.AddressBook;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.person.Address;
import seedu.address.model.person.Email;
import seedu.address.model.person.FollowUp;
import seedu.address.model.person.Guardian;
import seedu.address.model.person.HourlyRate;
import seedu.address.model.person.Lesson;
import seedu.address.model.person.LessonLocation;
import seedu.address.model.person.LessonMode;
import seedu.address.model.person.Name;
import seedu.address.model.person.Phone;
import seedu.address.model.person.ProgressNote;
import seedu.address.model.person.School;
import seedu.address.model.person.SchoolingLevel;
import seedu.address.model.person.Student;
import seedu.address.model.person.Subject;
import seedu.address.model.person.WeeklyLessonSlot;
import seedu.address.model.tag.Tag;
import seedu.address.model.util.SampleDataUtil;
import seedu.address.storage.JsonAddressBookStorage;

/**
 * Writes a StudentBook data file filled with realistic Singapore tuition students.
 * Students are built from the real model classes and saved with the app's own storage, so every generated file
 * is one the app can load. Run it with, for example:
 * {@code ./gradlew seed --args="--students 500 --seed 7 --out data/studentbook.json --force"}.
 */
public class StudentBookSeeder {
    public static final int DEFAULT_STUDENTS = 25;
    public static final long DEFAULT_SEED = 2103;
    /** Overlapping pairs of lessons added on purpose, so clash warnings can be tried on generated data. */
    public static final int DEFAULT_CLASHES = 2;
    public static final Path DEFAULT_OUT = Path.of("data", "studentbook.json");
    /** The break the tutor needs between lessons in different places, to travel or set up. */
    public static final int TRAVEL_MINUTES = 30;

    private static final List<String> CHINESE_SURNAMES = List.of("Tan", "Lim", "Lee", "Ng", "Ong", "Wong", "Goh",
            "Chua", "Chan", "Koh", "Teo", "Ang", "Yeo", "Tay", "Ho", "Low", "Toh", "Sim", "Chong", "Chia");
    private static final List<String> CHINESE_GIVEN = List.of("Wei Jie", "Jun Jie", "Xin Yi", "Hui Min", "Jia Hui",
            "Zhi Hao", "Kai Xuan", "Yu Xuan", "Shu Ting", "Jia Le", "Wen Hui", "Rui En", "Zi Xuan", "Yi Ling",
            "Jun Hao", "Li Ting", "Hao Ran", "Xuan Ning", "En Qi", "Kai Wen");
    private static final List<String> ENGLISH_GIVEN = List.of("Chloe", "Ethan", "Isaac", "Ryan", "Sarah", "Rachel",
            "Joshua", "Natalie", "Caleb", "Megan", "Darren", "Valerie", "Marcus", "Charmaine", "Jayden", "Ashley",
            "Gabriel", "Sophia", "Nathan", "Phoebe");
    private static final List<String> MALAY_MALE = List.of("Muhammad Hafiz", "Muhammad Irfan", "Ahmad Danial",
            "Haziq", "Aiman", "Farhan", "Iskandar", "Syafiq", "Amirul", "Zulkifli", "Rizwan", "Hakim");
    private static final List<String> MALAY_FEMALE = List.of("Nur Aisyah", "Siti Nurhaliza", "Nurul Huda", "Aqilah",
            "Farah", "Insyirah", "Alya", "Nadhirah", "Sofea", "Syahirah", "Hannah", "Liyana");
    private static final List<String> MALAY_FATHERS = List.of("Rahman", "Ismail", "Hassan", "Osman", "Yusof",
            "Ibrahim", "Salleh", "Kamal", "Rashid", "Hamzah", "Jamal", "Aziz");
    private static final List<String> INDIAN_GIVEN = List.of("Priya", "Arjun", "Divya", "Karthik", "Shreya",
            "Vikram", "Anjali", "Rohan", "Kavya", "Nikhil", "Meera", "Aravind", "Sanjana", "Pranav");
    private static final List<String> INDIAN_FATHERS = List.of("Ramasamy", "Krishnan", "Subramaniam", "Rajendran",
            "Muthu", "Nair", "Pillai", "Selvam", "Ganesan", "Kumar", "Raman", "Chandran");
    private static final List<String> EURASIAN_SURNAMES = List.of("De Souza", "Pereira", "Rodrigues", "Fernandez",
            "Oliveiro", "Shepherdson");
    /** The number of distinct names the pools above give, and so the most random students one file can hold. */
    public static final int MAX_STUDENTS = CHINESE_SURNAMES.size() * (ENGLISH_GIVEN.size() + CHINESE_GIVEN.size())
            + MALAY_FATHERS.size() * (MALAY_MALE.size() + MALAY_FEMALE.size())
            + INDIAN_FATHERS.size() * INDIAN_GIVEN.size()
            + EURASIAN_SURNAMES.size() * ENGLISH_GIVEN.size();

    private static final String USAGE = String.join("\n",
            "Usage: ./gradlew seed --args=\"[--students N] [--seed S] [--out FILE] [--force] [--clashes C] "
                    + "[--edge-cases]\"",
            "  --students N  number of students, at most " + MAX_STUDENTS + " (default "
                    + DEFAULT_STUDENTS + "; try 500 for stress testing)",
            "  --seed S      random seed; the same seed gives the same file (default " + DEFAULT_SEED + ")",
            "  --out FILE    file to write (default " + DEFAULT_OUT + ")",
            "  --force       replace FILE if it already exists",
            "  --clashes C   move lessons of C random students to overlap another student's lesson (default "
                    + DEFAULT_CLASHES + "; 0 for a clash-free week)",
            "  --edge-cases  also add fixed students with max-length fields, a Zoom link, back-to-back and",
            "                deliberately overlapping lessons on weekday mornings, 'Alex Tan' and 'alex tan',",
            "                and no lessons or guardian");

    private static final List<String> PRIMARY_SCHOOLS = List.of("Tampines Primary School",
            "Rosyth School", "Nan Hua Primary School", "Henry Park Primary School", "Ai Tong School",
            "Pei Hwa Presbyterian Primary School", "Rulang Primary School", "Woodlands Ring Primary School",
            "Punggol Green Primary School", "Red Swastika School");
    private static final List<String> SECONDARY_SCHOOLS = List.of("Bedok View Secondary School",
            "Bukit Panjang Government High School", "Chung Cheng High School (Main)", "Crescent Girls' School",
            "Dunman High School", "Hwa Chong Institution", "Jurong Secondary School", "Nan Chiau High School",
            "Raffles Girls' School (Secondary)", "Yishun Town Secondary School");
    private static final List<String> JUNIOR_COLLEGES = List.of("Anderson Serangoon Junior College",
            "Catholic Junior College", "Eunoia Junior College", "Nanyang Junior College",
            "National Junior College", "Raffles Institution", "Tampines Meridian Junior College",
            "Victoria Junior College");

    private static final List<String> HDB_TOWNS = List.of("Tampines", "Bedok", "Jurong West", "Woodlands", "Yishun",
            "Ang Mo Kio", "Hougang", "Sengkang", "Punggol", "Bukit Batok", "Choa Chu Kang", "Pasir Ris", "Toa Payoh",
            "Clementi", "Bishan", "Serangoon", "Bukit Panjang", "Queenstown", "Geylang", "Sembawang");
    private static final List<String> STREET_KINDS = List.of("Street", "Avenue", "Drive", "Road");

    private static final String STUDENTS_HOME = "Student's home";
    private static final String TUTORS_HOME = "Tutor's home";
    private static final List<String> PHYSICAL_PLACES = List.of(STUDENTS_HOME, STUDENTS_HOME,
            STUDENTS_HOME, TUTORS_HOME, "Tampines Regional Library", "Jurong Regional Library",
            "Woodlands Regional Library", "Bishan Public Library", "Punggol Regional Library",
            "Bedok Public Library");
    private static final List<String> VIRTUAL_PLACES = List.of("Zoom", "Zoom", "Google Meet", "Microsoft Teams");

    /**
     * A level's weight among students, subjects, base SGD hourly rate, lesson lengths, and the earliest weekday
     * start and latest weekday end that suit it (after school, and not too late for younger children).
     */
    private record LevelInfo(String level, int weight, List<String> subjects, int baseRate, List<Integer> lengths,
            LocalTime earliest, LocalTime latest) {}

    private static final List<LevelInfo> LEVELS = List.of(
            new LevelInfo("P1", 2, List.of("English", "Mathematics", "Chinese"), 30, List.of(60), time(14, 0),
                    time(20, 0)),
            new LevelInfo("P2", 2, List.of("English", "Mathematics", "Chinese"), 30, List.of(60), time(14, 0),
                    time(20, 0)),
            new LevelInfo("P3", 3, List.of("English", "Mathematics", "Science", "Chinese", "Malay"), 35,
                    List.of(60, 90), time(14, 0), time(20, 30)),
            new LevelInfo("P4", 3, List.of("English", "Mathematics", "Science", "Chinese", "Tamil"), 35,
                    List.of(60, 90), time(14, 0), time(20, 30)),
            new LevelInfo("P5", 5, List.of("English", "Mathematics", "Science", "Chinese", "Malay"), 40,
                    List.of(90), time(14, 30), time(20, 30)),
            new LevelInfo("P6", 5, List.of("English", "Mathematics", "Science", "Chinese", "Tamil"), 42,
                    List.of(90, 120), time(14, 30), time(20, 30)),
            new LevelInfo("Sec 1", 4, List.of("English", "Mathematics", "Science", "Chinese", "Geography"), 45,
                    List.of(90), time(15, 30), time(21, 30)),
            new LevelInfo("Sec 2", 4, List.of("English", "Mathematics", "Science", "History", "Literature"), 45,
                    List.of(90), time(15, 30), time(21, 30)),
            new LevelInfo("Sec 3", 6, List.of("Mathematics", "Additional Mathematics", "Physics", "Chemistry",
                    "Biology", "English", "Principles of Accounts"), 52, List.of(90, 120), time(15, 30),
                    time(21, 30)),
            new LevelInfo("Sec 4", 6, List.of("Mathematics", "Additional Mathematics", "Physics", "Chemistry",
                    "Biology", "English", "Combined Science"), 55, List.of(90, 120), time(15, 30), time(21, 30)),
            new LevelInfo("Sec 5", 1, List.of("Mathematics", "English", "Combined Science"), 55, List.of(90),
                    time(15, 30), time(21, 30)),
            new LevelInfo("JC1", 5, List.of("H2 Mathematics", "H2 Physics", "H2 Chemistry", "H2 Biology",
                    "H2 Economics", "General Paper", "H1 Mathematics"), 75, List.of(90, 120), time(17, 0),
                    time(22, 0)),
            new LevelInfo("JC2", 5, List.of("H2 Mathematics", "H2 Physics", "H2 Chemistry", "H2 Biology",
                    "H2 Economics", "General Paper"), 80, List.of(120), time(17, 0), time(22, 0)),
            // Students with no level, such as adult learners and polytechnic students, come after work.
            new LevelInfo(null, 4, List.of("Conversational Mandarin", "IELTS Preparation", "Python Programming",
                    "Engineering Mathematics", "Business Statistics"), 60, List.of(60, 90), time(19, 0),
                    time(22, 0)));
    private static final Set<String> EXAM_YEARS = Set.of("P6", "Sec 4", "Sec 5", "JC2");

    // Most students take one subject with this tutor.
    private static final List<Integer> LESSON_COUNTS = List.of(1, 2, 3);
    private static final List<Integer> LESSON_COUNT_WEIGHTS = List.of(75, 20, 5);

    // The tutor's week: after school on weekdays, Saturday daytime and Sunday morning, with the rest of Sunday off.
    // Weekday mornings are left free, which is where the edge-case lessons go.
    private static final LocalTime SATURDAY_START = time(9, 0);
    private static final LocalTime SATURDAY_END = time(18, 0);
    private static final LocalTime SUNDAY_START = time(9, 0);
    private static final LocalTime SUNDAY_END = time(13, 0);
    private static final int MAX_WEEKDAY_LESSONS = 4;
    private static final int MAX_SATURDAY_LESSONS = 5;
    private static final int MAX_SUNDAY_LESSONS = 2;
    /** Public libraries close at 9pm, so lessons there end by then. */
    private static final LocalTime LIBRARY_CLOSING = time(21, 0);
    /** The place of every virtual lesson and every lesson at the tutor's home, so these can run back to back. */
    private static final String TUTOR_BASE = "tutor";

    private static final double ARCHIVED_SHARE = 0.05;
    private static final LocalDate TERM_START = LocalDate.of(2026, 7, 6);
    // Past students' notes come from the first half of the year, before they stopped lessons.
    private static final LocalDate PAST_TERM_START = LocalDate.of(2026, 1, 5);
    private static final List<String> NOTE_TEXTS = List.of("Covered fractions; needs more practice on word problems.",
            "Homework done well. Revised past-year paper 2.", "Struggled with the new topic; will recap next lesson.",
            "Quiz score improved from 14/25 to 19/25.", "Missed homework; reminded to finish before next lesson.",
            "Went through school test corrections together.", "Confident with the basics; moving on to harder "
                    + "questions.");
    private static final List<String> FOLLOW_UP_TEXTS = List.of("Confirm make-up lesson date",
            "Send term fee reminder", "Share mid-year exam results", "Ask about school test schedule",
            "Check if lessons continue next term");

    private final Random rng;
    private final Set<String> names = new HashSet<>();
    private final Set<String> phones = new HashSet<>();
    private final Set<String> emails = new HashSet<>();
    /** The lessons booked so far on each day, with where each takes place. */
    private final Map<DayOfWeek, List<Booking>> week = new EnumMap<>(DayOfWeek.class);

    /** Creates a seeder whose output depends only on {@code seed}. */
    public StudentBookSeeder(long seed) {
        rng = new Random(seed);
    }

    /** A booked lesson slot and the place it is taught, as {@link #placeOf} gives it. */
    private record Booking(WeeklyLessonSlot slot, String place) {}

    /** A family's naming pattern, so siblings get names that fit one family, and the guardian's name. */
    private record Family(Supplier<String> childName, String guardianName) {}

    /**
     * Returns {@code count} random students followed, if {@code withEdgeCases}, by the fixed students from
     * {@link #makeEdgeCases()}. The random students are the same either way.
     */
    public List<Student> generate(int count, boolean withEdgeCases) {
        return generate(count, withEdgeCases, 0);
    }

    /**
     * Returns {@code count} random students with {@code clashes} deliberate clashes added (see
     * {@link #addClashes}), followed, if {@code withEdgeCases}, by the fixed students from {@link #makeEdgeCases()}.
     */
    public List<Student> generate(int count, boolean withEdgeCases, int clashes) {
        List<Student> students = addClashes(generate(count), clashes);
        if (withEdgeCases) {
            students.addAll(makeEdgeCases());
        }
        return students;
    }

    /**
     * Returns {@code count} students with unique names. Some pairs are siblings who share a guardian and address.
     *
     * @throws IllegalArgumentException if {@code count} is more than {@link #MAX_STUDENTS}.
     */
    public List<Student> generate(int count) {
        if (count > MAX_STUDENTS) {
            throw new IllegalArgumentException("Cannot generate more than " + MAX_STUDENTS + " students with "
                    + "unique names, but " + count + " were requested.");
        }
        List<LevelInfo> schoolLevels = LEVELS.stream().filter(info -> info.level() != null).toList();
        List<Student> students = new ArrayList<>();

        while (students.size() < count) {
            Family family = makeFamily();
            LevelInfo levelInfo = weighted(LEVELS, LEVELS.stream().map(LevelInfo::weight).toList());
            // Adults and some older students have no guardian on record.
            boolean hasGuardian = levelInfo.level() != null && rng.nextDouble() < 0.9;
            Optional<Guardian> guardian = hasGuardian
                    ? Optional.of(new Guardian(new Name(family.guardianName()), new Phone(makePhone())))
                    : Optional.empty();
            Address address = makeAddress();
            int siblingCount = hasGuardian && rng.nextDouble() < 0.15 && count - students.size() >= 2 ? 2 : 1;

            for (int i = 0; i < siblingCount; i++) {
                String name = family.childName().get();
                if (!names.add(name)) {
                    break;
                }
                // A sibling is at a different school level from the first child.
                LevelInfo info = levelInfo;
                if (i > 0) {
                    List<LevelInfo> others = schoolLevels.stream()
                            .filter(other -> !other.level().equals(levelInfo.level())).toList();
                    info = pick(others);
                }
                students.add(makeStudent(name, address, info, guardian, i > 0));
            }
        }
        return students;
    }

    /**
     * Returns {@code students} with up to {@code clashes} lessons moved so that each partly overlaps one lesson of
     * a different current student, as when a tutor reschedules into a busy slot. Each move adds exactly one
     * overlapping pair, and every moved lesson belongs to a different student. Fewer clashes are added only if
     * too few students have lessons.
     */
    private List<Student> addClashes(List<Student> students, int clashes) {
        List<Student> result = new ArrayList<>(students);
        List<Integer> candidates = new ArrayList<>();
        for (int i = 0; i < result.size(); i++) {
            if (!result.get(i).getLessons().isEmpty()) {
                candidates.add(i);
            }
        }
        Collections.shuffle(candidates, rng);
        int added = 0;
        for (int mover : candidates) {
            if (added == clashes) {
                break;
            }
            Optional<Student> moved = moveIntoClash(result, mover);
            if (moved.isPresent()) {
                result.set(mover, moved.get());
                added++;
            }
        }
        return result;
    }

    /**
     * Returns the student at {@code mover} with one lesson moved to start or end 30 minutes into another
     * student's lesson, or nothing if no move adds exactly one overlapping pair.
     */
    private Optional<Student> moveIntoClash(List<Student> students, int mover) {
        Student student = students.get(mover);
        int clashesBefore = countClashes(students);
        List<Lesson> targets = new ArrayList<>();
        for (int i = 0; i < students.size(); i++) {
            if (i != mover) {
                targets.addAll(students.get(i).getLessons());
            }
        }
        Collections.shuffle(targets, rng);
        int position = rng.nextInt(student.getLessons().size());
        Lesson lesson = student.getLessons().get(position);
        int length = (int) Duration.between(lesson.getStart(), lesson.getEnd()).toMinutes();

        for (Lesson target : targets) {
            LocalTime start = rng.nextBoolean()
                    ? target.getStart().plusMinutes(30)
                    : target.getStart().minusMinutes(length - 30L);
            // Stay out of weekday mornings, which are kept for the edge-case lessons.
            boolean isWeekend = target.getDay() == DayOfWeek.SATURDAY || target.getDay() == DayOfWeek.SUNDAY;
            if (start.isBefore(isWeekend ? SATURDAY_START : time(14, 0))
                    || start.plusMinutes(length).isAfter(time(22, 0))) {
                continue;
            }
            List<Lesson> lessons = new ArrayList<>(student.getLessons());
            lessons.set(position, new Lesson(lesson.getSubject(),
                    new WeeklyLessonSlot(target.getDay(), start, start.plusMinutes(length)), lesson.getHourlyRate(),
                    lesson.getLocation(), lesson.getMode()));
            Student moved = new Student(student.getName(), student.getPhone(), student.getEmail(),
                    student.getAddress(), student.getTags(), student.getSchool(), student.getSchoolingLevel(),
                    student.getGuardian(), lessons, student.isArchived(), student.getNotes(), student.getFollowUps());
            List<Student> after = new ArrayList<>(students);
            after.set(mover, moved);
            if (countClashes(after) == clashesBefore + 1 && countClashes(List.of(moved)) == 0) {
                return Optional.of(moved);
            }
        }
        return Optional.empty();
    }

    /**
     * Returns fixed students for boundary testing. Their names never come from the random generator.
     * Their lessons are on weekday mornings, when the generated tutor never teaches, so they clash only with
     * each other, as intended: lessons on Monday and Friday are back to back (not clashes); Wednesday has a
     * partial and a contained overlap, and Thursday has two lessons in the same slot.
     */
    private List<Student> makeEdgeCases() {
        return List.of(
                // Same name apart from letter case; both load because names are compared exactly.
                edgeStudent("Alex Tan", "P4", "Tan Boon Huat", List.of(
                        lesson("Mathematics", DayOfWeek.MONDAY, "10:00", "11:30", "36", "Student's home",
                                LessonMode.PHYSICAL))),
                edgeStudent("alex tan", "Sec 2", "Tan Mei Hua", List.of(
                        lesson("English", DayOfWeek.MONDAY, "11:30", "13:00", "46", "Zoom", LessonMode.VIRTUAL))),
                // Every capped field at its limit: school 60, level 30, subject 40, location 100, note 500 and
                // follow-up 200 characters.
                new Student(new Name("Muhammad Nur Iskandar Hakim bin Abdul Rahman Shah Zulkarnain Al Habshi"),
                        new Phone(makePhone()),
                        new Email(makeEmail("Muhammad Nur Iskandar Hakim bin Abdul Rahman Shah Zulkarnain Al Habshi")),
                        new Address("Blk 999B Upper Bukit Timah Road, Hillview Heights Residences Tower 2, "
                                + "#25-1234, Singapore 678999"),
                        SampleDataUtil.getTagSet("examyear", "newstudent", "siblingdiscount", "scholarshipapplicant"),
                        Optional.of(new School("Bukit Timah Integrated Programme Secondary School (Campus 2)")),
                        Optional.of(new SchoolingLevel("Sec 4 Express Integrated Prog.")),
                        Optional.of(new Guardian(new Name("Abdul Rahman Shah Zulkarnain bin Ismail Al Habshi"),
                                new Phone(makePhone()))),
                        List.of(lesson("Combined Science (Physics and Chemistry)", DayOfWeek.TUESDAY, "11:00",
                                "13:00", "58.50", "Blk 999B Upper Bukit Timah Road, Hillview Heights Clubhouse, "
                                        + "Function Room 2, level 3, near the pool", LessonMode.PHYSICAL)),
                        false, List.of(new ProgressNote(TERM_START, "x".repeat(ProgressNote.MAX_TEXT_LENGTH))),
                        List.of(new FollowUp("y".repeat(FollowUp.MAX_TEXT_LENGTH)))),
                // Two lessons of one student, back to back.
                edgeStudent("Brandon Lim", "P6", "Lim Kok Leong", List.of(
                        lesson("Mathematics", DayOfWeek.FRIDAY, "10:00", "11:00", "42", "Tutor's home",
                                LessonMode.PHYSICAL),
                        lesson("Science", DayOfWeek.FRIDAY, "11:00", "12:30", "42", "Tutor's home",
                                LessonMode.PHYSICAL))),
                // Wednesday: Haikal overlaps the end of Kavitha's lesson; Gwendolyn's lies inside it
                // and ends as Haikal's starts.
                edgeStudent("Kavitha Nair", "JC1", "Nair Suresh", List.of(
                        lesson("H2 Physics", DayOfWeek.WEDNESDAY, "09:00", "11:00", "78", "Student's home",
                                LessonMode.PHYSICAL))),
                edgeStudent("Haikal bin Osman", "Sec 3", "Osman bin Salleh", List.of(
                        lesson("Physics", DayOfWeek.WEDNESDAY, "10:00", "11:30", "52", "Google Meet",
                                LessonMode.VIRTUAL))),
                edgeStudent("Gwendolyn Teo", "Sec 4", "Teo Ai Ling", List.of(
                        lesson("Chemistry", DayOfWeek.WEDNESDAY, "09:15", "10:00", "55", "Microsoft Teams",
                                LessonMode.VIRTUAL))),
                // Thursday: two lessons in exactly the same slot; Bryan's location is a full Zoom link.
                edgeStudent("Bryan Koh", "JC2", "Koh Swee Lan", List.of(
                        lesson("H2 Chemistry", DayOfWeek.THURSDAY, "10:00", "11:30", "82",
                                "https://nus-sg.zoom.us/j/81234567890?pwd=Q2hlbUx1c3NvbnNXZWVrbHk1",
                                LessonMode.VIRTUAL))),
                edgeStudent("Clement Chua", "Sec 4", null, List.of(
                        lesson("Additional Mathematics", DayOfWeek.THURSDAY, "10:00", "11:30", "56",
                                "Student's home", LessonMode.PHYSICAL))),
                // No lessons; then no lessons, no guardian, no level and no school.
                edgeStudent("Siti Aminah binte Yusof", "P6", "Yusof bin Hamzah", List.of()),
                edgeStudent("Theresa Pereira", null, null, List.of()),
                // Archived, so hidden from the default list.
                archived(edgeStudent("Daniel Ho", "Sec 1", "Ho Mei Fong", List.of(
                        lesson("Mathematics", DayOfWeek.TUESDAY, "09:00", "10:30", "45", "Student's home",
                                LessonMode.PHYSICAL)))));
    }

    /** Returns a student with a random phone, email and address; {@code level} and {@code guardianName} may be null. */
    private Student edgeStudent(String name, String level, String guardianName, List<Lesson> lessons) {
        if (!names.add(name)) {
            throw new IllegalStateException("Edge-case name is also a generated name: " + name);
        }
        Optional<Guardian> guardian = Optional.ofNullable(guardianName)
                .map(guardianNameValue -> new Guardian(new Name(guardianNameValue), new Phone(makePhone())));
        return new Student(new Name(name), new Phone(makePhone()), new Email(makeEmail(name)), makeAddress(),
                Set.of(), makeSchool(level), Optional.ofNullable(level).map(SchoolingLevel::new), guardian, lessons,
                false, List.of());
    }

    private static Student archived(Student student) {
        return new Student(student.getName(), student.getPhone(), student.getEmail(), student.getAddress(),
                student.getTags(), student.getSchool(), student.getSchoolingLevel(), student.getGuardian(),
                student.getLessons(), true, student.getNotes(), student.getFollowUps());
    }

    private static Lesson lesson(String subject, DayOfWeek day, String start, String end, String rate,
            String location, LessonMode mode) {
        return new Lesson(new Subject(subject), new WeeklyLessonSlot(day, LocalTime.parse(start), LocalTime.parse(end)),
                new HourlyRate(new BigDecimal(rate)), Optional.of(new LessonLocation(location)), Optional.of(mode));
    }

    private Student makeStudent(String name, Address address, LevelInfo info, Optional<Guardian> guardian,
            boolean isSibling) {
        Set<Tag> tags = new HashSet<>();
        if (info.level() != null && EXAM_YEARS.contains(info.level()) && rng.nextDouble() < 0.6) {
            tags.add(new Tag("examyear"));
        }
        if (rng.nextDouble() < 0.1) {
            tags.add(new Tag("newstudent"));
        }
        if (isSibling && rng.nextDouble() < 0.5) {
            tags.add(new Tag("siblingdiscount"));
        }
        int lessonCount = weighted(LESSON_COUNTS, LESSON_COUNT_WEIGHTS);
        boolean hasLeft = rng.nextDouble() < ARCHIVED_SHARE;
        List<Lesson> lessons = hasLeft ? List.of() : makeLessons(info, lessonCount, address);
        // Students who left, or who no longer fit the tutor's full week, are past students: archived, with their
        // weekly slots given up so they never clash with current students.
        boolean isArchived = lessons.isEmpty();
        List<FollowUp> followUps = !isArchived && guardian.isPresent() && rng.nextDouble() < 0.2
                ? List.of(new FollowUp(pick(FOLLOW_UP_TEXTS)))
                : List.of();
        List<ProgressNote> notes = makeNotes(rng.nextInt(3), isArchived ? PAST_TERM_START : TERM_START);
        return new Student(new Name(name), new Phone(makePhone()), new Email(makeEmail(name)), address, tags,
                makeSchool(info.level()), Optional.ofNullable(info.level()).map(SchoolingLevel::new), guardian,
                lessons, isArchived, notes, followUps);
    }

    /** Returns a school that fits {@code level}, or none for students with no level. */
    private Optional<School> makeSchool(String level) {
        if (level == null) {
            return Optional.empty();
        }
        List<String> schools = level.startsWith("P") ? PRIMARY_SCHOOLS
                : level.startsWith("JC") ? JUNIOR_COLLEGES
                : SECONDARY_SCHOOLS;
        return Optional.of(new School(pick(schools)));
    }

    /** Returns {@code count} progress notes from the term starting on {@code termStart}, oldest first. */
    private List<ProgressNote> makeNotes(int count, LocalDate termStart) {
        List<ProgressNote> notes = new ArrayList<>();
        LocalDate date = termStart;
        for (int i = 0; i < count; i++) {
            date = date.plusWeeks(rng.nextInt(1, 4));
            notes.add(new ProgressNote(date, pick(NOTE_TEXTS)));
        }
        return notes;
    }

    private Family makeFamily() {
        String culture = weighted(List.of("chinese", "malay", "indian", "eurasian"), List.of(70, 14, 11, 5));
        if (culture.equals("chinese")) {
            String surname = pick(CHINESE_SURNAMES);
            return new Family(() -> rng.nextBoolean()
                    ? pick(ENGLISH_GIVEN) + " " + surname
                    : surname + " " + pick(CHINESE_GIVEN), surname + " " + pick(CHINESE_GIVEN));
        }
        if (culture.equals("malay")) {
            String father = pick(MALAY_FATHERS);
            return new Family(() -> rng.nextBoolean()
                    ? pick(MALAY_MALE) + " bin " + father
                    : pick(MALAY_FEMALE) + " binte " + father, father + " bin " + pick(MALAY_FATHERS));
        }
        if (culture.equals("indian")) {
            String father = pick(INDIAN_FATHERS);
            String grandfather = pick(INDIAN_FATHERS.stream().filter(n -> !n.equals(father)).toList());
            return new Family(() -> pick(INDIAN_GIVEN) + " " + father, father + " " + grandfather);
        }
        String surname = pick(EURASIAN_SURNAMES);
        return new Family(() -> pick(ENGLISH_GIVEN) + " " + surname, pick(ENGLISH_GIVEN) + " " + surname);
    }

    private Address makeAddress() {
        String block = rng.nextInt(1, 1000) + pick(List.of("", "", "", "A", "B"));
        String street = pick(HDB_TOWNS) + " " + pick(STREET_KINDS) + " " + rng.nextInt(10, 100);
        return new Address(String.format("Blk %s %s, #%02d-%d", block, street, rng.nextInt(2, 26),
                rng.nextInt(1, 451)));
    }

    private String makePhone() {
        String phone;
        do {
            phone = pick(List.of("8", "9")) + String.format("%07d", rng.nextInt(10_000_000));
        } while (!phones.add(phone));
        return phone;
    }

    private String makeEmail(String name) {
        String local = String.join(".", name.toLowerCase(Locale.ROOT)
                .replaceAll("\\b(bin|binte)\\b", " ").trim().split("\\s+"));
        String email = local + "@example.com";
        for (int n = 2; !emails.add(email); n++) {
            email = local + n + "@example.com";
        }
        return email;
    }

    /**
     * Returns up to {@code count} lessons on different subjects, each in a free slot of the tutor's week.
     * A slot is free if it leaves the tutor {@link #TRAVEL_MINUTES} between it and every other lesson that day
     * in a different place, and the day is not already full. Lessons that find no free slot are left out, so
     * the result may be empty once the week is full.
     */
    private List<Lesson> makeLessons(LevelInfo info, int count, Address address) {
        List<String> subjects = new ArrayList<>(info.subjects());
        Collections.shuffle(subjects, rng);
        List<Lesson> lessons = new ArrayList<>();

        for (String subject : subjects.subList(0, Math.min(count, subjects.size()))) {
            boolean isPhysical = rng.nextDouble() < 0.65;
            String location = pick(isPhysical ? PHYSICAL_PLACES : VIRTUAL_PLACES);
            String place = placeOf(location, isPhysical, address);
            List<WeeklyLessonSlot> free = freeSlots(info, pick(info.lengths()), location, place);
            if (free.isEmpty()) {
                continue;
            }
            // Like a real tutor, keep each day's lessons together rather than scattered with long idle gaps.
            List<WeeklyLessonSlot> snug = free.stream().filter(candidate -> isSnug(candidate, place)).toList();
            WeeklyLessonSlot slot = pick(snug.isEmpty() ? free : snug);
            week.computeIfAbsent(slot.getDay(), day -> new ArrayList<>()).add(new Booking(slot, place));
            lessons.add(makeLesson(subject, slot, info.baseRate(), location, isPhysical));
        }
        return lessons;
    }

    /**
     * Returns where the tutor has to be for a lesson. Virtual lessons and lessons at the tutor's home share one
     * place, and a lesson at the student's home is at the student's address, so siblings share it.
     */
    private static String placeOf(String location, boolean isPhysical, Address address) {
        if (!isPhysical || location.equals(TUTORS_HOME)) {
            return TUTOR_BASE;
        }
        return location.equals(STUDENTS_HOME) ? address.value : location;
    }

    /**
     * Returns every half-hour-aligned slot of {@code length} minutes at {@code location} that suits {@code info}
     * and is free.
     */
    private List<WeeklyLessonSlot> freeSlots(LevelInfo info, int length, String location, String place) {
        List<WeeklyLessonSlot> free = new ArrayList<>();
        for (DayOfWeek day : DayOfWeek.values()) {
            LocalTime first = switch (day) {
                case SATURDAY -> SATURDAY_START;
                case SUNDAY -> SUNDAY_START;
                default -> info.earliest();
            };
            LocalTime lastEnd = switch (day) {
                case SATURDAY -> SATURDAY_END;
                case SUNDAY -> SUNDAY_END;
                default -> info.latest();
            };
            if (location.endsWith("Library") && lastEnd.isAfter(LIBRARY_CLOSING)) {
                lastEnd = LIBRARY_CLOSING;
            }
            for (LocalTime start = first; !start.plusMinutes(length).isAfter(lastEnd); start = start.plusMinutes(30)) {
                WeeklyLessonSlot slot = new WeeklyLessonSlot(day, start, start.plusMinutes(length));
                if (isFree(slot, place)) {
                    free.add(slot);
                }
            }
        }
        return free;
    }

    private boolean isFree(WeeklyLessonSlot slot, String place) {
        List<Booking> bookings = week.getOrDefault(slot.getDay(), List.of());
        return bookings.size() < maxLessons(slot.getDay()) && bookings.stream().allMatch(booking -> {
            long gap = booking.place().equals(place) ? 0 : TRAVEL_MINUTES;
            return !slot.getStart().isBefore(booking.slot().getEnd().plusMinutes(gap))
                    || !booking.slot().getStart().isBefore(slot.getEnd().plusMinutes(gap));
        });
    }

    /** Returns whether {@code slot} starts or ends exactly one break away from a lesson already that day. */
    private boolean isSnug(WeeklyLessonSlot slot, String place) {
        return week.getOrDefault(slot.getDay(), List.of()).stream().anyMatch(booking -> {
            long gap = booking.place().equals(place) ? 0 : TRAVEL_MINUTES;
            return slot.getStart().equals(booking.slot().getEnd().plusMinutes(gap))
                    || booking.slot().getStart().equals(slot.getEnd().plusMinutes(gap));
        });
    }

    /** Returns the most lessons the tutor teaches on {@code day}. */
    public static int maxLessons(DayOfWeek day) {
        return switch (day) {
            case SATURDAY -> MAX_SATURDAY_LESSONS;
            case SUNDAY -> MAX_SUNDAY_LESSONS;
            default -> MAX_WEEKDAY_LESSONS;
        };
    }

    private Lesson makeLesson(String subject, WeeklyLessonSlot slot, int baseRate, String location,
            boolean isPhysical) {
        // Rates stay close to the level's base, so they rise with level; some end in .50.
        BigDecimal rate = BigDecimal.valueOf(baseRate + rng.nextInt(-3, 6));
        if (rng.nextDouble() < 0.2) {
            rate = rate.add(new BigDecimal("0.50"));
        }
        return new Lesson(new Subject(subject), slot, new HourlyRate(rate),
                Optional.of(new LessonLocation(location)),
                Optional.of(isPhysical ? LessonMode.PHYSICAL : LessonMode.VIRTUAL));
    }

    private static boolean overlaps(WeeklyLessonSlot a, WeeklyLessonSlot b) {
        return a.getDay() == b.getDay() && a.getStart().isBefore(b.getEnd()) && b.getStart().isBefore(a.getEnd());
    }

    /** Returns the number of pairs of lessons, across all students, whose times overlap. */
    public static int countClashes(List<Student> students) {
        List<WeeklyLessonSlot> slots = students.stream()
                .flatMap(student -> student.getLessons().stream())
                .map(Lesson::getLessonSlot)
                .toList();
        int clashes = 0;
        for (int i = 0; i < slots.size(); i++) {
            for (int j = i + 1; j < slots.size(); j++) {
                if (overlaps(slots.get(i), slots.get(j))) {
                    clashes++;
                }
            }
        }
        return clashes;
    }

    private <T> T pick(List<T> items) {
        return items.get(rng.nextInt(items.size()));
    }

    private <T> T weighted(List<T> items, List<Integer> weights) {
        int roll = rng.nextInt(weights.stream().mapToInt(Integer::intValue).sum());
        for (int i = 0; i < items.size(); i++) {
            roll -= weights.get(i);
            if (roll < 0) {
                return items.get(i);
            }
        }
        throw new AssertionError("weights must be positive");
    }

    private static LocalTime time(int hour, int minute) {
        return LocalTime.of(hour, minute);
    }

    /**
     * Saves {@code students} to {@code out} with the app's storage, then reads the file back to check that the
     * app will load exactly these students rather than start empty.
     */
    public static void save(List<Student> students, Path out) throws IOException, DataLoadingException {
        AddressBook addressBook = new AddressBook();
        students.forEach(addressBook::addPerson);
        JsonAddressBookStorage storage = new JsonAddressBookStorage(out);
        storage.saveAddressBook(addressBook);
        Optional<ReadOnlyAddressBook> loaded = storage.readAddressBook();
        if (loaded.isEmpty() || !loaded.get().getPersonList().equals(addressBook.getPersonList())
                || !loaded.get().getArchivedPersonList().equals(addressBook.getArchivedPersonList())) {
            throw new IOException("The file written to " + out + " does not load back as the same students.");
        }
    }

    /** Parses the command-line options described in {@link #USAGE} and writes the file. */
    public static void main(String[] args) throws IOException, DataLoadingException {
        int count = DEFAULT_STUDENTS;
        long seed = DEFAULT_SEED;
        Path out = DEFAULT_OUT;
        boolean force = false;
        boolean withEdgeCases = false;
        int clashes = DEFAULT_CLASHES;
        try {
            for (int i = 0; i < args.length; i++) {
                String option = args[i];
                if (option.equals("--students")) {
                    count = Integer.parseInt(args[++i]);
                } else if (option.equals("--seed")) {
                    seed = Long.parseLong(args[++i]);
                } else if (option.equals("--out")) {
                    out = Path.of(args[++i]);
                } else if (option.equals("--force")) {
                    force = true;
                } else if (option.equals("--clashes")) {
                    clashes = Integer.parseInt(args[++i]);
                } else if (option.equals("--edge-cases")) {
                    withEdgeCases = true;
                } else {
                    throw new IllegalArgumentException("Unknown option: " + option);
                }
            }
            if (clashes < 0) {
                throw new IllegalArgumentException("--clashes must not be negative");
            }
            if (count < 0) {
                throw new IllegalArgumentException("--students must not be negative");
            }
            if (count > MAX_STUDENTS) {
                throw new IllegalArgumentException("--students must be at most " + MAX_STUDENTS
                        + ", the number of distinct names the generator can make");
            }
        } catch (IllegalArgumentException | ArrayIndexOutOfBoundsException e) {
            System.err.println((e instanceof ArrayIndexOutOfBoundsException ? "Missing option value" : e.getMessage())
                    + "\n" + USAGE);
            System.exit(2);
        }
        // The default output is the app's own data file, so never replace it unless asked to.
        if (Files.exists(out) && !force) {
            System.err.println(out + " already exists; pass --force to replace it.");
            System.exit(2);
        }

        List<Student> students = new StudentBookSeeder(seed).generate(count, withEdgeCases, clashes);
        save(students, out);
        int lessonCount = students.stream().mapToInt(student -> student.getLessons().size()).sum();
        System.out.printf("Wrote %d students, %d lessons (%d overlapping pairs) to %s with seed %d%n",
                students.size(), lessonCount, countClashes(students), out.toAbsolutePath(), seed);
    }
}
