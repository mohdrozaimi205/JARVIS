package com.mohdrozaimi.jarvis.route

data class DistanceQuery(val from: String, val to: String)

private val DISTANCE = Regex(
    """(?:jarak|distance|berapa\s+(?:km|kilometer|jauh))\s*(?:dari|from)?\s+(.+?)\s+(?:ke|to)\s+(.+)""",
    RegexOption.IGNORE_CASE,
)

/** "jarak dari Ipoh ke KL" / "distance from Ipoh to KL" / "berapa km Ipoh ke Johor Bahru". */
fun parseDistanceCommand(text: String): DistanceQuery? {
    val m = DISTANCE.find(text.trim()) ?: return null
    val from = m.groupValues[1].clean()
    val to = m.groupValues[2].clean()
    if (from.isEmpty() || to.isEmpty()) return null
    return DistanceQuery(from, to)
}

private fun String.clean() = trim().trimEnd('?', '.', '!', ',').trim()
