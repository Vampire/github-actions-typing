package it.krzeminski.githubactionstyping

import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe
import io.kotest.mpp.syspropOrEnv
import java.io.File

private val schemaFile = File(syspropOrEnv("schemaFile")!!)
private val badDir = File(syspropOrEnv("badDir")!!)

class PragmaTest : FunSpec({
    withData(
        nameFn = { "${it.name} should start with schema pragma line" },
        badDir.listFiles()!!.asSequence()
    ) {
        it.useLines { lines ->
            lines.first() shouldBe "# yaml-language-server: \$schema=${schemaFile.relativeTo(badDir).invariantSeparatorsPath}"
        }
    }
})
