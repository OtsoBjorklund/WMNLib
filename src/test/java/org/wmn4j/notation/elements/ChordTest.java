/*
 * Copyright 2018 Otso Björklund.
 * Distributed under the MIT license (see LICENSE.txt or https://opensource.org/licenses/MIT).
 */
package org.wmn4j.notation.elements;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;
import org.wmn4j.notation.elements.Articulation;
import org.wmn4j.notation.elements.Chord;
import org.wmn4j.notation.elements.Duration;
import org.wmn4j.notation.elements.Durations;
import org.wmn4j.notation.elements.Note;
import org.wmn4j.notation.elements.Pitch;

/**
 *
 * @author Otso Björklund
 */
public class ChordTest {

	List<Note> cMajorNotes;
	Chord cMajor;
	Chord dMajor;
	Chord fMinor;
	Chord dMinorMaj9;

	public ChordTest() {
		this.cMajorNotes = new ArrayList<>();
		this.cMajorNotes.add(Note.getNote(Pitch.getPitch(Pitch.Base.C, 0, 4), Durations.QUARTER));
		this.cMajorNotes.add(Note.getNote(Pitch.getPitch(Pitch.Base.E, 0, 4), Durations.QUARTER));
		this.cMajorNotes.add(Note.getNote(Pitch.getPitch(Pitch.Base.G, 0, 4), Durations.QUARTER));
		this.cMajor = Chord.getChord(this.cMajorNotes);

		List<Note> dMajorNotes = new ArrayList<>();
		dMajorNotes.add(Note.getNote(Pitch.getPitch(Pitch.Base.D, 0, 3), Durations.QUARTER));
		dMajorNotes.add(Note.getNote(Pitch.getPitch(Pitch.Base.F, 1, 3), Durations.QUARTER));
		dMajorNotes.add(Note.getNote(Pitch.getPitch(Pitch.Base.A, 0, 3), Durations.QUARTER));
		this.dMajor = Chord.getChord(dMajorNotes);

		List<Note> fMinorNotes = new ArrayList<>();
		fMinorNotes.add(Note.getNote(Pitch.getPitch(Pitch.Base.F, 0, 4), Durations.QUARTER));
		fMinorNotes.add(Note.getNote(Pitch.getPitch(Pitch.Base.A, -1, 4), Durations.QUARTER));
		fMinorNotes.add(Note.getNote(Pitch.getPitch(Pitch.Base.C, 0, 5), Durations.QUARTER));
		this.fMinor = Chord.getChord(fMinorNotes);

		ArrayList<Note> DminorMaj9Notes = new ArrayList<>();
		DminorMaj9Notes.add(Note.getNote(Pitch.getPitch(Pitch.Base.D, 0, 4), Durations.EIGHT));
		DminorMaj9Notes.add(Note.getNote(Pitch.getPitch(Pitch.Base.F, 0, 4), Durations.EIGHT));
		DminorMaj9Notes.add(Note.getNote(Pitch.getPitch(Pitch.Base.A, 0, 4), Durations.EIGHT));
		DminorMaj9Notes.add(Note.getNote(Pitch.getPitch(Pitch.Base.C, 1, 5), Durations.EIGHT));
		DminorMaj9Notes.add(Note.getNote(Pitch.getPitch(Pitch.Base.E, 0, 5), Durations.EIGHT));
		this.dMinorMaj9 = Chord.getChord(DminorMaj9Notes);
	}

	@Test
	public void testChordImmutable() {

		List<Note> notes = new ArrayList<>(this.cMajorNotes);
		Chord cMaj = Chord.getChord(notes);
		assertEquals(this.cMajor, cMaj);

		// Test that modifying the list of notes used to create the chord does not
		// modify the chord.
		notes.add(Note.getNote(Pitch.getPitch(Pitch.Base.D, 0, 4), Durations.EIGHT));
		assertEquals(3, cMaj.getNoteCount());
		assertEquals(this.cMajor, cMaj);
	}

	@Test
	public void testGetNoteOrderCorrect() {
		for (int i = 0; i < cMajor.getNoteCount(); ++i) {
			if (i != 0) {
				assertFalse(cMajor.getNote(i - 1).getPitch().higherThan(cMajor.getNote(i).getPitch()));
			}
		}
	}

