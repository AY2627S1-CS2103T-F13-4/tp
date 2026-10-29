package seedu.address.ui.schedule;

import static java.util.Objects.requireNonNull;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.PriorityQueue;
import java.util.function.Predicate;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import seedu.address.model.Model;
import seedu.address.model.person.Lesson;
import seedu.address.model.person.Student;
import seedu.address.model.person.WeeklyLessonSlot;
import seedu.address.ui.UiPart;

/**
 * The weekly calendar: every listed student's lessons, Monday to Sunday of the current week, at their times.
 * Every lesson column has the same width, so a day with overlapping lessons is wider than the others and
 * no lesson is squeezed.
 * Today is shaded, with a line at the current time. Each lesson shows the student and lesson numbers that
 * commands use, e.g. "#3.2".
 */
public class ScheduleGrid extends UiPart<GridPane> {

    private static final String FXML = "ScheduleGrid.fxml";
    private static final int INTERVAL_MINUTE = 60;
    private static final double TIME_ROW_HEIGHT = 60;
    private static final double PIXELS_PER_MINUTE = TIME_ROW_HEIGHT / INTERVAL_MINUTE;
    /** The shortest block, tall enough for one line; shorter lessons are drawn this long. */
    private static final int MIN_BLOCK_MINUTES = 15;
    /** Blocks this long fit the time under the number and student; keep in step with .lesson-card-compact. */
    private static final int TWO_LINE_CARD_MINUTES = 30;
    /** Blocks this long fit all three lines of a lesson card. */
    private static final int FULL_CARD_MINUTES = 60;
    private static final int MINUTES_PER_DAY = 24 * 60;
    /** The hours always shown, so a light week still reads as a working day; lessons outside extend it. */
    private static final LocalTime DAY_START = LocalTime.of(9, 0);
    private static final LocalTime DAY_END = LocalTime.of(20, 0);
    /**
     * Narrowest a lesson column gets, so a block shows a time like "10.00am-11.00am" in full; a week that would
     * need narrower columns scrolls sideways instead.
     */
    private static final double MIN_LESSON_COLUMN_WIDTH = 116;
    /** Gap between a lesson block and the edges of its column. */
    private static final double CARD_SIDE_GAP = 4;
    private static final Duration NOW_LINE_REFRESH = Duration.minutes(1);
    private static final int SECONDS_PER_MINUTE = 60;
    /** Diameter of the dot on the current-time line; keep equal to .schedule-now-dot in LightTheme.css. */
    private static final double NOW_DOT_SIZE = 8;

    private final ObservableList<Student> students;
    private final ObservableValue<Predicate<Lesson>> lessonPredicate;
    private final Clock clock;
    private final ListChangeListener<Student> studentListListener = change -> refreshSchedule();
    private final ChangeListener<Predicate<Lesson>> filterListener = (unused, oldValue, newValue) -> refreshSchedule();
    private final Map<DayOfWeek, ObservableList<ScheduleEntry>> entriesByDay = new EnumMap<>(DayOfWeek.class);
    /** How many columns each day uses, in order; a day with overlapping lessons uses more than one. */
    private final List<Integer> dayColumnCounts = new ArrayList<>();
    /** The first grid column of each day, used to draw today's shading and the current-time line. */
    private final Map<DayOfWeek, Integer> dayStartColumns = new EnumMap<>(DayOfWeek.class);
    /** The current-time marker: a dot on today's left edge and a line across the day. */
    private final StackPane nowLine = new StackPane();
    /** The day headings and the corner above the times; they stay at the top while the hours scroll. */
    private final List<Region> headings = new ArrayList<>();
    private double headingOffset;
    /** The times, their background and the corner; they stay at the left while the days scroll sideways. */
    private final List<Region> timeColumn = new ArrayList<>();
    private double timeColumnOffset;
    private Region timeBackground;
    private final Timeline nowLineUpdater;
    /** The date the headings and today's shading were drawn for; a new day redraws them. */
    private LocalDate renderedDate;
    private LocalTime earliestTime;
    /** The latest minute of the day any block reaches; 1440 is the next midnight. */
    private int latestMinute;
    private int gridStartMinute;
    private int gridEndMinute;
    private boolean disposed;

    @FXML
    private GridPane scheduleGrid;

