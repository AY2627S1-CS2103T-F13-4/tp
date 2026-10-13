package seedu.address.ui.schedule;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import java.util.PriorityQueue;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import seedu.address.model.person.Lesson;
import seedu.address.model.person.Student;
import seedu.address.ui.UiPart;

/**
 * Grid containing lessons grouped by day of the week.
 */
public class ScheduleGrid extends UiPart<GridPane> {

    private static final String FXML = "ScheduleGrid.fxml";
    private static final int INTERVAL_MINUTE = 30;

    private Map<DayOfWeek, ObservableList<ScheduleEntry>> entriesByDay = new EnumMap<>(DayOfWeek.class);
    private LocalTime earliestTime;
    private LocalTime latestTime;

    @FXML
    private GridPane scheduleGrid;

    /**
     * Creates a {@code ScheduleGrid} with the given students' lessons grouped by day of the week
     * and partitioned into the minimum number of non-overlapping columns, starting from the left.
     */
    public ScheduleGrid(ObservableList<Student> personList) {
        super(FXML);

        for (DayOfWeek day : DayOfWeek.values()) {
            entriesByDay.put(day, FXCollections.observableArrayList());
        }

        registerStudentLessons(personList);
        generateRowsOfInterval(INTERVAL_MINUTE);

        int currDisplayColumn = 1;

        for (DayOfWeek day : DayOfWeek.values()) {
            int dayColumnWidth = assignColumnValues(currDisplayColumn, entriesByDay.get(day));
            addDayLabel(day, currDisplayColumn, dayColumnWidth);
            currDisplayColumn += dayColumnWidth;

            for (ScheduleEntry entry : entriesByDay.get(day)) {
                makeLessonCard(entry);
            }
        }
    }

    private void registerStudentLessons(ObservableList<Student> personList) {
        earliestTime = LocalTime.MAX;
        latestTime = LocalTime.MIN;

        for (Student student : personList) {
            for (Lesson lesson : student.getLessons()) {
                earliestTime = lesson.getStart().isBefore(earliestTime) ? lesson.getStart() : earliestTime;
                latestTime = lesson.getEnd().isAfter(latestTime) ? lesson.getEnd() : latestTime;
                entriesByDay.get(lesson.getDay()).add(new ScheduleEntry(student, lesson));
            }
        }
    }

    private void addColumn() {
        ColumnConstraints col = new ColumnConstraints();
        col.setMinWidth(100);
        col.setPrefWidth(100);
        col.setHgrow(Priority.SOMETIMES);
        scheduleGrid.getColumnConstraints().add(col);
    }

    private void addRow() {
        RowConstraints row = new RowConstraints();
        row.setMinHeight(60);
        row.setVgrow(Priority.SOMETIMES);
        scheduleGrid.getRowConstraints().add(row);
    }

    private void addDayLabel(DayOfWeek day, int currDisplayColumn, int dayColumnWidth) {
        Label dayLabel = new Label(day.getDisplayName(TextStyle.SHORT_STANDALONE, new Locale("EN", "SG")));
        dayLabel.setAlignment(Pos.CENTER);
        dayLabel.getStyleClass().add("background");
        dayLabel.setMaxHeight(Double.MAX_VALUE);
        dayLabel.setMaxWidth(Double.MAX_VALUE);
        GridPane.setMargin(dayLabel, new Insets(1, 1, 1, 1));
        scheduleGrid.add(dayLabel, currDisplayColumn, 0, dayColumnWidth, 1);
    }

    /**Returns the number of unique column values assigned*/
    private int assignColumnValues(int startColumn, ObservableList<ScheduleEntry> dayEntries) {
        if (dayEntries.isEmpty()) {
            addColumn();
            return 1;
        }

        int currColumn = startColumn;

        dayEntries.sort(null);
        PriorityQueue<ScheduleEntry> activeEntries = new PriorityQueue<>();
        PriorityQueue<Integer> availableColumns = new PriorityQueue<>();

        for (ScheduleEntry entry : dayEntries) {
            while (!activeEntries.isEmpty() && activeEntries.peek().getEnd().isBefore(entry.getStart())) {
                availableColumns.add(activeEntries.remove().getDisplayColumn());
            }
            int column;
            if (availableColumns.isEmpty()) {
                addColumn();
                column = currColumn++;
            } else {
                column = availableColumns.remove();
            }
            entry.setDisplayColumn(column);
            activeEntries.add(entry);
        }

        return currColumn - startColumn;
    }

    private void generateRowsOfInterval(int minuteInterval) {
        LocalTime startTime = earliestTime.minusMinutes(earliestTime.getMinute() % minuteInterval);
        LocalTime endTime = latestTime.plusMinutes((minuteInterval - latestTime.getMinute()) % minuteInterval);

        int rowCount = (int) ChronoUnit.MINUTES.between(startTime, endTime) / minuteInterval;

        LocalTime currTime = startTime;
        for (int i = 0; i < rowCount; i++) {
            addRow();
            Label timeLabel = new Label(currTime.toString());
            scheduleGrid.add(timeLabel, 0, i + 1, 1, 1);
            GridPane.setValignment(timeLabel, VPos.TOP);
            currTime = currTime.plusMinutes(minuteInterval);
        }
    }

    private void makeLessonCard(ScheduleEntry entry) {
        int start = (int) ChronoUnit.MINUTES.between(earliestTime, entry.getStart()) / INTERVAL_MINUTE;
        int end = (int) ChronoUnit.MINUTES.between(earliestTime, entry.getEnd()) / INTERVAL_MINUTE;

        LessonCard lessonCard = new LessonCard(entry);
        GridPane.setMargin(lessonCard.getRoot(), new Insets(10, 10, 10, 10));
        scheduleGrid.add(lessonCard.getRoot(), entry.getDisplayColumn(), 1 + start, 1, end - start);
    }
}
