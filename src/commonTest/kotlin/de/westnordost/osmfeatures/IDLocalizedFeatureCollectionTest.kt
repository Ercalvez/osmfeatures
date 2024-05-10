package de.westnordost.osmfeatures

import okio.Source
import kotlin.test.*

class IDLocalizedFeatureCollectionTest {
    @Test
    fun features_not_found_produces_runtime_exception() {
        assertFails {
            IDLocalizedFeatureCollection(object : FileAccessAdapter {
                override fun exists(name: String): Boolean = false
                override fun open(name: String): Source = throw Exception()
            })
        }
    }

    @Test
    fun load_features_and_two_localizations() {
        val c = IDLocalizedFeatureCollection(object : FileAccessAdapter {
            override fun exists(name: String): Boolean =
                name in listOf("presets.json", "en.json", "de.json")

            override fun open(name: String): Source = when (name) {
                "presets.json" -> getSource("some_presets_min.json")
                "en.json" -> getSource("localizations_en.json")
                "de.json" -> getSource("localizations_de.json")
                else -> throw Exception("File not found")
            }
        })

        // getting non-localized features
        val notLocalized = listOf<String?>(null)
        val notLocalizedFeatures = c.getAll(notLocalized)
        assertEquals(setOf("test", "test", "test"), notLocalizedFeatures.map { it.name }.toSet())
        assertEquals("test", c.get("some/id", notLocalized)?.name)
        assertEquals("test", c.get("another/id", notLocalized)?.name)
        assertEquals("test", c.get("yet/another/id", notLocalized)?.name)

        // getting English features
        val english = listOf("en")
        val englishFeatures = c.getAll(english)
        assertEquals(setOf("Bakery"), englishFeatures.map { it.name }.toSet())
        assertEquals("Bakery", c.get("some/id", english)?.name)
        assertNull(c.get("another/id", english))
        assertNull(c.get("yet/another/id", english))

        // getting Germany features
        // this also tests if the fallback from de-DE to de works if de-DE.json does not exist
        val germany = listOf("de-DE")
        val germanyFeatures = c.getAll(germany)
        assertEquals(setOf("Bäckerei", "Gullideckel"), germanyFeatures.map { it.name }.toSet())
        assertEquals("Bäckerei", c.get("some/id", germany)?.name)
        assertEquals("Gullideckel", c.get("another/id", germany)?.name)
        assertNull(c.get("yet/another/id", germany))

        // getting features through fallback chain
        val locales = listOf("en", "de-DE", null)
        val fallbackFeatures = c.getAll(locales)
        assertEquals(
            setOf("Bakery", "Gullideckel", "test"),
            fallbackFeatures.map { it.name }.toSet()
        )
        assertEquals("Bakery", c.get("some/id", locales)?.name)
        assertEquals("Gullideckel", c.get("another/id", locales)?.name)
        assertEquals("test", c.get("yet/another/id", locales)?.name)
        assertEquals("en", c.get("some/id", locales)?.locale)
        assertEquals("de", c.get("another/id", locales)?.locale)
        assertNull(c.get("yet/another/id", locales)?.locale)
    }

    @Test
    fun load_features_and_merge_localizations() {
        val c = IDLocalizedFeatureCollection(object : FileAccessAdapter {
            override fun exists(name: String): Boolean =
                name in listOf(
                    "presets.json",
                    "de-AT.json",
                    "de.json",
                    "de-Cyrl.json",
                    "de-Cyrl-AT.json"
                )

            override fun open(name: String): Source = when (name) {
                "presets.json" -> getSource("some_presets_min.json")
                "de-AT.json" -> getSource("localizations_de-AT.json")
                "de.json" -> getSource("localizations_de.json")
                "de-Cyrl-AT.json" -> getSource("localizations_de-Cyrl-AT.json")
                "de-Cyrl.json" -> getSource("localizations_de-Cyrl.json")
                else -> throw Exception("File not found")
            }
        })

        // standard case - no merging
        val german = listOf("de")
        val germanFeatures = c.getAll(german)
        assertEquals(setOf("Bäckerei", "Gullideckel"), germanFeatures.map { it.name }.toSet())
        assertEquals("Bäckerei", c.get("some/id", german)?.name)
        assertEquals("Gullideckel", c.get("another/id", german)?.name)
        assertNull(c.get("yet/another/id", german))

        // merging de-AT and de
        val austria = listOf("de-AT")
        val austrianFeatures = c.getAll(austria)
        assertEquals(
            setOf("Backhusl", "Gullideckel", "Brückle"),
            austrianFeatures.map { it.name }.toSet()
        )
        assertEquals("Backhusl", c.get("some/id", austria)?.name)
        assertEquals("Gullideckel", c.get("another/id", austria)?.name)
        assertEquals("Brückle", c.get("yet/another/id", austria)?.name)

        // merging scripts
        val cryllic = listOf("de-Cyrl")
        assertEquals("бацкхаус", c.get("some/id", cryllic)?.name)
        val cryllicAustria = listOf("de-Cyrl-AT")
        assertEquals("бацкхусл", c.get("some/id", cryllicAustria)?.name)
    }
}

private fun getSource(file: String): Source =
    FileSystemAccess("src/commonTest/resources").open(file)