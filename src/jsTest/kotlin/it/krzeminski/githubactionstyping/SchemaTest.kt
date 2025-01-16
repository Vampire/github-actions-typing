package it.krzeminski.githubactionstyping

import ajv.Ajv
import ajv.ValidateFunction
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.Matcher
import io.kotest.matchers.MatcherResult
import io.kotest.matchers.should
import io.kotest.matchers.shouldNot
import io.kotest.mpp.env
import node.buffer.BufferEncoding.Companion.utf8
import node.fs.readFile
import node.fs.readdirSync
import node.path.path
import yaml.YAML
import ajv.Options as AjvOptions
import jsonlint.Options as JsonlintOptions

private val schemaFile = env("schemaFile")!!
private val goodDir = env("goodDir")!!
private val badDir = env("badDir")!!

private lateinit var validate: ValidateFunction

class SchemaTest : FunSpec({
    beforeSpec {
        validate = Ajv(AjvOptions(strict = true)).compile(
            JSON.parse(readFile(schemaFile, utf8))
        )
    }

    test("jsonlint validation") {
        jsonlint.parse(
            readFile(schemaFile, utf8),
            JsonlintOptions(
                ignoreBOM = false,
                ignoreComments = false,
                ignoreTrailingCommas = false,
                allowSingleQuotedStrings = false,
                allowDuplicateObjectKeys = false,
            )
        )
    }

    withData(
        nameFn = { "valid typing: $it" },
        readdirSync(goodDir).asSequence()
    ) {
        it.shouldBeValid()
    }

    withData(
        nameFn = { "invalid typing: $it" },
        readdirSync(badDir).asSequence()
    ) {
        it.shouldNotBeValid()
    }
})

private fun beValid(): Matcher<String> {
    return Matcher { data ->
        MatcherResult(
            validate(YAML.parse(data)),
            { "schema validation failed:\n${JSON.stringify(validate.errors, null, 4)}" },
            { "should have failed schema validation but passed" },
        )
    }
}

private suspend fun String.shouldBeValid(): String {
    readFile(path.join(goodDir, this), utf8) should beValid()
    return this
}

private suspend fun String.shouldNotBeValid(): String {
    readFile(path.join(badDir, this), utf8) shouldNot beValid()
    return this
}
