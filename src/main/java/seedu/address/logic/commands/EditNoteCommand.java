package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NID;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NOTE;

import java.util.ArrayList;
import java.util.List;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Note;
import seedu.address.model.person.Person;

/**
 * Replaces the text of a note, identified by its NID, of the person identified using their displayed index.
 */
public class EditNoteCommand extends Command {

    public static final String COMMAND_WORD = "editnote";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Replaces the text of the note numbered NID of the student at INDEX.\n"
            + "Parameters: INDEX (must be a positive integer) " + PREFIX_NID + "NID (must be a positive integer) "
            + PREFIX_NOTE + "TEXT\n"
            + "Example: " + COMMAND_WORD + " 1 " + PREFIX_NID + "2 " + PREFIX_NOTE + "Improving in algebra";

    public static final String MESSAGE_SUCCESS = "Edited note %1$d of %2$s at %3$s (%4$s).\nOld: %5$s\nNew: %6$s";
    public static final String MESSAGE_NO_NOTES = "%1$s at %2$s (%3$s) has no notes to edit.";
    public static final String MESSAGE_NOTE_UNCHANGED =
            "No changes made: note %1$d of %2$s at %3$s (%4$s) already says: %5$s";

    private final Index targetIndex;
    private final Index targetNid;
    private final Note newNote;

    /**
     * Creates an {@code EditNoteCommand} to replace note {@code targetNid} of the person at
     * {@code targetIndex} with {@code newNote}.
     */
    public EditNoteCommand(Index targetIndex, Index targetNid, Note newNote) {
        requireAllNonNull(targetIndex, targetNid, newNote);
        this.targetIndex = targetIndex;
        this.targetNid = targetNid;
        this.newNote = newNote;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> lastShownList = model.getFilteredPersonList();
        if (targetIndex.getZeroBased() >= lastShownList.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }

        Person person = lastShownList.get(targetIndex.getZeroBased());
        NoteCommandUtil.verifyNoteExists(person, targetNid, MESSAGE_NO_NOTES);
        assert targetNid.getZeroBased() < person.getNotes().size() : "NID was checked by verifyNoteExists";
        Note oldNote = person.getNotes().get(targetNid.getZeroBased());
        if (oldNote.equals(newNote)) {
            return CommandResult.showStudentWarning(String.format(MESSAGE_NOTE_UNCHANGED, targetNid.getOneBased(),
                    person.getName(), person.getSchool(), person.getLevel(), oldNote), person, targetIndex);
        }

        List<Note> notes = new ArrayList<>(person.getNotes());
        notes.set(targetNid.getZeroBased(), newNote);
        Person editedPerson = person.copyWithNotes(notes);

        String successMessage = String.format(MESSAGE_SUCCESS, targetNid.getOneBased(),
                editedPerson.getName(), editedPerson.getSchool(), editedPerson.getLevel(), oldNote, newNote);
        String feedback = NoteCommandUtil.setPersonKeepingIndex(model, person, editedPerson, successMessage);
        return CommandResult.showStudent(feedback, editedPerson, targetIndex);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof EditNoteCommand otherEditNoteCommand)) {
            return false;
        }

        return targetIndex.equals(otherEditNoteCommand.targetIndex)
                && targetNid.equals(otherEditNoteCommand.targetNid)
                && newNote.equals(otherEditNoteCommand.newNote);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("targetIndex", targetIndex)
                .add("targetNid", targetNid)
                .add("newNote", newNote)
                .toString();
    }
}
