package seedu.address.testutil;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.function.Supplier;

import seedu.address.commons.exceptions.DataLoadingException;
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
import seedu.address.model.person.SchoolingLevel;
import seedu.address.model.person.Student;
import seedu.address.model.person.Subject;
import seedu.address.model.person.WeeklyLessonSlot;
import seedu.address.model.tag.Tag;
import seedu.address.storage.JsonAddressBookStorage;

/**
 * Writes a StudentBook data file filled with realistic Singapore tuition students.
 * Students are built from the real model classes and saved with the app's own storage, so every generated file
 * is one the app can load. Run it with, for example:
 * {@code ./gradlew seed --args="--students 500 --seed 7 --out data/studentbook.json --force"}.
 */
public class StudentBookSeeder {
    public static final int DEFAULT_STUDENTS = 40;
    public static final long DEFAULT_SEED = 2103;
    public static final Path DEFAULT_OUT = Path.of("data", "studentbook.json");

    private static final String USAGE = String.join("\n",
            "Usage: ./gradlew seed --args=\"[--students N] [--seed S] [--out FILE] [--force]\"",
            "  --students N  number of students (default " + DEFAULT_STUDENTS + "; try 500 for stress testing)",
            "  --seed S      random seed; the same seed gives the same file (default " + DEFAULT_SEED + ")",
            "  --out FILE    file to write (default " + DEFAULT_OUT + ")",
            "  --force       overwrite FILE if it already exists");

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

    private static final List<String> HDB_TOWNS = List.of("Tampines", "Bedok", "Jurong West", "Woodlands", "Yishun",
            "Ang Mo Kio", "Hougang", "Sengkang", "Punggol", "Bukit Batok", "Choa Chu Kang", "Pasir Ris", "Toa Payoh",
            "Clementi", "Bishan", "Serangoon", "Bukit Panjang", "Queenstown", "Geylang", "Sembawang");
    private static final List<String> STREET_KINDS = List.of("Street", "Avenue", "Drive", "Road");

    private static final List<String> PHYSICAL_PLACES = List.of("Student's home", "Student's home",
            "Student's home", "Tutor's home", "Tampines Regional Library", "Jurong Regional Library",
            "Woodlands Regional Library", "Bishan Public Library", "Punggol Regional Library",
            "Bedok Public Library");
    private static final List<String> VIRTUAL_PLACES = List.of("Zoom", "Zoom", "Google Meet", "Microsoft Teams");

    /** A level's weight among students, subjects, base SGD hourly rate, lesson lengths and earliest weekday start. */
    private record LevelInfo(String level, int weight, List<String> subjects, int baseRate, List<Integer> lengths,
            LocalTime earliest) {}