	@Test
	public void testVarargsFactory() {
		Note C4 = Note.getNote(Pitch.getPitch(Pitch.Base.C, 0, 4), Durations.EIGHT);
		Note E4 = Note.getNote(Pitch.getPitch(Pitch.Base.E, 0, 4), Durations.EIGHT);
		Chord dyad = Chord.getChord(C4, E4);
		assertEquals(2, dyad.getNoteCount());
		assertTrue(dyad.contains(E4));
		assertTrue(dyad.contains(C4));
	}

	@Test
	public void testGetNoteDurationsSameForAllNotes() {
		Duration duration = this.dMinorMaj9.getDuration();
		for (int i = 0; i < this.dMinorMaj9.getNoteCount(); ++i) {
			assertTrue(duration.equals(this.dMinorMaj9.getNote(i).getDuration()));
		}

		try {
			Note a = Note.getNote(Pitch.getPitch(Pitch.Base.A, 0, 3), Durations.EIGHT);
			Note b = Note.getNote(Pitch.getPitch(Pitch.Base.A, 0, 3), Durations.QUARTER);
			ArrayList<Note> notes = new ArrayList<>();
			notes.add(a);
			notes.add(b);
			Chord c = Chord.getChord(notes);
			fail("Failed to throw IllegalArgumentException when "
					+ "creating chord with notes whose durations are not the same");
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}
	}

	@Test
	public void testContainsPitch() {
		assertTrue(this.cMajor.contains(Pitch.getPitch(Pitch.Base.C, 0, 4)));
		assertFalse(this.cMajor.contains(Pitch.getPitch(Pitch.Base.C, 1, 4)));
	}

	@Test
	public void testContainstNote() {
		assertTrue(this.cMajor.contains(Note.getNote(Pitch.getPitch(Pitch.Base.C, 0, 4), Durations.QUARTER)));
		assertFalse(this.cMajor.contains(Note.getNote(Pitch.getPitch(Pitch.Base.C, -1, 4), Durations.QUARTER)));
		HashSet<Articulation> articulations = new HashSet<>();
		articulations.add(Articulation.STACCATO);
		Note staccatoC5 = Note.getNote(Pitch.getPitch(Pitch.Base.C, 0, 5), Durations.QUARTER, articulations);
		Chord staccatoC = this.cMajor.addNote(staccatoC5);
		assertTrue(staccatoC.contains(staccatoC5));
		assertFalse(staccatoC.contains(Note.getNote(Pitch.getPitch(Pitch.Base.C, 0, 5), Durations.QUARTER)));
		assertFalse(this.cMajor.contains(staccatoC5));
	}

	@Test
	public void testGetDuration() {
		assertFalse(this.cMajor.getDuration().equals(Durations.SIXTEENTH));
		assertTrue(this.dMajor.getDuration().equals(Durations.QUARTER));
	}

	@Test
	public void testGetNote() {
		assertTrue(this.cMajor.getNote(1).equals(Note.getNote(Pitch.getPitch(Pitch.Base.E, 0, 4), Durations.QUARTER)));
		assertTrue(this.dMajor.getNote(2).equals(Note.getNote(Pitch.getPitch(Pitch.Base.A, 0, 3), Durations.QUARTER)));
	}

	@Test
	public void testGetLowestNote() {
		assertTrue(this.cMajor.getLowestNote()
				.equals(Note.getNote(Pitch.getPitch(Pitch.Base.C, 0, 4), Durations.QUARTER)));
		assertTrue(this.fMinor.getLowestNote()
				.equals(Note.getNote(Pitch.getPitch(Pitch.Base.F, 0, 4), Durations.QUARTER)));
	}

	@Test
	public void testGetHighestNote() {
		assertTrue(this.cMajor.getHighestNote()
				.equals(Note.getNote(Pitch.getPitch(Pitch.Base.G, 0, 4), Durations.QUARTER)));
		assertTrue(this.fMinor.getHighestNote()
				.equals(Note.getNote(Pitch.getPitch(Pitch.Base.C, 0, 5), Durations.QUARTER)));
	}

