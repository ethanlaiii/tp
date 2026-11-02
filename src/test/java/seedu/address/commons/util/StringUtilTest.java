package seedu.address.commons.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.io.FileNotFoundException;
import java.util.Optional;

import org.junit.jupiter.api.Test;

public class StringUtilTest {

    //---------------- Tests for isNonZeroUnsignedInteger --------------------------------------

    @Test
    public void isNonZeroUnsignedInteger() {

        // EP: empty strings
        assertFalse(StringUtil.isNonZeroUnsignedInteger("")); // Boundary value
        assertFalse(StringUtil.isNonZeroUnsignedInteger("  "));

        // EP: not a number
        assertFalse(StringUtil.isNonZeroUnsignedInteger("a"));
        assertFalse(StringUtil.isNonZeroUnsignedInteger("aaa"));

        // EP: zero
        assertFalse(StringUtil.isNonZeroUnsignedInteger("0"));

        // EP: zero as prefix
        assertTrue(StringUtil.isNonZeroUnsignedInteger("01"));

        // EP: signed numbers
        assertFalse(StringUtil.isNonZeroUnsignedInteger("-1"));
        assertFalse(StringUtil.isNonZeroUnsignedInteger("+1"));

        // EP: numbers with white space
        assertFalse(StringUtil.isNonZeroUnsignedInteger(" 10 ")); // Leading/trailing spaces
        assertFalse(StringUtil.isNonZeroUnsignedInteger("1 0")); // Spaces in the middle

        // EP: number larger than Integer.MAX_VALUE
        assertFalse(StringUtil.isNonZeroUnsignedInteger(Long.toString(Integer.MAX_VALUE + 1)));

        // EP: valid numbers, should return true
        assertTrue(StringUtil.isNonZeroUnsignedInteger("1")); // Boundary value
        assertTrue(StringUtil.isNonZeroUnsignedInteger("10"));
    }


    //---------------- Tests for containsWordIgnoreCase --------------------------------------

    /*
     * Invalid equivalence partitions for word: null, empty, multiple words
     * Invalid equivalence partitions for sentence: null
     * The four test cases below test one invalid input at a time.
     */

