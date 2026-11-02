package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NOTE;

import java.util.ArrayList;
import java.util.List;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Note;
import seedu.address.model.person.Person;

/**
 * Adds a note to the person identified using their displayed index.
 */
public class AddNoteCommand extends Command {

    public static final String COMMAND_WORD = "addnote";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Adds a note to the student at INDEX.\n"
            + "Parameters: INDEX (must be a positive integer) " + PREFIX_NOTE + "TEXT\n"
            + "Example: " + COMMAND_WORD + " 1 " + PREFIX_NOTE + "Weak in algebra";

    public static final String MESSAGE_SUCCESS = "Added note to %1$s at %2$s (%3$s): %4$s";

    private final Index targetIndex;
    private final Note note;

    /**
     * Creates an {@code AddNoteCommand} to add {@code note} to the person at {@code targetIndex}.
     */
    public AddNoteCommand(Index targetIndex, Note note) {
        requireAllNonNull(targetIndex, note);
        this.targetIndex = targetIndex;
        this.note = note;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> lastShownList = model.getFilteredPersonList();
        if (targetIndex.getZeroBased() >= lastShownList.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }

        Person person = lastShownList.get(targetIndex.getZeroBased());
        List<Note> notes = new ArrayList<>(person.getNotes());
        notes.add(note);
        Person editedPerson = person.copyWithNotes(notes);

        model.setPerson(person, editedPerson);
        String successMessage = String.format(MESSAGE_SUCCESS, editedPerson.getName(), editedPerson.getSchool(),
                editedPerson.getLevel(), note);
        return CommandResult.showStudent(successMessage, editedPerson, targetIndex);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof AddNoteCommand otherAddNoteCommand)) {
            return false;
        }

        return targetIndex.equals(otherAddNoteCommand.targetIndex)
                && note.equals(otherAddNoteCommand.note);
    }
}
