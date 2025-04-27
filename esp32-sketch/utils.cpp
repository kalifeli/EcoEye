#include "utils.h"
#include <Wire.h>

// ——— definizione delle variabili globali ———

Adafruit_SSD1306 display(SCREEN_WIDTH, SCREEN_HEIGHT, &Wire, OLED_RESET);

WiFiClientSecure net;
PubSubClient     client(net);

// ——— implementazioni ———

/*
  Inizializza il display e lo predispone per la visualizzazione del testo
*/
void setupDisplay() {
  Wire.begin();
  if (!display.begin(SSD1306_SWITCHCAPVCC, 0x3C)) {
    Serial.println("SSD1306 init failed");
    while(true) delay(100);
  }
  display.clearDisplay();
  display.display();
}

/*
  Aggiorna il contenuto del display con la stringa di testo passata come parametro. 
  Il testo viene visualizzato dinamicamente in base alla lunghezza del messaggio
  @param message, il messaggio che deve essere visualizzato sul display
*/
void updateDisplay(const char* message) {
  //Preparazione del display
  display.clearDisplay();
  display.setTextSize(2);
  display.setTextColor(SSD1306_WHITE);
  display.setTextWrap(false);

  // Misura il bounding box del testo
  int16_t  x0, y0;
  uint16_t w,  h;
  display.getTextBounds(message, 0, 0, &x0, &y0, &w, &h);

  // Se il testo entra tutto nello schermo, viene mostrato al centro
  if (w <= SCREEN_WIDTH) {
    int16_t cx = (SCREEN_WIDTH  - w) / 2;  // centro orizzontale
    int16_t cy = (SCREEN_HEIGHT - h) / 2;  // centro verticale
    display.setCursor(cx, cy);
    display.println(message);
    display.display();
    return;
  }

  //Altrimenti scrolla orizzontalmente
  const int totalScroll = w + SCREEN_WIDTH;   // pixel totali da percorrere
  const uint16_t delayMs = 50;               // ms tra uno step e l’altro

  for (int offset = 0; offset < totalScroll; offset++) {
    display.clearDisplay();
    // posiziona il testo in modo che scorra
    display.setCursor(SCREEN_WIDTH - offset, (SCREEN_HEIGHT - h) / 2);
    display.println(message);
    display.display();
    delay(delayMs);
  }
}

/*
  Funzione di callback che viene attivata al momento della ricezione di un messaggio osservato sul topic MQTT sottoscritto.
  Compie una deserializzazione del payload (contenuto del messaggio sul topic) estrando esclusivamente il testo contenuto nel campo "transcription"
*/
void messageHandler(char* topic, byte* payload, unsigned int length) {
  Serial.print("incoming: ");
  Serial.println(topic);

  StaticJsonDocument<200> doc;
  deserializeJson(doc, payload, length);
  const char* message = doc["transcription"];
  updateDisplay(message);
}

/*
  Si occupa di :
  - stabilire una connessione com la rete WiFi,
  - impostare i certificati per la comunicazione con il server 
  - impostare il server con cui si vuole comunicare, introducento endopount e porta
  - impostare il callback da azionare alla ricezione di un messaggio
  - sottoscrivere il client (ESP32) ad un topic MQTT
  - fornire controlli e messaggi di log
*/
void connectAWS() {
  WiFi.mode(WIFI_STA);
  WiFi.disconnect();
  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
  Serial.print("Connecting to Wi-Fi");
  
  while(WiFi.status() != WL_CONNECTED) {
    Serial.print(".");
    delay(500);
    yield();
  } 

  Serial.println(" OK");

  net.setCACert(AWS_CERT_CA);
  net.setCertificate(AWS_CERT_CRT);
  net.setPrivateKey(AWS_CERT_PRIVATE);

  client.setServer(AWS_IOT_ENDPOINT, 8883);
  client.setCallback(messageHandler);

  Serial.print("Connecting to AWS IoT Core");

  unsigned int tentativi = 0;
  while (!client.connect(THINGNAME) && tentativi < 10) {
    Serial.print(".");
    tentativi++;
    delay(200);
    client.loop();  
    yield();
  }

  if(!client.connected()){
    Serial.println("Impossibile connettersi ad AWS IoT!");
    return;
  }

  client.subscribe(AWS_IOT_SUBSCRIBE_TOPIC);
  client.loop();  // completa handshake + SUBACK
  Serial.println(" AWS IoT Connected!");
}

void publishMessage(const String& msg) {
  // Controlla connessione
  if (!client.connected()) {
    Serial.printf("Publish: client non connesso (state=%d), riconnessione...\n",
                  client.state());
    connectAWS();
    client.loop();
  }

  // Debug prima del publish
  Serial.printf("MQTT connected? %s (state=%d)\n",
                client.connected() ? "YES" : "NO",
                client.state());

  // Il vero publish
  bool ok = client.publish(AWS_IOT_PUBLISH_TOPIC, msg.c_str());
  if (ok) {
    Serial.println("Messaggio pubblicato con successo");
  } else {
    Serial.printf("Errore nella pubblicazione (state=%d)\n", client.state());
  }
}