    @Test
    public void containsWordIgnoreCase_nullWord_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> StringUtil.containsWordIgnoreCase("typical sentence", null));
    }

    @Test
    public void containsWordIgnoreCase_emptyWord_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, "Word parameter cannot be empty", ()
            -> StringUtil.containsWordIgnoreCase("typical sentence", "  "));
    }

    @Test
    public void containsWordIgnoreCase_multipleWords_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, "Word parameter should be a single word", ()
            -> StringUtil.containsWordIgnoreCase("typical sentence", "aaa BBB"));
    }

    @Test
    public void containsWordIgnoreCase_nullSentence_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> StringUtil.containsWordIgnoreCase(null, "abc"));
    }

    /*
     * Valid equivalence partitions for word:
     *   - any word
     *   - word containing symbols/numbers
     *   - word with leading/trailing spaces
     *
     * Valid equivalence partitions for sentence:
     *   - empty string
     *   - one word
     *   - multiple words
     *   - sentence with extra spaces
     *
     * Possible scenarios returning true:
     *   - matches first word in sentence
     *   - last word in sentence
     *   - middle word in sentence
     *   - matches multiple words
     *
     * Possible scenarios returning false:
     *   - query word matches part of a sentence word
     *   - sentence word matches part of the query word
     *
     * The test method below tries to verify all above with a reasonably low number of test cases.
     */

    @Test
    public void containsWordIgnoreCase_validInputs_correctResult() {

        // Empty sentence
        assertFalse(StringUtil.containsWordIgnoreCase("", "abc")); // Boundary case
        assertFalse(StringUtil.containsWordIgnoreCase("    ", "123"));

        // Matches a partial word only
        assertFalse(StringUtil.containsWordIgnoreCase("aaa bbb ccc", "bb")); // Sentence word bigger than query word
        assertFalse(StringUtil.containsWordIgnoreCase("aaa bbb ccc", "bbbb")); // Query word bigger than sentence word

        // Matches word in the sentence, different upper/lower case letters
        assertTrue(StringUtil.containsWordIgnoreCase("aaa bBb ccc", "Bbb")); // First word (boundary case)
        assertTrue(StringUtil.containsWordIgnoreCase("aaa bBb ccc@1", "CCc@1")); // Last word (boundary case)
        assertTrue(StringUtil.containsWordIgnoreCase("  AAA   bBb   ccc  ", "aaa")); // Sentence has extra spaces
        assertTrue(StringUtil.containsWordIgnoreCase("Aaa", "aaa")); // Only one word in sentence (boundary case)
        assertTrue(StringUtil.containsWordIgnoreCase("aaa bbb ccc", "  ccc  ")); // Leading/trailing spaces

        // Matches multiple words in sentence
        assertTrue(StringUtil.containsWordIgnoreCase("AAA bBb ccc  bbb", "bbB"));
    }

    //---------------- Tests for containsIgnoreCaseAndSpaces ----------------------

    @Test
    public void containsIgnoreCaseAndSpaces_partOfWordInAnyCase_returnsTrue() {
        assertTrue(StringUtil.containsIgnoreCaseAndSpaces("Victoria School", "vic"));
        assertTrue(StringUtil.containsIgnoreCaseAndSpaces("Victoria School", "TORIA SCH"));
    }

    @Test
    public void containsIgnoreCaseAndSpaces_extraSpaces_returnsTrue() {
        assertTrue(StringUtil.containsIgnoreCaseAndSpaces("Victoria   School", "toria sch"));
        assertTrue(StringUtil.containsIgnoreCaseAndSpaces("Victoria School", " toria \t  sch "));
    }

    @Test
    public void containsIgnoreCaseAndSpaces_curlyApostrophe_matchesStraight() {
        assertTrue(StringUtil.containsIgnoreCaseAndSpaces("St. Joseph’s Institution", "joseph's"));
        assertTrue(StringUtil.containsIgnoreCaseAndSpaces("Cedar Girls' School", "girls’"));
    }

    @Test
    public void containsIgnoreCaseAndSpaces_curlyDoubleQuote_matchesStraight() {
        assertTrue(StringUtil.containsIgnoreCaseAndSpaces("revise \"quadratic\" formula", "“quadratic”"));
        assertTrue(StringUtil.containsIgnoreCaseAndSpaces("Blk 5 “Sunrise” Court", "\"sunrise\""));
    }

    @Test
    public void containsIgnoreCaseAndSpaces_longDash_matchesHyphen() {
        assertTrue(StringUtil.containsIgnoreCaseAndSpaces("Blk 1–3 Bedok", "1-3"));
        assertTrue(StringUtil.containsIgnoreCaseAndSpaces("Blk 1-3 Bedok", "1—3"));
    }

    @Test
    public void containsIgnoreCaseAndSpaces_nonBreakingSpaceInText_returnsTrue() {
        assertTrue(StringUtil.containsIgnoreCaseAndSpaces("Bedok\u00A0North", "bedok north"));
    }

    @Test
    public void containsIgnoreCaseAndSpaces_textNotPresent_returnsFalse() {
        assertFalse(StringUtil.containsIgnoreCaseAndSpaces("Victoria School", "raffles"));
        assertFalse(StringUtil.containsIgnoreCaseAndSpaces("Victoria School", "school victoria"));
    }

    @Test
    public void containsIgnoreCaseAndSpaces_nullArgument_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> StringUtil.containsIgnoreCaseAndSpaces(null, "a"));
        assertThrows(NullPointerException.class, () -> StringUtil.containsIgnoreCaseAndSpaces("a", null));
    }

    @Test
    public void containsIgnoreCaseAndSpaces_blankSearchText_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> StringUtil.containsIgnoreCaseAndSpaces("a", ""));
        assertThrows(IllegalArgumentException.class, () -> StringUtil.containsIgnoreCaseAndSpaces("a", "  "));
    }

    //---------------- Tests for normalizeInput --------------------------------------

    @Test
    public void normalizeInput_plainText_returnsSameText() {
        assertEquals("add n/Rajesh Kumar p/91234567", StringUtil.normalizeInput("add n/Rajesh Kumar p/91234567"));
        assertEquals("", StringUtil.normalizeInput(""));
    }

    @Test
    public void normalizeInput_unicodeSpaces_returnsNormalSpaces() {
        // non-breaking, full-width, figure, and narrow non-breaking spaces
        assertEquals("a b c d e", StringUtil.normalizeInput("a b　c d e"));
    }

    @Test
    public void normalizeInput_tabsAndLineBreaks_returnNormalSpaces() {
        // tab, line feed, carriage return, next line, and line separator
        assertEquals("a b c d e f", StringUtil.normalizeInput("a\tb\nc\rd\u0085e f"));
    }

    @Test
    public void normalizeInput_invisibleCharacters_removed() {
        // zero-width space, byte-order mark, left-to-right and right-to-left marks, soft hyphen, word joiner
        assertEquals("Bedok", StringUtil.normalizeInput("﻿Be​d‎‏o­k⁠"));
    }

    @Test
    public void normalizeInput_controlCharacters_removed() {
        assertEquals("abcd", StringUtil.normalizeInput("a\u0000b\u007Fc\u009Fd"));
    }

    @Test
    public void normalizeInput_joiners_kept() {
        String familyEmoji = "👨‍👩‍👧";
        assertEquals(familyEmoji, StringUtil.normalizeInput(familyEmoji));
        assertEquals("a‌b", StringUtil.normalizeInput("a‌b"));
    }

    @Test
    public void normalizeInput_decomposedAccent_composed() {
        assertEquals("école", StringUtil.normalizeInput("école"));

        // a zero-width space between the letter and its accent
        assertEquals("école", StringUtil.normalizeInput("e​́cole"));
    }

    @Test
    public void normalizeInput_curlyApostrophes_returnsStraightApostrophe() {
        assertEquals("Blk 5 'Sunrise' Court", StringUtil.normalizeInput("Blk 5 ‘Sunrise’ Court"));
    }

    @Test
    public void normalizeInput_curlyDoubleQuotes_returnsStraightDoubleQuote() {
        assertEquals("revise \"quadratic\"", StringUtil.normalizeInput("revise “quadratic”"));
    }

    @Test
    public void normalizeInput_enAndEmDashes_returnHyphens() {
        assertEquals("9123-4567", StringUtil.normalizeInput("9123–4567"));
        assertEquals("algebra - weak", StringUtil.normalizeInput("algebra — weak"));
    }

    @Test
    public void normalizeInput_otherDashes_kept() {
        // non-breaking hyphen and minus sign
        assertEquals("a‑b−c", StringUtil.normalizeInput("a‑b−c"));
    }

    @Test
    public void normalizeInput_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> StringUtil.normalizeInput(null));
    }

    //---------------- Tests for findInvalidCharacter --------------------------------------

    @Test
    public void findInvalidCharacter_allAllowed_returnsEmpty() {
        assertEquals(Optional.empty(), StringUtil.findInvalidCharacter("Cafe", "[A-Za-z]"));
        assertEquals(Optional.empty(), StringUtil.findInvalidCharacter("", "[A-Za-z]"));
    }

    @Test
    public void findInvalidCharacter_severalInvalid_returnsFirst() {
        assertEquals(Optional.of("é"), StringUtil.findInvalidCharacter("Café 中文", "[A-Za-z ]"));
    }

    @Test
    public void findInvalidCharacter_emoji_returnsWholeEmoji() {
        String emoji = "😀";
        assertEquals(Optional.of(emoji), StringUtil.findInvalidCharacter("Good " + emoji, "[A-Za-z ]"));
    }

    @Test
    public void findInvalidCharacter_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> StringUtil.findInvalidCharacter(null, "[A-Za-z]"));
        assertThrows(NullPointerException.class, () -> StringUtil.findInvalidCharacter("Cafe", null));
    }

    //---------------- Tests for getDetails --------------------------------------

    /*
     * Equivalence Partitions: null, valid throwable object
     */

    @Test
    public void getDetails_exceptionGiven() {
        assertTrue(StringUtil.getDetails(new FileNotFoundException("file not found"))
            .contains("java.io.FileNotFoundException: file not found"));
    }

    @Test
    public void getDetails_nullGiven_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> StringUtil.getDetails(null));
    }

}
