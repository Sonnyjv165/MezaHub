package com.example.mezahub

import com.example.mezahub.data.AppLanguage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.w3c.dom.Element
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

/** Guards the translations: no missing/extra keys, and placeholders that match English. */
class TranslationsTest {

    private val res = File("src/main/res")
    private val placeholder = Regex("""%(\d+)\$[sd]""")

    private data class Strings(val strings: Map<String, String>, val plurals: Map<String, Map<String, String>>)

    private fun load(folder: String): Strings {
        val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(File(res, "$folder/strings.xml"))
        val strings = mutableMapOf<String, String>()
        val plurals = mutableMapOf<String, Map<String, String>>()
        val strNodes = doc.getElementsByTagName("string")
        for (i in 0 until strNodes.length) {
            val e = strNodes.item(i) as Element
            if (e.getAttribute("translatable") != "false") strings[e.getAttribute("name")] = e.textContent
        }
        val pluralNodes = doc.getElementsByTagName("plurals")
        for (i in 0 until pluralNodes.length) {
            val e = pluralNodes.item(i) as Element
            val items = e.getElementsByTagName("item")
            plurals[e.getAttribute("name")] = (0 until items.length).associate {
                val item = items.item(it) as Element
                item.getAttribute("quantity") to item.textContent
            }
        }
        return Strings(strings, plurals)
    }

    private val translationFolders = res.listFiles { f -> f.isDirectory && f.name.startsWith("values-") && File(f, "strings.xml").exists() }!!
        .map { it.name }
        .sorted()

    private fun placeholders(text: String) = placeholder.findAll(text).map { it.value }.toSortedSet()

    @Test
    fun everyTranslationHasExactlyTheEnglishKeys() {
        val english = load("values")
        assertEquals(8, translationFolders.size)
        for (folder in translationFolders) {
            val t = load(folder)
            assertEquals("string keys in $folder", english.strings.keys, t.strings.keys)
            assertEquals("plural keys in $folder", english.plurals.keys, t.plurals.keys)
        }
    }

    @Test
    fun placeholdersMatchEnglish() {
        val english = load("values")
        for (folder in translationFolders) {
            val t = load(folder)
            for ((key, text) in english.strings) {
                assertEquals("$folder/$key", placeholders(text), placeholders(t.strings.getValue(key)))
            }
            for ((key, items) in english.plurals) {
                val expected = placeholders(items.getValue("other"))
                t.plurals.getValue(key).forEach { (quantity, text) ->
                    assertEquals("$folder/$key[$quantity]", expected, placeholders(text))
                }
                assertTrue("$folder/$key needs an 'other' quantity", "other" in t.plurals.getValue(key))
            }
        }
    }

    @Test
    fun everyAppLanguageHasStringsAndIsDeclaredForTheSystemPicker() {
        val localesConfig = File(res, "xml/locales_config.xml").readText()
        val declared = Regex("""android:name="([^"]+)"""").findAll(localesConfig).map { it.groupValues[1] }.toSet()
        for (language in AppLanguage.entries) {
            val tag = language.tag ?: continue
            if (tag != "en") {
                val folder = "values-" + tag.replace("-", "-r")
                assertTrue("missing $folder for $language", File(res, "$folder/strings.xml").exists())
            }
            val modernTag = when (tag) { "tl" -> "fil"; "in" -> "id"; else -> tag }
            assertTrue("$modernTag missing from locales_config", modernTag in declared)
        }
        assertEquals(AppLanguage.entries.count { it.tag != null }, declared.size)
    }
}
