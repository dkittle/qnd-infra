package ca.kittle.core

import ca.kittle.Stack
import ca.kittle.envTags
import com.pulumi.aws.acm.kotlin.certificate

suspend fun lookupWebsiteCertificate(env: Stack) =
    certificate("${env.stackName}-qnd-certificate") {
        args {
            domainName("*.quillndice.com")
            keyAlgorithm("RSA_2048")
            subjectAlternativeNames("*.quillndice.com", "quillndice.com")
            validationMethod("DNS")
            tags(envTags(env, "qnd-cert"))
        }
        opts {
            retainOnDelete(true)
        }
    }
