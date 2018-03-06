package net.vasalf.mtstress

import com.beust.klaxon.Json

data class Problem(
        @Json("generate_cmd")
        val generateCmd : String,
        @Json("check_template")
        val checkTemplate : String,
        @Json("correct_cmd")
        val correctCmd : String,
        @Json("solutions_path")
        val solutionsPath : String,
        @Json("compiled_path")
        val compiledPath : String,
        @Json("workdir")
        val workDir: String
)