    private static final List<LevelInfo> LEVELS = List.of(
            new LevelInfo("P1", 2, List.of("English", "Mathematics", "Chinese"), 30, List.of(60), time(14, 0)),
            new LevelInfo("P2", 2, List.of("English", "Mathematics", "Chinese"), 30, List.of(60), time(14, 0)),
            new LevelInfo("P3", 3, List.of("English", "Mathematics", "Science", "Chinese", "Malay"), 35,
                    List.of(60, 90), time(14, 0)),
            new LevelInfo("P4", 3, List.of("English", "Mathematics", "Science", "Chinese", "Tamil"), 35,
                    List.of(60, 90), time(14, 0)),
            new LevelInfo("P5", 5, List.of("English", "Mathematics", "Science", "Chinese", "Malay"), 40,
                    List.of(90), time(14, 30)),
            new LevelInfo("P6", 5, List.of("English", "Mathematics", "Science", "Chinese", "Tamil"), 42,
                    List.of(90, 120), time(14, 30)),
            new LevelInfo("Sec 1", 4, List.of("English", "Mathematics", "Science", "Chinese", "Geography"), 45,
                    List.of(90), time(15, 30)),
            new LevelInfo("Sec 2", 4, List.of("English", "Mathematics", "Science", "History", "Literature"), 45,
                    List.of(90), time(15, 30)),
            new LevelInfo("Sec 3", 6, List.of("Mathematics", "Additional Mathematics", "Physics", "Chemistry",
                    "Biology", "English", "Principles of Accounts"), 52, List.of(90, 120), time(15, 30)),
            new LevelInfo("Sec 4", 6, List.of("Mathematics", "Additional Mathematics", "Physics", "Chemistry",
                    "Biology", "English", "Combined Science"), 55, List.of(90, 120), time(15, 30)),
            new LevelInfo("Sec 5", 1, List.of("Mathematics", "English", "Combined Science"), 55, List.of(90),
                    time(15, 30)),
            new LevelInfo("JC1", 5, List.of("H2 Mathematics", "H2 Physics", "H2 Chemistry", "H2 Biology",
                    "H2 Economics", "General Paper", "H1 Mathematics"), 75, List.of(90, 120), time(17, 0)),
            new LevelInfo("JC2", 5, List.of("H2 Mathematics", "H2 Physics", "H2 Chemistry", "H2 Biology",
                    "H2 Economics", "General Paper"), 80, List.of(120), time(17, 0)),
            // Students with no level, such as adult learners and polytechnic students.
            new LevelInfo(null, 4, List.of("Conversational Mandarin", "IELTS Preparation", "Python Programming",
                    "Engineering Mathematics", "Business Statistics"), 60, List.of(60, 90), time(19, 0)));
    private static final Set<String> EXAM_YEARS = Set.of("P6", "Sec 4", "Sec 5", "JC2");

    // Most students take one subject with this tutor, so 40 students fit a tutor's week with only some clashes.
    private static final List<Integer> LESSON_COUNTS = List.of(1, 2, 3);
    private static final List<Integer> LESSON_COUNT_WEIGHTS = List.of(75, 20, 5);
    private static final LocalTime WEEKDAY_END = time(22, 0);
    private static final LocalTime WEEKEND_START = time(8, 0);
    private static final LocalTime WEEKEND_END = time(21, 0);
    // Tries for a slot no other student has before accepting a clash; at 40 students this leaves roughly 30.
    private static final int FREE_SLOT_TRIES = 40;
    private static final int ANY_SLOT_TRIES = 20;

    private final Random rng;
    private final Set<String> names = new HashSet<>();
    private final Set<String> phones = new HashSet<>();
    private final Set<String> emails = new HashSet<>();
    private final List<WeeklyLessonSlot> booked = new ArrayList<>();

    /** Creates a seeder whose output depends only on {@code seed}. */
    public StudentBookSeeder(long seed) {
        rng = new Random(seed);
    }

    /** A family's naming pattern, so siblings get names that fit one family, and the guardian's name. */
    private record Family(Supplier<String> childName, String guardianName) {}

