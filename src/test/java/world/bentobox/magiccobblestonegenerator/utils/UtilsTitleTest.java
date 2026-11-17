package world.bentobox.magiccobblestonegenerator.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Tests for shortening names so panel titles fit inside the inventory window.
 */
class UtilsTitleTest {

    @Test
    void testCharWidthsAreBold() {
        // Glyph + 1 px spacing + 1 px bold.
        assertEquals(7, Utils.getCharWidth('A'));
        assertEquals(3, Utils.getCharWidth('i'));
        assertEquals(5, Utils.getCharWidth(' '));
        assertEquals(10, Utils.getCharWidth('島'));
    }

    @Test
    void testShortNameUnchanged() {
        assertEquals("Diamond Generator", Utils.truncateToWidth("Diamond Generator", Utils.TITLE_WIDTH));
    }

    @Test
    void testNameThatJustFitsUnchanged() {
        // 159 px bold: fits in the 160 px title area without the label.
        assertEquals(159, Utils.getTextWidth("Starting Nether Generator"));
        assertEquals("Starting Nether Generator",
            Utils.truncateToWidth("Starting Nether Generator", Utils.TITLE_WIDTH));
    }

    @Test
    void testLongNameShortenedToFit() {
        String name = "Extremely Long Ultimate Netherite Generator";
        String result = Utils.truncateToWidth(name, Utils.TITLE_WIDTH);

        assertTrue(result.endsWith(Utils.ELLIPSIS), result);
        assertTrue(Utils.getTextWidth(result) <= Utils.TITLE_WIDTH, result);
        assertTrue(name.startsWith(result.substring(0, result.length() - 1)), result);
    }

    @Test
    void testNoSpaceBeforeEllipsis() {
        // If the cut lands just after a space, the space is dropped.
        String result = Utils.truncateToWidth("AAAAAAAAAAAAAAAAAAAAA BBBBBBBBB", Utils.TITLE_WIDTH);
        assertTrue(!result.contains(" " + Utils.ELLIPSIS), result);
    }

    @Test
    void testAlwaysKeepsOneCharacter() {
        // Even with no room, the title shows at least the first character.
        assertEquals("D" + Utils.ELLIPSIS, Utils.truncateToWidth("Diamond", 0));
    }

    @Test
    void testWideCharactersShortened() {
        String name = "島島島島島島島島島島島島島島島島島島島島";
        String result = Utils.truncateToWidth(name, Utils.TITLE_WIDTH);

        assertTrue(result.endsWith(Utils.ELLIPSIS), result);
        assertTrue(Utils.getTextWidth(result) <= Utils.TITLE_WIDTH, result);
    }
}
