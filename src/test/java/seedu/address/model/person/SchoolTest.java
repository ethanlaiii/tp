package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class SchoolTest {

    @Test
    public void constructor_invalidSchool_throwsException() {
        assertThrows(NullPointerException.class, () -> new School(null));
        assertThrows(IllegalArgumentException.class, School.MESSAGE_CONSTRAINTS, () -> new School(""));
    }

    @Test
    public void isValidSchool_validAndInvalidValues_returnsExpectedResult() {
        assertFalse(School.isValidSchool(""));
        assertFalse(School.isValidSchool(" "));
        assertFalse(School.isValidSchool("---"));
        assertFalse(School.isValidSchool("School/Name"));
        assertFalse(School.isValidSchool("School #1"));
        assertFalse(School.isValidSchool("School_Name"));
        assertFalse(School.isValidSchool("School １２３"));
        assertFalse(School.isValidSchool("123")); // no ASCII letter
        assertFalse(School.isValidSchool("École Française"));
        assertFalse(School.isValidSchool("李明学校"));
        assertFalse(School.isValidSchool("a".repeat(101)));
        assertTrue(School.isValidSchool("Unknown"));
        assertTrue(School.isValidSchool("St Josephs Institution"));
        assertTrue(School.isValidSchool("Anglo Chinese School"));
        assertTrue(School.isValidSchool("Anglo-Chinese School (Independent)"));
        assertTrue(School.isValidSchool("St. Joseph's Institution"));
        assertTrue(School.isValidSchool("Anglo-Chinese School"));
        assertTrue(School.isValidSchool("School of Science and Technology, Singapore"));
        assertTrue(School.isValidSchool("Cedar Girls’ Secondary School"));
        assertTrue(School.isValidSchool("School123"));
        assertTrue(School.isValidSchool("a".repeat(100)));
    }

    @Test
    public void getInvalidReason_invalidSchool_returnsSpecificReason() {
        assertEquals(School.MESSAGE_BLANK, School.getInvalidReason("   ").orElseThrow());
        assertEquals(String.format(School.MESSAGE_TOO_LONG, 101),
                School.getInvalidReason("a".repeat(101)).orElseThrow());
        assertEquals(String.format(School.MESSAGE_INVALID_CHARACTER, "School#1", "#"),
                School.getInvalidReason("School#1").orElseThrow());
        assertEquals(String.format(School.MESSAGE_INVALID_CHARACTER, "École Française", "É"),
                School.getInvalidReason("École Française").orElseThrow());
        assertEquals(String.format(School.MESSAGE_MISSING_ASCII_LETTER, "123"),
                School.getInvalidReason("123").orElseThrow());
        assertTrue(School.getInvalidReason("Anglo-Chinese School (Independent)").isEmpty());
    }

    @Test
    public void equals_caseAndSpacingVariants_returnsTrue() {
        School school = new School("Bedok Green Secondary School");
        School variant = new School("bedok  green secondary school");
        assertEquals(school, variant);
        assertEquals(school.hashCode(), variant.hashCode());
        assertEquals("Bedok Green Secondary School", school.toString());
        assertFalse(school.equals(null));
        assertFalse(school.equals("Bedok Green Secondary School"));
        assertFalse(school.equals(new School("Victoria School")));
    }

    @Test
    public void equals_curlyAndStraightApostrophe_returnsTrue() {
        School curly = new School("St. Joseph’s Institution");
        School straight = new School("St. Joseph's Institution");
        assertEquals(curly, straight);
        assertEquals(curly.hashCode(), straight.hashCode());
        assertEquals("St. Joseph’s Institution", curly.toString());
    }
}
