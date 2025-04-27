package com.ecoeye.util

import com.amazonaws.regions.Regions

object Config {
    const val AWS_IOT_ENDPOINT = "_YOUR_AWS_IOT_ENDPOINT_"
    const val COGNITO_IDENTITY_POOL_ID = "_YOUR_COGNITO_IDENTITY_POOL_ID_"
    val REGION = Regions.EU_CENTRAL_1
    const val AWS_IOT_TOPIC = "_YOUR_AWS_IOT_SUBSCRIBE_TOPIC_"
}
