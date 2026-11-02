package seedu.address.model.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import seedu.address.model.person.Person;
import seedu.address.model.tag.Tag;

public class SampleDataUtilTest {

    @Test
    public void getSampleAddressBook_validSchools_containsSamplePersons() {
        Person[] persons = SampleDataUtil.getSamplePersons();
        assertEquals(6, persons.length);
        assertEquals(Arrays.asList(persons), SampleDataUtil.getSampleAddressBook().getPersonList());
    }

    @Test
    public void getSamplePersons_allPersons_haveSampleTag() {
        Tag sampleTag = new Tag("sample");
        for (Person person : SampleDataUtil.getSamplePersons()) {
            assertTrue(person.getTags().contains(sampleTag), person.getName() + " lacks the sample tag");
        }
    }
}
