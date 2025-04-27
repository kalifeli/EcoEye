#ifndef UTILS_H
#define UTILS_H

#include <Arduino.h>

#include <Adafruit_SSD1306.h>

#include <WiFi.h>
#include <WiFiClientSecure.h>
#include <PubSubClient.h>
#include <ArduinoJson.h>
#include "certs.h"

// ——— Constanti per il display OLED ———
#define SCREEN_WIDTH  128   // larghezza in pixel
#define SCREEN_HEIGHT 64    // altezza in pixel
#define OLED_RESET    -1    // reset gestito internamente

// ——— Topic MQTT di input e di ouput
#define AWS_IOT_PUBLISH_TOPIC   "esp32/trascrizione/in"
#define AWS_IOT_SUBSCRIBE_TOPIC "esp32/trascrizione/out"

// ——— Variabili esterne ———

// display OLED
extern Adafruit_SSD1306 display;

// Istanza globale del client TLS e del PubSubClient
extern WiFiClientSecure net;
extern PubSubClient     client;

// --- Prototipi delle funzioni ---

// Inizializzazione del display
void setupDisplay();

// Aggiornamento del contenuto del display
void updateDisplay(const char* message);

// callback per i messaggi MQTT in ingresso
void messageHandler(char* topic, byte* payload, unsigned int length);

// Connessione al WiFi e al servizio AWS IoT Core
void connectAWS();

// Pubblicazione di un messaggio su un topic di input
void publishMessage(const String& msg);

#endif  // UTILS_H
