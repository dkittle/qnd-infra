package ca.kittle

import ca.kittle.core.lookupWebsiteCertificate
import ca.kittle.network.createWebsiteDomainRecord
import ca.kittle.storage.secureWebsite
import ca.kittle.storage.buildWebsiteBucket
import ca.kittle.storage.buildCdnForWebsite
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

fun envTags(env: Stack, resource: String): Map<String, String> = mapOf(
    "Name" to "${env.stackName}-$resource",
    "Env" to env.name
)

fun run(ctx: Context) {
    runBlocking {
        val env = Stack.valueOf(ctx.stackName().replaceFirstChar { it.uppercase() })

        val qndCert = lookupWebsiteCertificate(env)
        val website = buildWebsiteBucket(env)
        secureWebsite(env, website)
        val cdn = buildCdnForWebsite(env, website, qndCert)
        createWebsiteDomainRecord(env, cdn)

        val cdnUrl = cdn.domainName.applyValue(fun(name: String): String { return name })

        ctx.export("cdn url", cdnUrl)
    }

}

