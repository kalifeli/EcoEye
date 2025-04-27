package com.ecoeye.util

import com.amazonaws.regions.Regions

object Config {
    const val AWS_IOT_ENDPOINT = "a2f2bxcm4j6lao-ats.iot.eu-central-1.amazonaws.com"
    const val COGNITO_IDENTITY_POOL_ID = "eu-central-1:9b701385-b372-47ea-8cc2-8189d77a2b5f"
    val REGION = Regions.EU_CENTRAL_1
    const val AWS_IOT_TOPIC = "esp32/trascrizione/out"

}
