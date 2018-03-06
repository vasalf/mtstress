package net.vasalf.mtstress

class Solution(val filename : String) {
    val language = MtStress.config.languageDetector.detect(this)

    val executableName : String = filename.substringAfterLast("/")
                                          .dropLastWhile { it != '.' }
                                          .dropLast(1)

    val executablePath : String = "${MtStress.config.problem.compiledPath}/$executableName"

    val compileLine : String = templateReplace(
            language.compileTemplate,
            mapOf("%solution%" to filename, "%executable%" to executablePath)
    )

    val execLine : String = templateReplace(
            language.execTemplate,
            mapOf("%solution%" to filename, "%executable%" to executablePath)
    )

    fun compile() : Boolean {
        val process = ProcessBuilder(compileLine.split(" ").toList()).start()
        process.waitFor()
        return process.exitValue() == 0
    }

    override fun toString() : String {
        return "$filename in ${language.name}"
    }

    var verdict = Verdict.OK
}