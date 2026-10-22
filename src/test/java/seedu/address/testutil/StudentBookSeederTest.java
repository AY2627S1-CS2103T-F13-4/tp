package seedu.address.testutil;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.model.person.FollowUp;
import seedu.address.model.person.Guardian;
import seedu.address.model.person.HourlyRate;
import seedu.address.model.person.Lesson;
import seedu.address.model.person.ProgressNote;
import seedu.address.model.person.SchoolingLevel;
import seedu.address.model.person.Student;
import seedu.address.model.person.Subject;
import seedu.address.model.person.WeeklyLessonSlot;
import seedu.address.storage.JsonAddressBookStorage;

public class StudentBookSeederTest {

    private static final List<Student> STUDENTS = new StudentBookSeeder(StudentBookSeeder.DEFAULT_SEED).generate(200);
    private static final List<Student> EDGE_CASES = new StudentBookSeeder(1).generate(0, true);

    @TempDir
    public Path tempDir;

    @Test
    public void generate_sameSeed_sameStudents() {
        assertEquals(new StudentBookSeeder(7).generate(40), new StudentBookSeeder(7).generate(40));
    }

    @Test
    public void generate_returnsRequestedCountWithUniqueNames() {
        assertEquals(0, new StudentBookSeeder(1).generate(0).size());
        assertEquals(200, STUDENTS.size());
        assertEquals(200, STUDENTS.stream().map(Student::getName).distinct().count());
    }

