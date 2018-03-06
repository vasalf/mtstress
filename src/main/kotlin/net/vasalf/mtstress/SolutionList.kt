package net.vasalf.mtstress

import java.io.File

class SolutionList {
    val solutions: List<Solution>

    init {
        val mutSolutions : MutableList<Solution> = ArrayList()
        File(MtStress.config.problem.solutionsPath)
                .walk()
                .forEach {
                    if (it.isFile) {
                        mutSolutions.add(Solution(it.toPath().toString()))
                    }
                }
        solutions = mutSolutions
    }
}