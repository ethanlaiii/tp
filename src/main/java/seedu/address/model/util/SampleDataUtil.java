package seedu.address.model.util;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import seedu.address.model.AddressBook;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.person.Address;
import seedu.address.model.person.Band;
import seedu.address.model.person.Email;
import seedu.address.model.person.Guardian;
import seedu.address.model.person.Level;
import seedu.address.model.person.Name;
import seedu.address.model.person.Note;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.School;
import seedu.address.model.person.Subject;
import seedu.address.model.tag.Tag;

/**
 * Contains utility methods for populating {@code AddressBook} with sample data.
 */
public class SampleDataUtil {

    private static final String SAMPLE_TAG = "sample";

    private SampleDataUtil() {}

    /**
     * Returns fictional sample students with contact details, subjects, bands, guardians, and tutoring notes.
     */
    public static Person[] getSamplePersons() {
        return new Person[] {
            new Person(new Name("Chloe Tan"), new Phone("91234567"),
                    new School("Bedok Green Secondary School"),
                    new Address("Blk 123 Bedok North Avenue 2, #04-111"),
                    new Email("chloe.tan@example.com"), Level.S2,
                    List.of(new Subject("Mathematics", Band.G1), new Subject("Science", Band.G2)),
                    getTagSet(SAMPLE_TAG, "exams", "paid"),
                    List.of(new Note("Needs more practice with algebra.")),
                    new Guardian(new Name("Amy Tan"), new Phone("92345678"))),
            new Person(new Name("Marcus Lim"), new Phone("82345678"),
                    new School("Tampines Secondary School"),
                    new Address("Blk 311 Tampines Street 33, #05-212"),
                    new Email("marcus.lim@example.com"), Level.S5,
                    List.of(new Subject("English", Band.G2), new Subject("Mathematics", Band.G2)),
                    getTagSet(SAMPLE_TAG, "unpaid"),
                    List.of(new Note("Review comprehension techniques before the next lesson.")),
                    new Guardian(new Name("Grace Lim"), new Phone("98765432"))),
            new Person(new Name("Aisha Rahman"), new Phone("93456789"),
                    new School("Pasir Ris Secondary School"),
                    new Address("Blk 258 Pasir Ris Street 21, #09-303"), null, Level.S3,
                    List.of(new Subject("Chemistry", Band.G3), new Subject("Mathematics", Band.G3)),
                    getTagSet(SAMPLE_TAG),
                    List.of(new Note("Strong in calculations; revise chemical bonding."))),
            new Person(new Name("Daniel Lee"), null,
                    new School("Victoria School"), new Address("12 Bukit Timah Road"), null, Level.S1,
                    List.of(new Subject("Mathematics", Band.G2), new Subject("Science", Band.G2)),
                    getTagSet(SAMPLE_TAG),
                    List.of(new Note("Practise fractions before the next lesson."))),
            new Person(new Name("Natalie Goh"), new Phone("84567890"),
                    new School("Cedar Girls' Secondary School"),
                    new Address("Blk 107 Toa Payoh Lorong 1, #08-212"),
                    new Email("natalie.goh@example.com"), Level.S4,
                    List.of(new Subject("Physics", Band.G3), new Subject("Chemistry", Band.G3)),
                    getTagSet(SAMPLE_TAG, "online"),
                    List.of(new Note("WA2 Physics: 36/50. Focus on electricity.")),
                    new Guardian(new Name("Michelle Goh"), new Phone("81234567"))),
            new Person(new Name("Ethan Kumar"), new Phone("95678901"),
                    new School("Springfield Secondary School"),
                    new Address("Blk 420 Tampines Street 41, #03-144"), null, Level.S2,
                    List.of(new Subject("Geography", Band.G1), new Subject("English", Band.G2)),
                    getTagSet(SAMPLE_TAG),
                    List.of(new Note("Bring the completed map-reading worksheet.")))
        };
    }

    /**
     * Returns a tag set containing the given strings, in order.
     */
    public static Set<Tag> getTagSet(String... strings) {
        return Arrays.stream(strings)
                .map(Tag::new)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /**
     * Returns an address book containing the sample persons.
     */
    public static ReadOnlyAddressBook getSampleAddressBook() {
        AddressBook sampleAb = new AddressBook();
        for (Person samplePerson : getSamplePersons()) {
            sampleAb.addPerson(samplePerson);
        }
        return sampleAb;
    }

}
