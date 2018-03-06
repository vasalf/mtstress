package net.vasalf.mtstress

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.default

class MtStressArgs(parser : ArgParser) {
    val ldFile by parser.storing("--languages",
            help = "path to language detector config").default("languages.json")

    val prFile by parser.storing("--problem",
            help = "path to problem config").default("problem.json")

    val numTests by parser.storing("--num-tests",
            help = "number of tests to be run on each solution") { toInt() } .default(100)

    val numThreads by parser.storing("--num-threads",
            help = "number of threads to be launched") { toInt() }.default(2)
}

fun main(args : Array<String>) {
    val parsedArgs = ArgParser(args).parseInto(::MtStressArgs)

    MtStress.initialize(parsedArgs.ldFile, parsedArgs.prFile, parsedArgs.numThreads, parsedArgs.numTests)

    println("found solutions: ")
    MtStress.solutionList.solutions.forEach {
        println(it)
    }
    println()

    print("compiling solutions... ")
    MtStress.compileAll()
    println("done.")
    println()

    print("running the stress...")
    MtStress.run()
    println("done.")
    println()

    for (solution in MtStress.solutionList.solutions) {
        if (solution.verdict != Verdict.OK) {
            println("Solution $solution got ${solution.verdict}")
        }
    }
}