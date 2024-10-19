package ca.kittle.website

import ca.kittle.Stack
import ca.kittle.core.lookupWebsiteCertificate
import ca.kittle.network.createWebsiteDomainRecord
import ca.kittle.storage.buildCdnForWebsite
import ca.kittle.storage.createWebsiteBucket
import ca.kittle.storage.secureWebsite
import com.pulumi.aws.cloudfront.kotlin.Distribution

suspend fun createWebsite(
    env: Stack,
    name: String,
    domainName: String
): Distribution {
    val qndCert = lookupWebsiteCertificate(env)
    val bucket = createWebsiteBucket(env, name)
    secureWebsite(env, bucket, name)
    val cdn = buildCdnForWebsite(env, bucket, qndCert, name, domainName)
    createWebsiteDomainRecord(env, cdn)
    return cdn
}
