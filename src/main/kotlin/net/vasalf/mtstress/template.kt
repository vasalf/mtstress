package net.vasalf.mtstress

fun templateReplace(s : String, mp : Map<String, String>) : String {
    var t = s
    mp.forEach { old, new -> t = t.replace(old, new) }
    return t
}