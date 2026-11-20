package com.mohdrozaimi.jarvis.weather

const val DEFAULT_PLACE = "Kuala Lumpur"

/**
 * Padankan nama daerah dari Geocoder dengan nama lokasi MET.
 * Urutan: sama tepat → satu mengandungi yang lain → lalai "Kuala Lumpur".
 */
fun matchLocation(district: String?, names: List<String>): String {
    val d = district?.trim()?.lowercase()
    if (d.isNullOrEmpty()) return DEFAULT_PLACE
    names.firstOrNull { it.lowercase() == d }?.let { return it }
    names.firstOrNull { it.lowercase().contains(d) || d.contains(it.lowercase()) }?.let { return it }
    return DEFAULT_PLACE
}
