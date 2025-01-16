package jsonlint

import js.objects.JsPlainObject

@JsPlainObject
external interface Options {
    val ignoreBOM: Boolean?
    val ignoreComments: Boolean?
    val ignoreTrailingCommas: Boolean?
    val allowSingleQuotedStrings: Boolean?
    val allowDuplicateObjectKeys: Boolean?
    val ignoreProtoKey: Boolean?
    val ignorePrototypeKeys: Boolean?
    val mode: String?
}
