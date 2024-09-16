package ca.kittle

import ca.kittle.cluster.containerRegistry
import ca.kittle.cluster.cluster
import ca.kittle.core.qndCertificate
import ca.kittle.network.*
import ca.kittle.security.SecurityGroupContext
import ca.kittle.security.inboundSecurityGroup
import ca.kittle.storage.*
import com.pulumi.Context
import com.pulumi.kotlin.Pulumi
import com.pulumi.kotlin.export
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.runBlocking

val mainScope = MainScope()
const val DOMAIN_NAME_KEY = "fqdn"

fun main() {
    Pulumi.run(::run)
}

enum class Stack {
    Dev,
    Staging,
    Prod;

    fun stackEnv(): String = name.lowercase()
}
fun envTags(env: Stack, resource: String): Map<String, String> = mapOf(
    "Name" to "${env.stackEnv()}-$resource",
    "Env" to env.name
)

fun run(ctx: Context) {
    runBlocking {
        val env = Stack.valueOf(ctx.stackName().replaceFirstChar { it.uppercase() })
        val domain = ctx.config().require(DOMAIN_NAME_KEY)
        val fqdn = "https://$domain"
        val staticSitePath = "../dm-seer/build/js/packages/dmseer"

        val vpc = environmentVpc(env)
        val privateSubnet = privateSubnet(env, vpc)
        val privateSubnet2 = privateSubnet2(env, vpc)
        val subnetGroup = privateSubnetGroup(env, privateSubnet, privateSubnet2)

        val qndCert = qndCertificate(env)
        val staticWebsite = staticWebsite(env)
        val websitePolicy = secureStaticWebsite(env, staticWebsite)
        val cdn = staticWebsiteCdn(env, staticWebsite, qndCert)
        val devDomain = domainRecord(env, cdn)

        val containerRegistry = containerRegistry(env)
        val mongoSG = inboundSecurityGroup(
            SecurityGroupContext(
                env,
                "mongo",
                "tcp",
                27017,
                vpc
            )
        )
        val mongo = mongoCluster(env, mongoSG, subnetGroup)
        mongoInstances(env, mongo)

        val containerRegistryUrl = containerRegistry.repositoryUrl.applyValue(fun(name: String): String { return name })
        val clusterUrn = mongo.urn.applyValue(fun(name: String): String { return name })
        val cdnUrl = cdn.domainName.applyValue(fun(name: String): String { return name })

        ctx.export("service domain name", fqdn)
        ctx.export("cdn url", containerRegistryUrl)
        ctx.export("mongo cluster urn", clusterUrn)
        ctx.export("container registry url", containerRegistryUrl)
    }

//    fun staticWebsite() {
//        Website()
//    }
}

