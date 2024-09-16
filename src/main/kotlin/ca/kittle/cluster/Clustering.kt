package ca.kittle.cluster

import ca.kittle.Stack
import ca.kittle.envTags
import com.pulumi.aws.ecr.kotlin.Repository
import com.pulumi.aws.ecr.kotlin.repository
import com.pulumi.aws.ecs.kotlin.cluster



suspend fun cluster(env: Stack) =
    cluster("${env.name.lowercase()}-dmseer-cluster") {
        args {
            tags(envTags(env, "cluster"))
        }
    }

suspend fun containerRegistry(env: Stack): Repository =
    repository("${env.name.lowercase()}-dmseer-repository") {
        args {
            tags(envTags(env, "container-repository"))
        }
    }
