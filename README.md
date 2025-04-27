# EcoEye

![Frame 1](https://github.com/user-attachments/assets/d67bf186-2baa-4f65-a96b-6cf18b31b8bb)

Il progetto EcoEye permette la comunicazione tra un dispositivo ESP32 e un'app Android tramite AWS IoT Core,
offrendo funzionalità di riconoscimento vocale (registrazione e upload audio) e messaggistica MQTT.

E' possibile ottenere maggiori informazioni sul processo di realizzazione del progetto visitando la WiKi: https://github.com/kalifeli/EcoEye/wiki

## Requisiti

* Hardware:
  * ESP32
  * Display OLED I2C
  * Cavi Jumper
    
* Software ESP32:
  * Arduino IDE
    
* Software Android:
  * Android Studio
  * JDK 11+
  * Gradle 7.x
  * Amplify CLI v8.x
* Account AWS
* Una connessione ad internet


## Utilizzo

### ESP32

* Al boot si connette al WiFi e ad AWS IoT;
* sottoscrive un topic MQTT e si mette in attesa dell'arrivo di una trascrizione

### App Android:

* Premi il microfono per registrare audio e inviare la trascrizione al firmware
* Usa la sezione “Messaggi Rapidi” per inviare testi predefiniti

## Configurazione 

1. Clona la repositoty su un nuovo progetto di Android Studio.
2. Installa Arduino IDE e le seguenti librerie:
   - `Wire.h`
   - `Adafruit_SSD1306.h`
   - `WiFi.h`
   - `WiFiClientSecure.h`
   - `PubSubClient.h`
   - `ArduinoJson.h`
3. Per poter eseguire correttamente il progetto, è necessario configurare due file di impostazioni:
 * Uno per il firmware ESP32
 * Uno per l'app Android
  
### Configurazione ESP32

1. Copia il contenuto del file `certs.h` e sostituisci i palceholder con
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
2. Colelgare tramite USB l'ESP32 al computer.
3. Vai su `Tools` e seleziona i corretti valori per `Board` e `Port`.
4. Verifica e Carica lo sketch nella memoria flash del microcontrollore.

Queste credenziali permetteranno:
* La connessione alla rete WiFi.
* La connessione sicura a AWS IoT Core tramite handshake TLS

### Configurazione Android

Per l'app Android è necessario configurare il file `Config.kt` (`app/src/main/java/com/ecoeye/util/Config.kt`)
All'interno di questo file vanno specificate:
```kotlin
object Config {
    const val AWS_IOT_ENDPOINT = "_YOUR_AWS_IOT_ENDPOINT_"
    const val COGNITO_IDENTITY_POOL_ID = "_YOUR_COGNITO_IDENTITY_POOL_ID_"
    val REGION = Regions.EU_CENTRAL_1
    const val AWS_IOT_TOPIC = "_YOUR_AWS_IOT_SUBSCRIBE_TOPIC_"
}
```
>[!NOTE]
> Una volta configurato questo file è possibile eseguire l'applicazione su un dispositivo reale o su un emulatore.

### Configurazione AWS

#### IoT Core:

1. Crea un Thing EcoEye_esp32 e assegna policy che permetta connect/publish/subscribe
2. Scarica i certificati

#### Cognito:

1. Crea un Identity Pool con autenticazione anonima
2. Annota il Pool ID da inserire in Config.kt

#### S3

1. Crea un bucket di input, in cui inserire gli audio (es. ecoeye-audio-input)
2. Crea un bucket di output, in cui verranno caricati gli audio (es. ecoeye-audio-ouput)
3. Configura policy CORS e policy IAM per l’upload tramite Amplify

#### Lambda

1. Crea la prima funzione lambda che serve per gestire la trascrizione sul file audio caricato sul bucket di input:
   ```python
   import json 
   import boto3 #libreria per interagire con diversi servizi tra cui AWS
   import uuid #libreria per la generazione di un codice univoco

   def lambda_handler(event, context):
       
       # Estrai il nome del bucket e il nome del file dall'evento S3
       try:
           bucket = event['Records'][0]['s3']['bucket']['name']
           key = event['Records'][0]['s3']['object']['key']
       except Exception as e:
           print("Errore nell'estrazione dei dati S3: ", e)
           return {"statusCode": 400, "body": "Evento malformato"}
       
       # l'URI del file in S3 che specifica la sua posizione
       media_file_uri = f's3://{bucket}/{key}'
       print("Media file URI:", media_file_uri)
       
       # Configurazione del client Transcribe
       transcribe = boto3.client('transcribe')
       
       # Genera un nome univoco per il job di trascrizione
       job_name = "TranscriptionJob-" + str(uuid.uuid4())
       
       try:
           response = transcribe.start_transcription_job(
               TranscriptionJobName=job_name,
               Media={'MediaFileUri': media_file_uri},
               MediaFormat='m4a', 
               LanguageCode='it-IT',
               OutputBucketName='ecoeye-transcription-output'  # Utilizzo due bucket: qui introduco il nome del bucket di output creato
           )
           print("Transcription job started:", job_name)
       except Exception as e:
           print("Errore nell'avviare il job di trascrizione:", e)
           return {"statusCode": 500, "body": json.dumps("Errore nella trascrizione")}
       
       return {
           'statusCode': 200,
           'body': json.dumps('Job di trascrizione avviato con successo: ' + job_name)
       }
   ```
3. Definisci la seconda lambda che gestisce invece il caricamento dell'audio trascritto su un topic MQTT:
   ```python
        import json
     import boto3
      
     def lambda_handler(event, context):
         bucket_out = event['Records'][0]['s3']['bucket']['name']
         key_out = event['Records'][0]['s3']['object']['key']
     
         # Creazione di un client S3
         s3 = boto3.resource('s3') 
     
         #Lettura del file JSON
         try:
             object = s3.Object(bucket_out, key_out) # rappresenta un ogggetto Amazon Simple Storage Service (S3)
             body = object.get()['Body'].read().decode('utf-8')
             data = json.loads(body)
             print(data)
         except Exception as e:
             print("Errore nella lettura del file da S3. Errore:", e)
             return {
                 'statusCode': 500,
                 'body': json.dumps('Errore nella lettura del file da S3')
             }
     
         #Estrazione del testo trascritto
         try:
             trascrizione = data['results']['transcripts'][0]['transcript']
         except Exception as e:
             print("Errore nell'estrazione del testo trascritto. Errore:", e)
             return {
                 'statusCode': 500,
                 'body': json.dumps('Errore nell\'estrazione del testo trascritto')
             }
         # Log del testo prelevato dal file
         print("Testo trascritto:", trascrizione)
     
         iot_data = boto3.client('iot-data', region_name='eu-central-1')
         topic = "esp32/trascrizione/out"
         try:
             iot_data.publish(
                 topic=topic,
                 payload=json.dumps({"transcription": trascrizione}),
                 qos=1  #  QoS 1 garantisce che il messaggio sia consegnato almeno una volta
             )
             print("Messaggio pubblicato su IoT Core:", topic)
         except Exception as e:
             print("Errore nell'invio del messaggio su IoT Core:", e)
             return {"statusCode": 500, "body": "Errore nell'invio del messaggio su IoT Core"}
         
         return {
             'statusCode': 200,
             'body': json.dumps("Trascrizione pubblicata con successo!")
         }

   ```
Una volta terminata la configurazione è arrivato il momento di generare l'apk della nostra applicazione.

1. Recati nella sezione `Build` -> `Build APK` .
2. Verrà prodotto il file eseguibile dell'applicazione.

> [!WARNING]
> Ricorda che è necessario eseguire la configurazione del progetto per renderlo funzionante!

## Contribuire

Ti invito a contribuire al mio progetto! Apri issue o crea pull request.

## Contatti

Per maggiori informazioni [felizianiale34@gmail.com](mailto:felizianiale34@gmail.com)
