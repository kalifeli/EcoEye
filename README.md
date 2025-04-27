# EcoEye
Il progetto EcoEye permette la comunicazione tra un dispositivo ESP32 e un'app Android tramite AWS IoT Core,
offrendo funzionalità di riconoscimento vocale (registrazione e upload audio) e messaggistica MQTT.

## Requisiti

* Hardware:
  * ESP32
*  Software ESP32:
  * Arduino IDE
* Software Android:
  * Android Studio
  * JDK 11+
  * Gradle 7.x
  * Amplify CLI v8.x
* Account AWS

  ## Configurazione delle credenziali

  ### 1. ESP32
  * Copia il contenuto del file certs.h e sostituisci i palceholder con
    * le corrette credenziali della tua rete
    * il corretto endpoint di AWS
    * i tuoi certificati
  ```cpp
  __WIFI_SSID__             // es.: "Wind3 HUB-XXXX"
  __WIFI_PASSWORD__         // es.: "password_wifi"
  __AWS_IOT_ENDPOINT__      // es.: "xxxxxxxxxxxxxx-ats.iot.regione.amazonaws.com"
  __AWS_CERT_CA__           // certificato CA Root Amazon
  __AWS_CERT_CRT__          // certificato dispositivo
  __AWS_CERT_PRIVATE__      // chiave privata dispositivo
  ```
