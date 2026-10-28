package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import seedu.address.model.person.SchoolingLevel.Stage;

public class SchoolingLevelTest {

    // Common ways tutors write levels get their school stage's colour.
    @Test
    public void stageOf_recognisedForms() {
        for (String level : new String[] {"P5", "p 6", "Pri 3", "Primary 5", "primary 1 GEP"}) {
            assertEquals(Stage.PRIMARY, SchoolingLevel.stageOf(level), level);
        }
        for (String level : new String[] {"Sec 3", "S4", "Secondary 2", "sec 4 Express", "Sec 1 NA"}) {
            assertEquals(Stage.SECONDARY, SchoolingLevel.stageOf(level), level);
        }
        for (String level : new String[] {"JC1", "jc 2", "J2", "Junior College 1"}) {
            assertEquals(Stage.JC, SchoolingLevel.stageOf(level), level);
        }
    }

    // The level is free text, so anything not clearly a stage must stay neutral rather than risk a wrong colour.
    @Test
    public void stageOf_unclearForms_areOther() {
        for (String level : new String[] {"Poly Y1", "IB Year 1", "P7", "Sec 6", "JC4", "Kindergarten", "Pre-U"}) {
            assertEquals(Stage.OTHER, SchoolingLevel.stageOf(level), level);
        }
    }

    @Test
    public void stage_usesTheLevelText() {
        assertEquals(Stage.SECONDARY, new SchoolingLevel("Sec 3 Express").stage());
        assertEquals(Stage.OTHER, new SchoolingLevel("Poly Y1").stage());
    }

    @Test
    public void number_recognisedForms_isTheYear() {
        assertEquals(5, new SchoolingLevel("P5").number());
        assertEquals(5, new SchoolingLevel("Primary 5").number());
        assertEquals(3, new SchoolingLevel("Sec 3 Express").number());
        assertEquals(2, new SchoolingLevel("JC2").number());
    }

    @Test
    public void number_unclearForms_isMaxValue() {
        assertEquals(Integer.MAX_VALUE, new SchoolingLevel("Poly Y1").number());
        assertEquals(Integer.MAX_VALUE, new SchoolingLevel("IB").number());
    }
}
