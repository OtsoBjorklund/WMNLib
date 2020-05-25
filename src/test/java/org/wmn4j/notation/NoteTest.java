/*
 * Distributed under the MIT license (see LICENSE.txt or https://opensource.org/licenses/MIT).
 */
package org.wmn4j.notation;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class NoteTest {

	@Test
	void testEquals() {
		final Note A1 = Note.of(Pitch.Base.A, 0, 1, Durations.QUARTER);
		final Note A1differentDur = Note.of(Pitch.Base.A, 0, 1, Durations.EIGHTH);
		final Note A1Copy = Note.of(Pitch.Base.A, 0, 1, Durations.QUARTER);
		final Note B1 = Note.of(Pitch.Base.B, 0, 1, Durations.QUARTER);
		final Note Asharp1 = Note.of(Pitch.Base.A, 1, 1, Durations.QUARTER);
		final Note C4 = Note.of(Pitch.of(Pitch.Base.C, 0, 4), Durations.QUARTER);

		assertTrue(A1.equals(A1));
		assertTrue(A1.equals(A1Copy));
		assertTrue(A1Copy.equals(A1));
		assertFalse(A1.equals(A1differentDur));
		assertFalse(A1.equals(B1));
		assertFalse(A1.equals(Asharp1));
		assertTrue(C4.equals(Note.of(Pitch.of(Pitch.Base.C, 0, 4), Durations.QUARTER)));

		final Pitch pitch = Pitch.of(Pitch.Base.C, 0, 1);
		final HashSet<Articulation> articulations = new HashSet<>();
		articulations.add(Articulation.STACCATO);
		final Note note1 = Note.of(pitch, Durations.EIGHTH, articulations);
		articulations.add(Articulation.TENUTO);
		final Note note2 = Note.of(pitch, Durations.EIGHTH, articulations);
		final Note note3 = Note.of(pitch, Durations.EIGHTH, articulations);

		assertFalse(note1.equals(Note.of(pitch, Durations.EIGHTH)));
		assertFalse(note1.equals(note2));
		assertTrue(note2.equals(note2));
		assertTrue(note2.equals(note3));
	}

	@Test
	void testCreatingInvalidNote() {

		try {
			Note.of(Pitch.Base.C, 5, 1, Durations.QUARTER);
			fail("No exception was thrown. Expected: IllegalArgumentException");
		} catch (final Exception e) {
			assertTrue(e instanceof IllegalArgumentException);
		}

		try {
			Note.of(Pitch.Base.C, 0, 11, Durations.QUARTER);
			fail("No exception was thrown. Expected: IllegalArgumentException");
		} catch (final Exception e) {
			assertTrue(e instanceof IllegalArgumentException);
		}

		try {
			Note.of(Pitch.Base.C, 0, 1, null);
			fail("No exception was thrown. Expected: IllegalArgumentException");
		} catch (final Exception e) {
			assertTrue(e instanceof NullPointerException);
		}
	}

	@Test
	void testHasArticulation() {
		final Pitch pitch = Pitch.of(Pitch.Base.C, 0, 1);
		final Set<Articulation> articulations = new HashSet<>();
		articulations.add(Articulation.STACCATO);
		assertTrue(Note.of(pitch, Durations.EIGHTH, articulations).hasArticulation(Articulation.STACCATO));
		assertFalse(Note.of(pitch, Durations.EIGHTH).hasArticulation(Articulation.STACCATO));
	}

	@Test
	void testHasArticulations() {
		final Pitch pitch = Pitch.of(Pitch.Base.C, 0, 1);
		final HashSet<Articulation> articulations = new HashSet<>();
		articulations.add(Articulation.STACCATO);
		assertTrue(Note.of(pitch, Durations.EIGHTH, articulations).hasArticulations());
		assertFalse(Note.of(pitch, Durations.EIGHTH).hasArticulations());
	}

	@Test
	void testGetArticulations() {
		final Pitch pitch = Pitch.of(Pitch.Base.C, 0, 1);
		assertTrue(Note.of(pitch, Durations.EIGHTH).getArticulations().isEmpty());

		final Set<Articulation> articulations = new HashSet<>();
		articulations.add(Articulation.STACCATO);
		articulations.add(Articulation.TENUTO);
		final Note note = Note.of(pitch, Durations.EIGHTH, articulations);

		final Collection<Articulation> artic = note.getArticulations();
		assertEquals(2, artic.size());
		assertTrue(artic.contains(Articulation.STACCATO));
		assertTrue(artic.contains(Articulation.TENUTO));
		try {
			artic.remove(Articulation.STACCATO);
			fail("Removing articulation succeeded, immutability violated");
		} catch (final Exception e) {
			/* Do nothing */
		}

		assertTrue(note.hasArticulation(Articulation.STACCATO));
	}

	@Test
	void testTies() {
		final NoteBuilder firstBuilder = new NoteBuilder(Pitch.of(Pitch.Base.C, 0, 4), Durations.QUARTER);
		final NoteBuilder secondBuilder = new NoteBuilder(Pitch.of(Pitch.Base.C, 0, 4), Durations.EIGHTH);
		firstBuilder.addTieToFollowing(secondBuilder);

		final Note secondNote = secondBuilder.build();
		assertTrue(secondNote.isTied());
		assertTrue(!secondNote.getFollowingTiedNote().isPresent());
		assertTrue(secondNote.isTiedFromPrevious());

		final Note firstNote = firstBuilder.build();
		assertTrue(firstNote.isTied());
		assertTrue(firstNote.getFollowingTiedNote() != null);
		assertFalse(firstNote.isTiedFromPrevious());

	}

	@Test
	void testTiedDuration() {
		final Note untied = Note.of(Pitch.of(Pitch.Base.C, 0, 4), Durations.QUARTER);
		assertEquals(Durations.QUARTER, untied.getTiedDuration());

		final NoteBuilder firstBuilder = new NoteBuilder(Pitch.of(Pitch.Base.C, 0, 4), Durations.QUARTER);
		final NoteBuilder secondBuilder = new NoteBuilder(Pitch.of(Pitch.Base.C, 0, 4), Durations.EIGHTH);
		firstBuilder.addTieToFollowing(secondBuilder);

		Note firstNote = firstBuilder.build();
		Note secondNote = secondBuilder.build();

		assertEquals(Durations.QUARTER.addDot(), firstNote.getTiedDuration());
		assertEquals(Durations.EIGHTH, secondNote.getTiedDuration());

		firstBuilder.clearCache();
		secondBuilder.clearCache();

		final NoteBuilder thirdBuilder = new NoteBuilder(Pitch.of(Pitch.Base.C, 0, 4), Durations.EIGHTH);
		secondBuilder.addTieToFollowing(thirdBuilder);

		firstNote = firstBuilder.build();
		secondNote = secondBuilder.build();
		final Note thirdNote = thirdBuilder.build();

		assertEquals(Durations.HALF, firstNote.getTiedDuration());
		assertEquals(Durations.QUARTER, secondNote.getTiedDuration());
		assertEquals(Durations.EIGHTH, thirdNote.getTiedDuration());
	}

	@Test
	void testBeginsAndEndsNotation() {
		final Note followingNote = Note.of(Pitch.of(Pitch.Base.C, 0, 4), Durations.EIGHTH);

		Notation.Connection slurBeginning = Notation.Connection
				.beginningOf(Notation.of(Notation.Type.SLUR), followingNote);
		Notation.Connection glissandoEnd = Notation.Connection.endOf(Notation.of(Notation.Type.GLISSANDO));
		List<Notation.Connection> notationConnections = new ArrayList<>();
		notationConnections.add(slurBeginning);
		notationConnections.add(glissandoEnd);

		final Note noteWithNotationConnections = Note.of(Pitch.of(Pitch.Base.C, 0, 4), Durations.EIGHTH,
				Collections.emptySet(), notationConnections);

		assertTrue(noteWithNotationConnections.beginsNotation(Notation.Type.SLUR));
		assertTrue(noteWithNotationConnections.endsNotation(Notation.Type.GLISSANDO));

		assertFalse(noteWithNotationConnections.beginsNotation(Notation.Type.GLISSANDO));
		assertFalse(noteWithNotationConnections.endsNotation(Notation.Type.SLUR));
	}

	@Test
	void testHasNotationConnection() {
		final Note followingNote = Note.of(Pitch.of(Pitch.Base.C, 0, 4), Durations.EIGHTH);
		Notation.Connection slurBeginning = Notation.Connection
				.beginningOf(Notation.of(Notation.Type.SLUR), followingNote);
		List<Notation.Connection> notationConnections = new ArrayList<>();
		notationConnections.add(slurBeginning);

		final Note noteThatBeginsSlur = Note.of(Pitch.of(Pitch.Base.C, 0, 4), Durations.EIGHTH,
				Collections.emptySet(), notationConnections);

		assertTrue(noteThatBeginsSlur.hasNotations());
		assertFalse(followingNote.hasNotations());
		assertTrue(noteThatBeginsSlur.hasNotation(Notation.Type.SLUR));
		assertFalse(noteThatBeginsSlur.hasNotation(Notation.Type.GLISSANDO));
	}

	@Test
	void testEqualsAndHashCodeWithNotations() {
		final Note followingNote = Note.of(Pitch.of(Pitch.Base.C, 0, 4), Durations.EIGHTH);

		Notation.Connection slurBeginning = Notation.Connection
				.beginningOf(Notation.of(Notation.Type.SLUR), followingNote);
		Notation.Connection glissandoEnd = Notation.Connection.endOf(Notation.of(Notation.Type.GLISSANDO));
		List<Notation.Connection> notationConnections = new ArrayList<>();

		notationConnections.add(slurBeginning);
		final Note noteThatBeginsSlur = Note.of(Pitch.of(Pitch.Base.C, 0, 4), Durations.EIGHTH,
				Collections.emptySet(), notationConnections);

		Notation.Connection slurEnd = Notation.Connection.endOf(Notation.of(Notation.Type.SLUR));
		List<Notation.Connection> slurEndList = new ArrayList<>();
		slurEndList.add(slurEnd);
		final Note noteThatEndsSlur = Note.of(Pitch.of(Pitch.Base.C, 0, 4), Durations.EIGHTH,
				Collections.emptySet(), slurEndList);

		notationConnections.add(glissandoEnd);
		final Note noteWithNotationConnections = Note.of(Pitch.of(Pitch.Base.C, 0, 4), Durations.EIGHTH,
				Collections.emptySet(), notationConnections);

		assertEquals(noteThatBeginsSlur, noteThatEndsSlur);
		assertEquals(noteThatBeginsSlur.hashCode(), noteThatEndsSlur.hashCode());

		assertFalse(noteThatBeginsSlur.equals(followingNote));
		assertFalse(noteWithNotationConnections.equals(followingNote));
		assertFalse(noteWithNotationConnections.equals(noteThatBeginsSlur));
	}

	@Test
	void testGetNotationConnections() {
		final Note followingNote = Note.of(Pitch.of(Pitch.Base.C, 0, 4), Durations.EIGHTH);

		Notation.Connection slurBeginning = Notation.Connection
				.beginningOf(Notation.of(Notation.Type.SLUR), followingNote);
		Notation.Connection glissandoEnd = Notation.Connection.endOf(Notation.of(Notation.Type.GLISSANDO));
		List<Notation.Connection> notationConnections = new ArrayList<>();
		notationConnections.add(slurBeginning);
		notationConnections.add(glissandoEnd);

		final Note noteWithNotationConnections = Note.of(Pitch.of(Pitch.Base.C, 0, 4), Durations.EIGHTH,
				Collections.emptySet(), notationConnections);

		final Collection<Notation> notationsInNote = noteWithNotationConnections.getNotations();
		assertEquals(2, notationsInNote.size());
		assertTrue(notationsInNote.contains(slurBeginning.getNotation()));
		assertTrue(notationsInNote.contains(glissandoEnd.getNotation()));

		try {
			notationsInNote.add(Notation.of(Notation.Type.GLISSANDO));
			fail("No exception was thrown when trying to add to notations");
		} catch (Exception e) {
			/* Do nothing */
		}

		assertEquals(2, noteWithNotationConnections.getNotations().size());
	}

	@Test
	void testGetNotationConnection() {
		final Note followingNote = Note.of(Pitch.of(Pitch.Base.C, 0, 4), Durations.EIGHTH);
		Notation.Connection slurBeginning = Notation.Connection
				.beginningOf(Notation.of(Notation.Type.SLUR), followingNote);
		List<Notation.Connection> notationConnections = new ArrayList<>();
		notationConnections.add(slurBeginning);

		final Note noteThatBeginsSlur = Note.of(Pitch.of(Pitch.Base.C, 0, 4), Durations.EIGHTH,
				Collections.emptySet(), notationConnections);

		Optional<Notation.Connection> slurBeginningOptional = noteThatBeginsSlur
				.getConnection(slurBeginning.getNotation());
		assertTrue(slurBeginningOptional.isPresent());
		assertEquals(slurBeginning, slurBeginningOptional.get());

		assertFalse(noteThatBeginsSlur.getConnection(Notation.of(Notation.Type.GLISSANDO)).isPresent());
	}
}
