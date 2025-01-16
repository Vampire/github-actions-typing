package yaml

@JsModule("yaml")
external object YAML {
    fun <T> parse(text: String): T
}
