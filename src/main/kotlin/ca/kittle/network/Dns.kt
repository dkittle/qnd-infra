package ca.kittle.network

import ca.kittle.Stack
import com.pulumi.aws.cloudfront.kotlin.Distribution
import com.pulumi.aws.route53.kotlin.Record
import com.pulumi.aws.route53.kotlin.record

// TODO Look up Zone Ids from Route53
private fun domainNames(env: Stack): Pair<String, String> =
    when (env) {
        Stack.Dev -> Pair("dev.quillndice.com", "Z02219281O973CNEZDMPD")
        Stack.Staging -> Pair("stage.quillndice.com", "Z02219281O973CNEZDMPD")
        Stack.Prod -> Pair("quillndice.com", "Z02219281O973CNEZDMPD")
    }


suspend fun createWebsiteDomainRecord(env: Stack, cdn: Distribution, name: String, domainName: String): Record {
    val cdnAlias = cdn.domainName.applyValue(fun(name: String): String { return name })
    val cdnZoneId = cdn.hostedZoneId.applyValue(fun(name: String): String { return name })
    return record("${env.stackName}-$name-record") {
        args {
            zoneId(domainNames(env).second)
            name("${env.subdomain()}$domainName")
//            name(domainNames(env).first)
            aliases {
                name(cdnAlias)
                zoneId(cdnZoneId)
                evaluateTargetHealth(false)
            }
            type("A")
        }
    }
}
