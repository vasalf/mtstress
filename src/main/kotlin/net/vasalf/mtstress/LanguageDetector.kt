package net.vasalf.mtstress

import com.beust.klaxon.Json
import com.beust.klaxon.Klaxon

class LanguageDetector(ldJson : String) {
    class KlaxonReadableLanguageDetector(@Json(name = "languages") val langList : List<Language>)

    val langList : List<Language>
    init {
        val krLD = Klaxon().parse<KlaxonReadableLanguageDetector>(ldJson)
                ?: throw RuntimeException("Could not read the LanguageDetector config")
        langList = krLD.langList
    }

    class UnknownLanguageException(filename : String) : Exception("Could not detect the language for $filename")

    fun detect(s : Solution) : Language  {
        for (l in langList) {
            for (suffix in l.suffixes) {
                if (s.filename.endsWith(suffix)) {
                    return l
                }
            }
        }
        throw UnknownLanguageException(s.filename)
    }
}