    @Test
    public void generate_moreThanMaxStudents_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                new StudentBookSeeder(1).generate(StudentBookSeeder.MAX_STUDENTS + 1));
    }

    @Test
    public void generate_maxStudents_returnsAllUniqueNames() {
        List<Student> students = new StudentBookSeeder(1).generate(StudentBookSeeder.MAX_STUDENTS, true);

        assertEquals(students.size(), students.stream().map(Student::getName).distinct().count());
    }

    @Test
    public void generate_schoolStudentsHaveSchoolsAndOthersDoNot() {
        for (Student student : STUDENTS) {
            assertEquals(student.getSchoolingLevel().isPresent(), student.getSchool().isPresent(),
                    student.getName().fullName);
        }
    }

    @Test
    public void generate_someStudentsArchivedOrWithNotesAndFollowUps() {
        long archived = STUDENTS.stream().filter(Student::isArchived).count();

        assertTrue(archived > 0 && archived < STUDENTS.size() / 5, "archived: " + archived);
        assertTrue(STUDENTS.stream().anyMatch(student -> !student.getNotes().isEmpty()));
        assertTrue(STUDENTS.stream().anyMatch(student -> !student.getFollowUps().isEmpty()));
    }

    @Test
    public void generate_coversEveryLevelGroup() {
        Set<SchoolingLevel.Stage> stages = STUDENTS.stream().map(Student::getStage).collect(Collectors.toSet());
        assertEquals(EnumSet.allOf(SchoolingLevel.Stage.class), stages);
    }

    @Test
    public void generate_ratesRiseWithLevel() {
        Map<SchoolingLevel.Stage, Double> averageRates = STUDENTS.stream()
                .flatMap(student -> student.getLessons().stream()
                        .map(lesson -> Map.entry(student.getStage(), lesson.getHourlyRate().value.doubleValue())))
                .collect(Collectors.groupingBy(Map.Entry::getKey, Collectors.averagingDouble(Map.Entry::getValue)));
        assertTrue(averageRates.get(SchoolingLevel.Stage.PRIMARY) < averageRates.get(SchoolingLevel.Stage.SECONDARY));
        assertTrue(averageRates.get(SchoolingLevel.Stage.SECONDARY) < averageRates.get(SchoolingLevel.Stage.JC));
    }

    @Test
    public void generate_hasSiblingsSharingGuardianAndAddress() {
        Map<Optional<Guardian>, List<Student>> byGuardian = STUDENTS.stream()
                .filter(student -> student.getGuardian().isPresent())
                .collect(Collectors.groupingBy(Student::getGuardian));
        List<List<Student>> families = byGuardian.values().stream().filter(family -> family.size() > 1).toList();

        assertFalse(families.isEmpty());
        for (List<Student> family : families) {
            assertEquals(1, family.stream().map(Student::getAddress).distinct().count());
        }
    }

    @Test
    public void generate_ownLessonsNeverOverlap() {
        for (Student student : STUDENTS) {
            assertEquals(0, StudentBookSeeder.countClashes(List.of(student)), student.getName().fullName);
        }
    }

    @Test
    public void generate_mostlyOneLessonPerStudent() {
        double average = STUDENTS.stream().mapToInt(student -> student.getLessons().size()).average().orElse(0);
        assertTrue(average >= 1 && average < 2, "average lessons: " + average);
        assertEquals(2, STUDENTS.stream().map(Student::getLessons).flatMap(List::stream).map(Lesson::getMode)
                .distinct().count());
    }

    @Test
    public void save_writesFileTheAppLoads() throws Exception {
        Path out = tempDir.resolve("studentbook.json");
        StudentBookSeeder.save(STUDENTS, out);

        assertEquals(STUDENTS, new JsonAddressBookStorage(out).readAddressBook().orElseThrow().getPersonList());
    }

    @Test
    public void generateWithEdgeCases_keepsRandomStudentsAndAppendsEdgeCases() {
        List<Student> withEdgeCases = new StudentBookSeeder(StudentBookSeeder.DEFAULT_SEED).generate(40, true);

        assertEquals(new StudentBookSeeder(StudentBookSeeder.DEFAULT_SEED).generate(40),
                withEdgeCases.subList(0, 40));
        assertEquals(EDGE_CASES.stream().map(Student::getName).toList(),
                withEdgeCases.subList(40, withEdgeCases.size()).stream().map(Student::getName).toList());
    }

    @Test
    public void generateWithEdgeCases_namesDifferOnlyInCase() {
        Student upper = edgeCase("Alex Tan");
        Student lower = edgeCase("alex tan");

        assertTrue(upper.getName().fullName.equalsIgnoreCase(lower.getName().fullName));
        assertFalse(upper.isSamePerson(lower));
    }

    @Test
    public void generateWithEdgeCases_cappedFieldsAtTheirLimits() {
        Student longest = edgeCase("Muhammad Nur Iskandar Hakim bin Abdul Rahman Shah Zulkarnain Al Habshi");
        Lesson lesson = longest.getLessons().get(0);

        assertEquals(60, longest.getSchool().orElseThrow().value.length());
        assertEquals(30, longest.getSchoolingLevel().orElseThrow().value.length());
        assertEquals(SchoolingLevel.Stage.SECONDARY, longest.getStage());
        assertEquals(40, lesson.getSubject().value.length());
        assertEquals(100, lesson.getLocation().orElseThrow().value.length());
        assertEquals(ProgressNote.MAX_TEXT_LENGTH, longest.getNotes().get(0).getText().length());
        assertEquals(FollowUp.MAX_TEXT_LENGTH, longest.getFollowUps().get(0).getText().length());
    }

    @Test
    public void generateWithEdgeCases_hasZoomLink() {
        assertTrue(edgeCase("Bryan Koh").getLessons().get(0).getLocation().orElseThrow().value
                .startsWith("https://nus-sg.zoom.us/j/"));
    }

    @Test
    public void generateWithEdgeCases_backToBackLessonsDoNotClash() {
        assertEquals(0, StudentBookSeeder.countClashes(List.of(edgeCase("Alex Tan"), edgeCase("alex tan"))));
        assertEquals(0, StudentBookSeeder.countClashes(List.of(edgeCase("Brandon Lim"))));
    }

    @Test
    public void generateWithEdgeCases_overlappingLessonsClash() {
        // Kavitha overlaps Haikal (partly) and Gwendolyn (fully); Haikal and Gwendolyn just touch at 8pm.
        assertEquals(2, StudentBookSeeder.countClashes(List.of(edgeCase("Kavitha Nair"), edgeCase("Haikal bin Osman"),
                edgeCase("Gwendolyn Teo"))));
        assertEquals(1, StudentBookSeeder.countClashes(List.of(edgeCase("Bryan Koh"), edgeCase("Clement Chua"))));
    }

    @Test
    public void generateWithEdgeCases_hasStudentsWithoutLessonsOrGuardian() {
        assertTrue(edgeCase("Siti Aminah binte Yusof").getLessons().isEmpty());
        assertTrue(edgeCase("Clement Chua").getGuardian().isEmpty());
        Student bare = edgeCase("Theresa Pereira");
        assertTrue(bare.getLessons().isEmpty() && bare.getGuardian().isEmpty()
                && bare.getSchoolingLevel().isEmpty() && bare.getSchool().isEmpty());
    }

    @Test
    public void generateWithEdgeCases_hasArchivedStudent() {
        assertTrue(edgeCase("Daniel Ho").isArchived());
        assertEquals(1, EDGE_CASES.stream().filter(Student::isArchived).count());
    }

    @Test
    public void saveWithEdgeCases_writesFileTheAppLoads() throws Exception {
        List<Student> students = new StudentBookSeeder(3).generate(40, true);
        Path out = tempDir.resolve("edge.json");
        StudentBookSeeder.save(students, out);

        assertEquals(students, new JsonAddressBookStorage(out).readAddressBook().orElseThrow().getPersonList());
    }

    private static Student edgeCase(String name) {
        return EDGE_CASES.stream().filter(student -> student.getName().fullName.equals(name)).findFirst()
                .orElseThrow();
    }

    @Test
    public void countClashes_countsOverlappingPairsOnly() {
        Student first = new PersonBuilder().withName("First")
                .withLessons(lesson(DayOfWeek.MONDAY, 16, 18), lesson(DayOfWeek.TUESDAY, 16, 18)).build();
        // Overlaps Monday 4-6pm; Tuesday 6-7pm is back to back with 4-6pm, which is not a clash.
        Student second = new PersonBuilder().withName("Second")
                .withLessons(lesson(DayOfWeek.MONDAY, 17, 19), lesson(DayOfWeek.TUESDAY, 18, 19)).build();

        assertEquals(1, StudentBookSeeder.countClashes(List.of(first, second)));
        assertEquals(0, StudentBookSeeder.countClashes(List.of()));
    }

    private static Lesson lesson(DayOfWeek day, int startHour, int endHour) {
        return new Lesson(new Subject("Mathematics"),
                new WeeklyLessonSlot(day, LocalTime.of(startHour, 0), LocalTime.of(endHour, 0)),
                new HourlyRate(new BigDecimal("40")));
    }
}
