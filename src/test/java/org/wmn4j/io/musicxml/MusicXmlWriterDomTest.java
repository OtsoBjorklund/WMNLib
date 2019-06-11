/*
 * Distributed under the MIT license (see LICENSE.txt or https://opensource.org/licenses/MIT).
 */
package org.wmn4j.io.musicxml;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.wmn4j.io.ParsingFailureException;
import org.wmn4j.notation.TestHelper;
import org.wmn4j.notation.builders.MeasureBuilder;
import org.wmn4j.notation.builders.NoteBuilder;
import org.wmn4j.notation.builders.PartBuilder;
import org.wmn4j.notation.builders.ScoreBuilder;
import org.wmn4j.notation.elements.Durations;
import org.wmn4j.notation.elements.Note;
import org.wmn4j.notation.elements.Pitch;
import org.wmn4j.notation.elements.Score;
import org.wmn4j.notation.iterators.PartWiseScoreIterator;
import org.wmn4j.notation.iterators.ScoreIterator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class MusicXmlWriterDomTest {

	private Document readDocument(Path path) {
		try {
			final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setValidating(false);
			dbf.setNamespaceAware(true);
			dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
			dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
			final DocumentBuilder docBuilder = dbf.newDocumentBuilder();
			return docBuilder.parse(path.toFile());
		} catch (Exception e) {
			fail("Failed to open and parse document: " + e);
		}

		return null;
	}

	@TempDir
	Path temporaryDirectory;

	private final String MUSICXML_FILE_PATH = "musicxml/";

	private Score readMusicXmlTestFile(String testFileName, boolean validate) {
		final MusicXmlReader reader = MusicXmlReader.getReader(validate);
		Score score = null;
		final Path path = Paths.get(TestHelper.TESTFILE_PATH + MUSICXML_FILE_PATH + testFileName);

		try {
			score = reader.readScore(path);
		} catch (final IOException | ParsingFailureException e) {
			fail("Parsing failed with exception " + e);
		}

		assertNotNull(score);
		return score;
	}

	private Score writeAndReadScore(Score score) {
		MusicXmlWriterDom writer = new MusicXmlWriterDom(score);
		Path file = temporaryDirectory.resolve("file.xml");
		writer.writeToFile(file);

		final MusicXmlReader reader = MusicXmlReader.getReader(true);
		Score writtenScore = null;

		try {
			writtenScore = reader.readScore(file);
		} catch (final IOException | ParsingFailureException e) {
			fail("Reading score written by MusicXmlWriterDom failed with exception " + e);
		}

		assertNotNull(writtenScore);
		return writtenScore;
	}

	@Test
	void testWritingNotes() {
		ScoreBuilder scoreBuilder = new ScoreBuilder();
		PartBuilder partBuilder = new PartBuilder("Part1");
		MeasureBuilder measureBuilder = new MeasureBuilder(1);

		measureBuilder.addToVoice(1, new NoteBuilder(Pitch.of(Pitch.Base.C, 0, 0), Durations.QUARTER));
		measureBuilder.addToVoice(1, new NoteBuilder(Pitch.of(Pitch.Base.D, 1, 1), Durations.HALF));
		measureBuilder.addToVoice(1, new NoteBuilder(Pitch.of(Pitch.Base.E, -2, 2), Durations.QUARTER_TRIPLET));

		partBuilder.add(measureBuilder);
		scoreBuilder.addPart(partBuilder);
		Score score = scoreBuilder.build();

		Score writtenScore = writeAndReadScore(score);

		ScoreIterator iterator = new PartWiseScoreIterator(writtenScore);

		Note note = (Note) iterator.next();
		assertEquals(Pitch.Base.C, note.getPitch().getBase());
		assertEquals(0, note.getPitch().getAlter());
		assertEquals(0, note.getPitch().getOctave());
		assertEquals(Durations.QUARTER, note.getDuration());

		note = (Note) iterator.next();
		assertEquals(Pitch.Base.D, note.getPitch().getBase());
		assertEquals(1, note.getPitch().getAlter());
		assertEquals(1, note.getPitch().getOctave());
		assertEquals(Durations.HALF, note.getDuration());

		note = (Note) iterator.next();
		assertEquals(Pitch.Base.E, note.getPitch().getBase());
		assertEquals(-2, note.getPitch().getAlter());
		assertEquals(2, note.getPitch().getOctave());
		assertEquals(Durations.QUARTER_TRIPLET, note.getDuration());

		assertFalse(iterator.hasNext());
	}

	@Test
	void testWritingKeySignatures() {
		Score score = readMusicXmlTestFile("keysigs.xml", false);
		Score writtenScore = writeAndReadScore(score);

		MusicXmlFileChecks.assertKeySignaturesReadToScoreCorrectly(writtenScore);
	}

	@Test
	void testWritingTimeSignatures() {
		Score score = readMusicXmlTestFile("timesigs.xml", false);
		Score writtenScore = writeAndReadScore(score);

		MusicXmlFileChecks.assertTimeSignaturesReadCorrectly(writtenScore);
	}

	@Test
	void testWritingClefs() {
		Score score = readMusicXmlTestFile("clefs.xml", false);
		Score writtenScore = writeAndReadScore(score);

		MusicXmlFileChecks.assertClefsReadCorrectly(writtenScore);
	}

	@Test
	void testWritingBarlines() {
		Score score = readMusicXmlTestFile("barlines.xml", false);
		Score writtenScore = writeAndReadScore(score);

		MusicXmlFileChecks.assertBarlinesReadCorrectly(writtenScore);
	}

	@Test
	void testWritingMultipleVoicesAndChords() {
		Score score = readMusicXmlTestFile("twoMeasures.xml", false);
		Score writtenScore = writeAndReadScore(score);

		MusicXmlFileChecks.assertChordsAndMultipleVoicesReadCorrectly(writtenScore);
	}

	@Test
	void testWritingPickupMeasure() {
		Score score = readMusicXmlTestFile("pickup_measure_test.xml", false);
		Score writtenScore = writeAndReadScore(score);

		MusicXmlFileChecks.assertPickupMeasureReadCorrectly(writtenScore);
	}

	@Test
	void testWritingBasicNoteAppearances() {
		final Score score = readMusicXmlTestFile("basic_duration_appearances.xml", false);

		MusicXmlWriter writer = new MusicXmlWriterDom(score);
		Path filePath = temporaryDirectory.resolve("temporary_file.xml");
		writer.writeToFile(filePath);

		final Document document = readDocument(filePath);
		final Node partNode = document.getElementsByTagName(MusicXmlTags.PART).item(0);
		assertNotNull(partNode);

		final Optional<Node> measureNode = DocHelper.findChild(partNode, MusicXmlTags.MEASURE);
		assertTrue(measureNode.isPresent());

		final List<Node> noteNodes = DocHelper.findChildren(measureNode.get(), MusicXmlTags.NOTE).stream()
				.filter(node -> DocHelper.findChild(node, MusicXmlTags.NOTE_REST).isEmpty())
				.collect(Collectors.toList());

		assertEquals(8, noteNodes.size(), "Wrong number of note notes found in written test document");

		assertNoteNodeDurationTypeElement("32th", noteNodes.get(0));
		assertNoteNodeDurationTypeElement("16th", noteNodes.get(1));
		assertNoteNodeDurationTypeElement("eighth", noteNodes.get(2));
		assertNoteNodeDurationTypeElement("quarter", noteNodes.get(3));
		assertNoteNodeDurationTypeElement("half", noteNodes.get(4));
		assertNoteNodeDurationTypeElement("whole", noteNodes.get(5));
		assertNoteNodeDurationTypeElement("breve", noteNodes.get(6));
		assertNoteNodeDurationTypeElement("long", noteNodes.get(7));
	}

	private void assertNoteNodeDurationTypeElement(String expectedDurationTypeText, Node noteNode) {
		final List<Node> durationTypeNodes = DocHelper.findChildren(noteNode, MusicXmlTags.NOTE_DURATION_TYPE);
		assertEquals(1, durationTypeNodes.size());

		final Node durationTypeNode = durationTypeNodes.get(0);
		assertEquals(expectedDurationTypeText, durationTypeNode.getTextContent());
	}
}
