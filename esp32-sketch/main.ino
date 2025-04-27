#include "Arduino.h"
#include "utils.h"

void setup() {
  Serial.begin(115200);
  setupDisplay();
  delay(1000);
  connectAWS();
}

void loop() {
  if(!client.connected()){
    connectAWS();
  }
  client.loop();
  delay(1000);
}