    /**
     * Creates a {@code ScheduleGrid} for the current week by the system clock.
     */
    public ScheduleGrid(ObservableList<Student> personList) {
        this(personList, new SimpleObjectProperty<>(Model.PREDICATE_SHOW_ALL_LESSONS), Clock.systemDefaultZone());
    }

    /** Creates a calendar that follows the active lesson filter. */
    public ScheduleGrid(ObservableList<Student> personList, ObservableValue<Predicate<Lesson>> lessonPredicate) {
        this(personList, lessonPredicate, Clock.systemDefaultZone());
    }

    /**
     * Creates a {@code ScheduleGrid} with the given students' lessons grouped by day of the week
     * and partitioned into the minimum number of non-overlapping columns, starting from the left.
     * {@code clock} decides which week's dates and which day is today.
     * The component and its source list should be used on the JavaFX application thread.
     */
    public ScheduleGrid(ObservableList<Student> personList, Clock clock) {
        this(personList, new SimpleObjectProperty<>(Model.PREDICATE_SHOW_ALL_LESSONS), clock);
    }

    /** Creates a calendar with an observable lesson filter and a clock for deterministic dates. */
    public ScheduleGrid(ObservableList<Student> personList, ObservableValue<Predicate<Lesson>> lessonPredicate,
            Clock clock) {
        super(FXML);

        students = requireNonNull(personList);
        this.lessonPredicate = requireNonNull(lessonPredicate);
        this.clock = requireNonNull(clock);
        for (DayOfWeek day : DayOfWeek.values()) {
            entriesByDay.put(day, FXCollections.observableArrayList());
        }
        Region nowDot = new Region();
        nowDot.getStyleClass().add("schedule-now-dot");
        // Centre the dot on the day's left edge, as in the mockup.
        nowDot.setTranslateX(-NOW_DOT_SIZE / 2);
        Region nowBar = new Region();
        nowBar.getStyleClass().add("schedule-now-bar");
        nowBar.setMaxWidth(Double.MAX_VALUE);
        // The dot sits on top of the line's start, so the two join.
        nowLine.getChildren().addAll(nowBar, nowDot);
        StackPane.setAlignment(nowDot, Pos.CENTER_LEFT);
        nowLine.getStyleClass().add("schedule-now-line");
        nowLine.setMouseTransparent(true);
        // The marker's middle, not its top, sits on the current time.
        nowLine.setTranslateY(-NOW_DOT_SIZE / 2);

        refreshSchedule();
        students.addListener(studentListListener);
        lessonPredicate.addListener(filterListener);
        nowLineUpdater = new Timeline(new KeyFrame(NOW_LINE_REFRESH, event -> updateForTime()));
        nowLineUpdater.setCycleCount(Animation.INDEFINITE);
        // Start on the next whole minute, so the line moves when the clock's minute changes.
        nowLineUpdater.setDelay(Duration.seconds(SECONDS_PER_MINUTE - LocalTime.now(clock).getSecond()));
        nowLineUpdater.play();
    }

    private void refreshSchedule() {
        // Observable-list listeners run on the modifying thread; render only on the JavaFX thread.
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(this::refreshSchedule);
            return;
        }
        if (disposed) {
            return;
        }

        clearSchedule();
        renderedDate = today();
        registerStudentLessons(students);
        int rowCount = generateRowsOfInterval(INTERVAL_MINUTE);
        Region corner = new Region();
        corner.getStyleClass().add("schedule-day");
        pin(corner);
        pinLeft(corner);
        // Above the day headings, which scroll sideways under it.
        corner.setViewOrder(-2);
        scheduleGrid.add(corner, 0, 0);

