package seedu.address.model.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.person.Guardian;
import seedu.address.model.person.Lesson;
import seedu.address.model.person.LessonMode;
import seedu.address.model.person.SchoolingLevel;
import seedu.address.model.person.Student;
import seedu.address.model.tag.Tag;

public class SampleDataUtilTest {

    @Test
    public void getSampleAddressBook_containsAllSamplePersons() {
        ReadOnlyAddressBook addressBook = SampleDataUtil.getSampleAddressBook();
        List<Student> active = Arrays.stream(SampleDataUtil.getSamplePersons())
                .filter(student -> !student.isArchived()).toList();
        List<Student> archived = Arrays.stream(SampleDataUtil.getSamplePersons())
                .filter(Student::isArchived).toList();

        assertEquals(active, addressBook.getPersonList());
        assertEquals(archived, addressBook.getArchivedPersonList());
    }

    @Test
    public void getTagSet_returnsTagsWithGivenNames() {
        assertEquals(2, SampleDataUtil.getTagSet("a", "b").size());
        assertTrue(SampleDataUtil.getTagSet("a", "b").containsAll(Arrays.asList(new Tag("a"), new Tag("b"))));
    }

    @Test
    public void getSamplePersons_coversEveryLevelGroup() {
        Set<SchoolingLevel.Stage> stages = Arrays.stream(SampleDataUtil.getSamplePersons())
                .map(Student::getStage)
                .collect(Collectors.toSet());

        assertEquals(EnumSet.allOf(SchoolingLevel.Stage.class), stages);
    }

    @Test
    public void getSamplePersons_schoolStudentsHaveSchoolsAndOnlyPastStudentArchived() {
        for (Student student : SampleDataUtil.getSamplePersons()) {
            assertEquals(student.getSchoolingLevel().isPresent(), student.getSchool().isPresent());
            // Current students have lessons; the one archived past student has given up their slot.
            assertEquals(student.isArchived(), student.getLessons().isEmpty());
        }
        assertEquals(1, Arrays.stream(SampleDataUtil.getSamplePersons()).filter(Student::isArchived).count());
    }

    @Test
    public void getSamplePersons_coversNotesFollowUpsAndMissingGuardian() {
        List<Student> current = Arrays.stream(SampleDataUtil.getSamplePersons())
                .filter(student -> !student.isArchived()).toList();

        assertTrue(current.stream().anyMatch(student -> !student.getNotes().isEmpty()));
        assertTrue(current.stream().anyMatch(student -> !student.getFollowUps().isEmpty()));
        assertTrue(current.stream().anyMatch(student -> student.getGuardian().isEmpty()));
    }

    @Test
    public void getSamplePersons_mixesPhysicalAndVirtualLessons() {
        Set<Optional<LessonMode>> modes = Arrays.stream(SampleDataUtil.getSamplePersons())
                .flatMap(student -> student.getLessons().stream())
                .map(Lesson::getMode)
                .collect(Collectors.toSet());

        assertEquals(Set.of(Optional.of(LessonMode.PHYSICAL), Optional.of(LessonMode.VIRTUAL)), modes);
    }

    @Test
    public void getSamplePersons_containsNoClashes() {
        List<Lesson> lessons = Arrays.stream(SampleDataUtil.getSamplePersons())
                .flatMap(student -> student.getLessons().stream())
                .toList();
        int clashes = 0;
        for (int i = 0; i < lessons.size(); i++) {
            for (int j = i + 1; j < lessons.size(); j++) {
                Lesson a = lessons.get(i);
                Lesson b = lessons.get(j);
                if (a.getDay() == b.getDay() && a.getStart().isBefore(b.getEnd())
                        && b.getStart().isBefore(a.getEnd())) {
                    clashes++;
                }
            }
        }

        assertEquals(0, clashes);
    }

    @Test
    public void getSamplePersons_hasSiblingsSharingGuardian() {
        List<Guardian> guardians = new ArrayList<>();
        Arrays.stream(SampleDataUtil.getSamplePersons())
                .forEach(student -> student.getGuardian().ifPresent(guardians::add));

        assertEquals(guardians.size() - 1, Set.copyOf(guardians).size());
    }
}
