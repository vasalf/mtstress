package net.vasalf.mtstress

import com.beust.klaxon.Klaxon
import java.io.File
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.thread

object MtStress {
    class ConfigUninitializedException : Exception("Attempt to get value from an uninitialized config.")

    class ConfigImpl(
            val languageDetector: LanguageDetector,
            val problem: Problem,
            val nthreads: Int,
            val nrep : Int
    )

    private var configImpl : ConfigImpl? = null
    val config : ConfigImpl
        get() = configImpl ?: throw ConfigUninitializedException()

    private var nullableSolutionList : SolutionList? = null
    val solutionList : SolutionList
        get() = nullableSolutionList ?: throw ConfigUninitializedException()

    fun initialize(ldFile : String, prFile : String, nthreads : Int, nrep : Int) {
        val ldContent = File(ldFile)
                .readLines()
                .joinToString(separator = "")
        val prContent = File(prFile)
                .readLines()
                .joinToString(separator = "")

        configImpl = ConfigImpl(
                LanguageDetector(ldContent),
                Klaxon().parse<Problem>(prContent) ?: throw RuntimeException("Could not read the Problem config"),
                nthreads, nrep
        )

        File(config.problem.solutionsPath).mkdirs()
        File(config.problem.compiledPath).mkdirs()
        File(config.problem.workDir).mkdirs()

        nullableSolutionList = SolutionList()
    }

    class CompilationFailedException(filename : String) : Exception("Compilation failed for ${filename}")

    fun compileAll() = solutionList.solutions.forEach {
        if (!it.compile())
            throw CompilationFailedException(it.filename)
    }

    fun runNoInp(exec: String, output: String = "/dev/null") : Boolean {
        val process = ProcessBuilder(exec.split(" ").toList())
                .redirectOutput(ProcessBuilder.Redirect.to(File(output)))
                .redirectError(ProcessBuilder.Redirect.to(File("/dev/null")))
                .start()
        process.waitFor()
        return process.exitValue() == 0
    }

    fun runAt(exec: String, input : String = "/dev/null", output: String = "/dev/null") : Boolean {
        val process = ProcessBuilder(exec.split(" ").toList())
                .redirectInput(ProcessBuilder.Redirect.from(File(input)))
                .redirectOutput(ProcessBuilder.Redirect.to(File(output)))
                .redirectError(ProcessBuilder.Redirect.to(File("/dev/null")))
                .start()
        process.waitFor()
        return process.exitValue() == 0
    }

    fun genTest(testFile : String) {
        if (!runNoInp(config.problem.generateCmd, output = testFile))
            throw RuntimeException("Generator failed!")
    }

    fun test(solution: Solution, input: String, output: String, correct: String) : Verdict {
        genTest(input)

        if (!runAt(config.problem.correctCmd, input, correct))
            return Verdict.FAIL
        if (!runAt(solution.execLine, input, output))
            return Verdict.RE

        if (!runNoInp(templateReplace(config.problem.checkTemplate, mapOf(
                        "%input%" to input, "%output%" to output, "%correct%" to correct
                )))) {
            return Verdict.WA
        }

        return Verdict.OK
    }

    fun run() {
        val i = AtomicInteger(0)
        val threads = ArrayList<Thread>()
        for (tn in 1..config.nthreads) {
            threads.add(thread(start = true) {
                while (true) {
                    var j : Int = -1
                    if (i.get() < solutionList.solutions.size) {
                        synchronized(solutionList) {
                            if (i.get() < solutionList.solutions.size) {
                                j = i.getAndIncrement()
                            }
                        }
                    }
                    if (j == -1)
                        break
                    val input = "${config.problem.workDir}/input_${solutionList.solutions[j].executableName}.txt"
                    val output = "${config.problem.workDir}/output_${solutionList.solutions[j].executableName}.txt"
                    val correct = "${config.problem.workDir}/correct_${solutionList.solutions[j].executableName}.txt"
                    val solution = solutionList.solutions[j]
                    for (rep in 1..config.nrep) {
                        val verdict = test(solution, input, output, correct)
                        if (verdict != Verdict.OK) {
                            solution.verdict = verdict
                            break
                        }
                    }
                }
            })
        }
        for (t in threads)
            t.join()
    }
}