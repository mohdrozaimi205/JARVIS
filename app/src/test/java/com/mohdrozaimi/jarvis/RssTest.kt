package com.mohdrozaimi.jarvis

import com.mohdrozaimi.jarvis.news.parseRss
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RssTest {
    private val xml = """<?xml version="1.0" encoding="UTF-8"?>
        <rss version="2.0"><channel><title>Feed</title>
        <item><title>  Banjir di   Kelantan </title></item>
        <item><title><![CDATA[Harga minyak &amp;#39;stabil&amp;#39;]]></title></item>
        <item><title>Ketiga</title></item>
        <item><title>Keempat</title></item>
        </channel></rss>"""

    @Test fun parsesAndCleans() {
        val h = parseRss(xml, "Malaysia", 3)
        assertEquals(3, h.size)
        assertEquals("Banjir di Kelantan", h[0].title)
        assertEquals("Malaysia", h[0].source)
    }

    @Test fun respectsMax() = assertEquals(1, parseRss(xml, "x", 1).size)

    @Test fun brokenXmlGivesEmpty() = assertTrue(parseRss("<rss><item>", "x", 3).isEmpty())
}
