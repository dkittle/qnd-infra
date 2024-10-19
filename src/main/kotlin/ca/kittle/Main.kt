package ca.kittle

import ca.kittle.core.lookupWebsiteCertificate
import ca.kittle.website.createWebsite
import com.pulumi.Context
import com.pulumi.kotlin.Pulumi
import kotlinx.coroutines.runBlocking

fun main() {
    Pulumi.run(::run)
}

enum class Stack {
    Dev,
    Staging,
    Prod;

    val stackName: String = name.lowercase()

    fun subdomain(): String = if (this == Prod) "" else "$stackName."
}

fun envTags(
    env: Stack,
    resource: String
): Map<String, String> =
    mapOf(
        "Name" to "${env.stackName}-$resource",
        "Env" to env.name
    )

fun run(ctx: Context) {
    runBlocking {
        val env = Stack.valueOf(ctx.stackName().replaceFirstChar { it.uppercase() })

        val qndCert = lookupWebsiteCertificate(env)
        val qndWebsite = createWebsite(env, "qnd-website", "quillndice.com")
        val qndContent = createWebsite(env, "qnd-content", "content.quillndice.com")

        val qndWebsiteUrl = qndWebsite.domainName.applyValue(fun(name: String): String = name)
        val qndContentUrl = qndContent.domainName.applyValue(fun(name: String): String = name)

        ctx.export("qnd website url", qndWebsiteUrl)
        ctx.export("qnd content url", qndContentUrl)
    }
}