    /**
     * Returns {@code count} students with unique names. Some pairs are siblings who share a guardian and address.
     */
    public List<Student> generate(int count) {
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
        return new Student(new Name(name), new Phone(makePhone()), new Email(makeEmail(name)), address, tags,
                Optional.ofNullable(info.level()).map(SchoolingLevel::new), guardian, makeLessons(info, lessonCount));
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
     * Returns up to {@code count} lessons on different subjects that never overlap each other.
     * Each lesson first tries for a slot no other student has, so clashes between students happen only
     * when the week is crowded, as they would for a busy tutor.
     */
    private List<Lesson> makeLessons(LevelInfo info, int count) {
        List<String> subjects = new ArrayList<>(info.subjects());
        Collections.shuffle(subjects, rng);
        List<WeeklyLessonSlot> taken = new ArrayList<>();
        List<Lesson> lessons = new ArrayList<>();

        for (String subject : subjects.subList(0, Math.min(count, subjects.size()))) {
            for (int attempt = 0; attempt < FREE_SLOT_TRIES + ANY_SLOT_TRIES; attempt++) {
                WeeklyLessonSlot slot = randomSlot(info);
                if (!overlapsAny(slot, taken) && (attempt >= FREE_SLOT_TRIES || !overlapsAny(slot, booked))) {
                    taken.add(slot);
                    booked.add(slot);
                    lessons.add(makeLesson(subject, slot, info.baseRate()));
                    break;
                }
            }
        }
        return lessons;
    }

    private WeeklyLessonSlot randomSlot(LevelInfo info) {
        DayOfWeek day = pick(List.of(DayOfWeek.values()));
        boolean isWeekend = day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY;
        LocalTime first = isWeekend ? WEEKEND_START : info.earliest();
        LocalTime lastEnd = isWeekend ? WEEKEND_END : WEEKDAY_END;
        int length = pick(info.lengths());
        int latestStart = lastEnd.toSecondOfDay() / 60 - length;
        int halfHours = (latestStart - first.toSecondOfDay() / 60) / 30;
        LocalTime start = first.plusMinutes(30L * rng.nextInt(halfHours + 1));
        return new WeeklyLessonSlot(day, start, start.plusMinutes(length));
    }

    private Lesson makeLesson(String subject, WeeklyLessonSlot slot, int baseRate) {
        // Rates stay close to the level's base, so they rise with level; some end in .50.
        BigDecimal rate = BigDecimal.valueOf(baseRate + rng.nextInt(-3, 6));
        if (rng.nextDouble() < 0.2) {
            rate = rate.add(new BigDecimal("0.50"));
        }
        boolean isPhysical = rng.nextDouble() < 0.65;
        return new Lesson(new Subject(subject), slot, new HourlyRate(rate),
                Optional.of(new LessonLocation(pick(isPhysical ? PHYSICAL_PLACES : VIRTUAL_PLACES))),
                Optional.of(isPhysical ? LessonMode.PHYSICAL : LessonMode.VIRTUAL));
    }

    private static boolean overlaps(WeeklyLessonSlot a, WeeklyLessonSlot b) {
        return a.getDay() == b.getDay() && a.getStart().isBefore(b.getEnd()) && b.getStart().isBefore(a.getEnd());
    }

    private static boolean overlapsAny(WeeklyLessonSlot slot, List<WeeklyLessonSlot> slots) {
        return slots.stream().anyMatch(other -> overlaps(slot, other));
    }

    /** Returns the number of pairs of lessons, across all students, whose times overlap. */
    public static int countClashes(List<Student> students) {
        List<WeeklyLessonSlot> slots = students.stream()
                .flatMap(student -> student.getLessons().stream())
                .map(Lesson::getWeeklyLessonSlot)
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
        if (loaded.isEmpty() || !loaded.get().getPersonList().equals(addressBook.getPersonList())) {
            throw new IOException("The file written to " + out + " does not load back as the same students.");
        }
    }

    /** Parses the command-line options described in {@link #USAGE} and writes the file. */
    public static void main(String[] args) throws IOException, DataLoadingException {
        int count = DEFAULT_STUDENTS;
        long seed = DEFAULT_SEED;
        Path out = DEFAULT_OUT;
        boolean force = false;
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
                } else {
                    throw new IllegalArgumentException("Unknown option: " + option);
                }
            }
            if (count < 0) {
                throw new IllegalArgumentException("--students must not be negative");
            }
        } catch (IllegalArgumentException | ArrayIndexOutOfBoundsException e) {
            System.err.println((e instanceof ArrayIndexOutOfBoundsException ? "Missing option value" : e.getMessage())
                    + "\n" + USAGE);
            System.exit(2);
        }
        if (Files.exists(out) && !force) {
            System.err.println(out + " already exists; pass --force to overwrite it.");
            System.exit(2);
        }

        List<Student> students = new StudentBookSeeder(seed).generate(count);
        save(students, out);
        int lessonCount = students.stream().mapToInt(student -> student.getLessons().size()).sum();
        System.out.printf("Wrote %d students, %d lessons (%d overlapping pairs) to %s with seed %d%n",
                students.size(), lessonCount, countClashes(students), out.toAbsolutePath(), seed);
    }
}
