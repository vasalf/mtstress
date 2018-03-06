package net.vasalf.mtstress

import com.beust.klaxon.Json

class Language(val name : String,
               val suffixes : Array<String>,
               @Json(name = "compile_template")
               val compileTemplate : String,
               @Json(name = "exec_template")
               val execTemplate : String) {

}
