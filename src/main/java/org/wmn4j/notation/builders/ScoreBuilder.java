/*
 * Distributed under the MIT license (see LICENSE.txt or https://opensource.org/licenses/MIT).
 */
package org.wmn4j.notation.builders;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.wmn4j.notation.elements.Part;
import org.wmn4j.notation.elements.Score;

/**
 * Class for building {@link Score} objects.
 */
public class ScoreBuilder {

	private final Map<Score.Attribute, String> scoreAttr;
	private final List<PartBuilder> partBuilders;

	/**
	 * Constructor that creates an empty builder.
	 */
	public ScoreBuilder() {
		this.scoreAttr = new HashMap<>();
		this.partBuilders = new ArrayList<>();
	}

	/**
	 * Set the given attribute to given value.
	 *
	 * @param attribute      the attribute to be set
	 * @param attributeValue value for the attribute
	 */
	public void setAttribute(Score.Attribute attribute, String attributeValue) {
		this.scoreAttr.put(attribute, attributeValue);
	}

	/**
	 * Add {@link PartBuilder} to this builder.
	 *
	 * @param partBuilder partBuilder to add to this builder
	 */
	public void addPart(PartBuilder partBuilder) {
		this.partBuilders.add(partBuilder);
	}

	/**
	 * Returns a {@link Score} with the contents of this builder.
	 *
	 * @return a score with the contents of this builder
	 */
	public Score build() {
		final List<Part> parts = new ArrayList<>();
		this.partBuilders.forEach((builder) -> parts.add(builder.build()));
		return Score.of(this.scoreAttr, parts);
	}
}
