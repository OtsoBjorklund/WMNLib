/*
 * Copyright 2018 Otso Björklund.
 * Distributed under the MIT license (see LICENSE.txt or https://opensource.org/licenses/MIT).
 */
package wmnlibmir.pattern_discovery;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import wmnlibio.musicxml.MusicXmlDomReader;
import wmnlibio.musicxml.MusicXmlReader;
import wmnlibnotation.Score;

/**
 *
 * @author Otso Björklund
 */
public class PointSetTest {
    
    public PointSetTest() {
    }
    
    @Before
    public void setUp() {
    }

    @Test
    public void testCreatingFromSingleStaffScore() {
        MusicXmlReader reader = new MusicXmlDomReader();
        try {
            Score score = reader.readScore("test/testfiles/musicxml/twoMeasures.xml");
            PointSet pointset = new PointSet(score);
            System.out.println(score);
            System.out.println(pointset);
            fail("This test is not implemented yet");
            
        }
        catch(Exception e) {
            System.out.println(e);
        }
    }
    
}