	@Test
	public void testGetNoteCount() {
		assertEquals(3, this.cMajor.getNoteCount());
		assertEquals(5, this.dMinorMaj9.getNoteCount());
	}

	@Test
	public void testEquals() {
		ArrayList<Note> cMajorNotes = new ArrayList<>();
		cMajorNotes.add(Note.getNote(Pitch.getPitch(Pitch.Base.C, 0, 4), Durations.QUARTER));
		cMajorNotes.add(Note.getNote(Pitch.getPitch(Pitch.Base.E, 0, 4), Durations.QUARTER));
		cMajorNotes.add(Note.getNote(Pitch.getPitch(Pitch.Base.G, 0, 4), Durations.QUARTER));
		Chord cMaj = Chord.getChord(cMajorNotes);
		assertTrue(this.cMajor.equals(cMaj));
		assertTrue(cMaj.equals(this.cMajor));
		assertTrue(this.cMajor.equals(this.cMajor));
		assertFalse(this.cMajor.equals(this.fMinor));
	}

	@Test
	public void testToString() {
		assertEquals("[C4(1/4),E4(1/4),G4(1/4)]", this.cMajor.toString());
		assertEquals("[D3(1/4),F#3(1/4),A3(1/4)]", this.dMajor.toString());
	}

	@Test
	public void testAddNote() {
		Chord cMaj = this.cMajor.addNote(Note.getNote(Pitch.getPitch(Pitch.Base.C, 0, 5), Durations.QUARTER));
		assertFalse(this.cMajor.equals(cMaj));
		assertEquals(4, cMaj.getNoteCount());
		assertEquals(Note.getNote(Pitch.getPitch(Pitch.Base.C, 0, 5), Durations.QUARTER), cMaj.getHighestNote());

		try {
			Chord illegalCMaj = this.cMajor.addNote(Note.getNote(Pitch.getPitch(Pitch.Base.C, 0, 5), Durations.EIGHT));
			fail("Failed to throw expected exception");
		} catch (IllegalArgumentException e) {
			assertTrue(e instanceof IllegalArgumentException);
		}
	}

	@Test
	public void testRemoveNote() {
		Chord D_FSharp = this.dMajor.removeNote(Note.getNote(Pitch.getPitch(Pitch.Base.A, 0, 3), Durations.QUARTER));
		assertFalse(this.dMajor.equals(D_FSharp));
		assertEquals(2, D_FSharp.getNoteCount());
		assertEquals(Note.getNote(Pitch.getPitch(Pitch.Base.F, 1, 3), Durations.QUARTER), D_FSharp.getHighestNote());
	}

	@Test
	public void testRemovePitch() {
		Chord D_FSharp = this.dMajor.removePitch(Pitch.getPitch(Pitch.Base.A, 0, 3));
		assertFalse(this.dMajor.equals(D_FSharp));
		assertEquals(2, D_FSharp.getNoteCount());
		assertEquals(Note.getNote(Pitch.getPitch(Pitch.Base.F, 1, 3), Durations.QUARTER), D_FSharp.getHighestNote());
	}

	@Test
	public void testIteration() {
		ArrayList<Note> cMajorNotes = new ArrayList<>();
		cMajorNotes.add(Note.getNote(Pitch.getPitch(Pitch.Base.C, 0, 4), Durations.QUARTER));
		cMajorNotes.add(Note.getNote(Pitch.getPitch(Pitch.Base.E, 0, 4), Durations.QUARTER));
		cMajorNotes.add(Note.getNote(Pitch.getPitch(Pitch.Base.G, 0, 4), Durations.QUARTER));

		int noteCount = 0;
		for (Note note : this.cMajor) {
			assertEquals(cMajorNotes.get(noteCount), note);
			++noteCount;
			assertTrue(cMajorNotes.contains(note));
		}

		Chord cMaj = Chord.getChord(cMajorNotes);
		Iterator<Note> iterator = cMaj.iterator();
		iterator.next();
		try {
			iterator.remove();
		} catch (Exception e) {
			assertTrue(e instanceof UnsupportedOperationException);
		}

		for (Note note : cMajorNotes) {
			assertTrue("Iterator violated immutability of Chord", cMaj.contains(note));
		}
	}
}