        int currDisplayColumn = 1;
        for (DayOfWeek day : DayOfWeek.values()) {
            int dayColumnWidth = assignColumnValues(currDisplayColumn, entriesByDay.get(day));
            dayColumnCounts.add(dayColumnWidth);
            dayStartColumns.put(day, currDisplayColumn);
            addDayLabel(day, currDisplayColumn, dayColumnWidth);
            addDayBackground(day, currDisplayColumn, dayColumnWidth, rowCount);
            currDisplayColumn += dayColumnWidth;
        }
        // Lessons go on top of the day backgrounds and hour lines, so they are added last.
        for (DayOfWeek day : DayOfWeek.values()) {
            for (ScheduleEntry entry : entriesByDay.get(day)) {
                makeLessonCard(entry);
            }
        }
        placeNowLine();
    }

    /**
     * Moves the current-time line, or redraws the whole week once the date has changed, so today's shading,
     * the circled date and the week's dates follow the clock while the app stays open.
     */
    void updateForTime() {
        if (today().equals(renderedDate)) {
            placeNowLine();
        } else {
            refreshSchedule();
        }
    }

    private void clearSchedule() {
        entriesByDay.values().forEach(ObservableList::clear);
        dayColumnCounts.clear();
        dayStartColumns.clear();
        headings.clear();
        timeColumn.clear();
        scheduleGrid.getChildren().clear();

        // Keep the FXML timestamp column and day-heading row; regenerate the remaining constraints.
        scheduleGrid.getColumnConstraints().remove(1, scheduleGrid.getColumnConstraints().size());
        scheduleGrid.getRowConstraints().remove(1, scheduleGrid.getRowConstraints().size());
    }

    /**
     * Stops observing the source list and the clock, and prevents queued refreshes from rebuilding the grid.
     * Call this on the JavaFX application thread when the component is closed or replaced.
     * Calling this more than once is safe.
     */
    public void dispose() {
        disposed = true;
        students.removeListener(studentListListener);
        lessonPredicate.removeListener(filterListener);
        nowLineUpdater.stop();
    }

    private void registerStudentLessons(ObservableList<Student> personList) {
        earliestTime = DAY_START;
        latestMinute = toMinuteOfDay(DAY_END);

        for (int studentIndex = 0; studentIndex < personList.size(); studentIndex++) {
            Student student = personList.get(studentIndex);
            List<Lesson> lessons = student.getLessons();
            for (int lessonIndex = 0; lessonIndex < lessons.size(); lessonIndex++) {
                Lesson lesson = lessons.get(lessonIndex);
                if (!lessonPredicate.getValue().test(lesson)) {
                    continue;
                }
                earliestTime = lesson.getStart().isBefore(earliestTime) ? lesson.getStart() : earliestTime;
                ScheduleEntry entry = new ScheduleEntry(student, lesson, studentIndex + 1, lessonIndex + 1);
                // A lengthened short lesson can reach past its own end, so the grid grows to fit its block.
                latestMinute = Math.max(latestMinute, blockEnd(entry));
                entriesByDay.get(lesson.getDay()).add(entry);
            }
        }
    }

    /**
     * Adds a lesson column. Lesson columns all start at the same width and grow equally into the width left
     * after the time column, so a day with two overlapping lessons has two columns and is twice as wide as a day
     * without overlaps.
     */
    private void addColumn() {
        ColumnConstraints col = new ColumnConstraints();
        col.setMinWidth(MIN_LESSON_COLUMN_WIDTH);
        col.setPrefWidth(MIN_LESSON_COLUMN_WIDTH);
        col.setHgrow(Priority.SOMETIMES);
        scheduleGrid.getColumnConstraints().add(col);
    }

    private void addRow() {
        RowConstraints row = new RowConstraints();
        // A fixed time scale keeps per-card minute offsets accurate when the window is resized.
        row.setMinHeight(TIME_ROW_HEIGHT);
        row.setPrefHeight(TIME_ROW_HEIGHT);
        row.setMaxHeight(TIME_ROW_HEIGHT);
        row.setVgrow(Priority.NEVER);
        scheduleGrid.getRowConstraints().add(row);
    }

    /** Adds a day's heading, e.g. "Mon 12", with today's date marked. */
    private void addDayLabel(DayOfWeek day, int currDisplayColumn, int dayColumnWidth) {
        LocalDate date = dateOf(day);
        Label name = new Label(day.getDisplayName(TextStyle.SHORT_STANDALONE, Locale.ENGLISH));
        name.getStyleClass().add("schedule-day-name");
        Label number = new Label(String.valueOf(date.getDayOfMonth()));
        number.getStyleClass().add("schedule-day-number");
        HBox heading = new HBox(8, name, number);
        heading.setAlignment(Pos.CENTER);
        heading.getStyleClass().add("schedule-day");
        if (date.equals(renderedDate)) {
            heading.getStyleClass().add("schedule-today");
        }
        heading.setMinWidth(0);
        heading.setMaxWidth(Double.MAX_VALUE);
        pin(heading);
        scheduleGrid.add(heading, currDisplayColumn, 0, dayColumnWidth, 1);
    }

    /** Keeps {@code heading} above the lessons and at the current scroll offset. */
    private void pin(Region heading) {
        heading.setViewOrder(-1);
        heading.setTranslateY(headingOffset);
        headings.add(heading);
    }

    /**
     * Moves the day headings down by {@code offset}, the distance the grid is scrolled, so they stay in view.
     */
    public void setHeadingOffset(double offset) {
        headingOffset = offset;
        headings.forEach(heading -> heading.setTranslateY(offset));
    }

    /** Keeps {@code node} of the time column above the lessons and at the current sideways scroll offset. */
    private void pinLeft(Region node) {
        node.setViewOrder(-1);
        node.setTranslateX(timeColumnOffset);
        timeColumn.add(node);
    }

    /**
     * Moves the time column right by {@code offset}, the distance the grid is scrolled sideways, so the times
     * stay in view. Its background shows only while the days scroll under it, so it never hides the left half of
     * the current-time dot on Monday.
     */
    public void setTimeColumnOffset(double offset) {
        timeColumnOffset = offset;
        timeColumn.forEach(node -> node.setTranslateX(offset));
        timeBackground.setVisible(offset > 0);
    }

    /** Draws the line on the left of a day, from the heading down to the last row, and shades today. */
    private void addDayBackground(DayOfWeek day, int currDisplayColumn, int dayColumnWidth, int rowCount) {
        Region background = new Region();
        background.getStyleClass().add("schedule-day-column");
        // Behind the hour lines, so today's shading never hides them.
        background.setViewOrder(1);
        if (dateOf(day).equals(renderedDate)) {
            background.getStyleClass().add("schedule-today-column");
        }
        scheduleGrid.add(background, currDisplayColumn, 1, dayColumnWidth, rowCount);
    }

    /** Returns the date of {@code day} in the week being drawn, which starts on Monday. */
    private LocalDate dateOf(DayOfWeek day) {
        return renderedDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).plusDays(day.ordinal());
    }

    private LocalDate today() {
        return LocalDate.now(clock);
    }

    /** Puts the current-time line across today's columns, or hides it when now is outside the drawn hours. */
    private void placeNowLine() {
        scheduleGrid.getChildren().remove(nowLine);
        LocalDateTime now = LocalDateTime.now(clock);
        int minute = toMinuteOfDay(now.toLocalTime()) - gridStartMinute;
        Integer startColumn = dayStartColumns.get(now.getDayOfWeek());
        if (disposed || startColumn == null || minute < 0 || minute >= gridEndMinute - gridStartMinute) {
            return;
        }
        int row = minute / INTERVAL_MINUTE;
        GridPane.setValignment(nowLine, VPos.TOP);
        GridPane.setMargin(nowLine, new Insets((minute - row * INTERVAL_MINUTE) * PIXELS_PER_MINUTE, 0, 0, 0));
        int dayColumns = dayColumnCounts.get(now.getDayOfWeek().ordinal());
        scheduleGrid.add(nowLine, startColumn, 1 + row, dayColumns, 1);
    }

    /** Returns how far down the grid the current-time line is, or nothing if it is not shown. */
    public OptionalDouble currentTimeOffset() {
        if (!scheduleGrid.getChildren().contains(nowLine)) {
            return OptionalDouble.empty();
        }
        return OptionalDouble.of(nowLine.getBoundsInParent().getMinY());
    }

    /** Returns the number of unique column values assigned */
    private int assignColumnValues(int startColumn, ObservableList<ScheduleEntry> dayEntries) {
        if (dayEntries.isEmpty()) {
            addColumn();
            return 1;
        }

        int currColumn = startColumn;

        dayEntries.sort(null);
        // Lessons share a column only if their drawn blocks do not overlap, so a lengthened short lesson
        // never covers the next one.
        PriorityQueue<ScheduleEntry> activeEntries =
                new PriorityQueue<>(Comparator.comparingInt(ScheduleGrid::blockEnd));
        PriorityQueue<Integer> availableColumns = new PriorityQueue<>();

        for (ScheduleEntry entry : dayEntries) {
            while (!activeEntries.isEmpty() && blockEnd(activeEntries.peek()) <= blockStart(entry)) {
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

    /** Adds a row per interval from the earlier of 9am and the first lesson to the later of 8pm and the last. */
    private int generateRowsOfInterval(int minuteInterval) {
        gridStartMinute = toMinuteOfDay(earliestTime) / minuteInterval * minuteInterval;
        // Keep the rounded end as minutes so 1440 represents the next midnight without wrapping to zero.
        gridEndMinute = (latestMinute + minuteInterval - 1) / minuteInterval * minuteInterval;

        int rowCount = (gridEndMinute - gridStartMinute) / minuteInterval;

        timeBackground = new Region();
        timeBackground.getStyleClass().add("schedule-time-column");
        pinLeft(timeBackground);
        timeBackground.setVisible(timeColumnOffset > 0);
        scheduleGrid.add(timeBackground, 0, 1, 1, rowCount);
        LocalTime currTime = LocalTime.MIDNIGHT.plusMinutes(gridStartMinute);
        for (int i = 0; i < rowCount; i++) {
            addRow();
            Region line = new Region();
            line.getStyleClass().add("schedule-hour-line");
            scheduleGrid.add(line, 0, i + 1, GridPane.REMAINING, 1);
            addHalfHourLine(i + 1, minuteInterval);
            Label timeLabel = new Label(WeeklyLessonSlot.formatTime(currTime));
            timeLabel.getStyleClass().add("schedule-time");
            pinLeft(timeLabel);
            scheduleGrid.add(timeLabel, 0, i + 1, 1, 1);
            GridPane.setValignment(timeLabel, VPos.TOP);
            currTime = currTime.plusMinutes(minuteInterval);
        }
        return rowCount;
    }

    /**
     * Returns the minute of the day where {@code entry}'s block starts: the lesson's start, except that a short
     * lesson just before midnight starts early enough for its block to end by midnight.
     */
    private static int blockStart(ScheduleEntry entry) {
        return Math.min(toMinuteOfDay(entry.getStart()), MINUTES_PER_DAY - MIN_BLOCK_MINUTES);
    }

    /** Returns the minute of the day where {@code entry}'s block ends, at least the shortest block after its start. */
    private static int blockEnd(ScheduleEntry entry) {
        return Math.max(toMinuteOfDay(entry.getEnd()), blockStart(entry) + MIN_BLOCK_MINUTES);
    }

    /** Draws a dotted line halfway down {@code row}, across every day but not the time column. */
    private void addHalfHourLine(int row, int minuteInterval) {
        Region line = new Region();
        line.getStyleClass().add("schedule-half-hour-line");
        line.setMouseTransparent(true);
        GridPane.setValignment(line, VPos.TOP);
        GridPane.setMargin(line, new Insets(minuteInterval / 2 * PIXELS_PER_MINUTE, 0, 0, 0));
        scheduleGrid.add(line, 1, row, GridPane.REMAINING, 1);
    }

    private void makeLessonCard(ScheduleEntry entry) {
        int startMinute = blockStart(entry) - gridStartMinute;
        int endMinute = blockEnd(entry) - gridStartMinute;
        int startRow = startMinute / INTERVAL_MINUTE;
        int endRowExclusive = (endMinute + INTERVAL_MINUTE - 1) / INTERVAL_MINUTE;

        // Span enclosing intervals, then inset to the block's exact times.
        double topMargin = (startMinute - startRow * INTERVAL_MINUTE) * PIXELS_PER_MINUTE;
        double bottomMargin = (endRowExclusive * INTERVAL_MINUTE - endMinute) * PIXELS_PER_MINUTE;

        int blockMinutes = endMinute - startMinute;
        int lineCount = blockMinutes >= FULL_CARD_MINUTES ? 3 : blockMinutes >= TWO_LINE_CARD_MINUTES ? 2 : 1;
        LessonCard lessonCard = new LessonCard(entry, lineCount);
        GridPane.setMargin(lessonCard.getRoot(), new Insets(topMargin, CARD_SIDE_GAP, bottomMargin, CARD_SIDE_GAP));
        scheduleGrid.add(lessonCard.getRoot(), entry.getDisplayColumn(), 1 + startRow, 1, endRowExclusive - startRow);
    }

    private static int toMinuteOfDay(LocalTime time) {
        return time.getHour() * 60 + time.getMinute();
    }
}
