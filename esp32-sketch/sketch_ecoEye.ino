// Librerie Necessarie
#include <BLEDevice.h>         // Libreria per il Bluetooth Low Energy (BLE) su ESP32
#include <BLEUtils.h>          // Funzioni utili per BLE
#include <BLEServer.h>         // Serve per creare un BLE server
#include <Wire.h>              // Per la comunicazione I2C (necessaria per il display OLED)
#include <Adafruit_GFX.h>      
#include <Adafruit_SSD1306.h>  // Libreria specifica per display OLED basati su SSD1306

// Definizioni di alcune costanti utili per il display
#define SCREEN_WIDTH 128       
#define SCREEN_HEIGHT 64      
#define OLED_RESET    -1 //serve per indicare di gestire internamente il reset

// Creazione dell'oggetto 'display' della classe Adafruit_SSD1306.
// Questo oggetto gestisce la comunicazione con il display tramite il bus I2C (usando la libreria Wire).
Adafruit_SSD1306 display(SCREEN_WIDTH, SCREEN_HEIGHT, &Wire, OLED_RESET);

// UUID per identificare univocamente il servizio BLE e la sua caratteristica (128 bit)
#define SERVICE_UUID        "4fafc201-1fb5-459e-8fcc-c5c9c331914b"
#define CHARACTERISTIC_UUID "beb5483e-36e1-4688-b7f5-ea07361b26a8"

// classe per gestire l'evento di scrittutra di una caratteristica
class MyCallbacks: public BLECharacteristicCallbacks {
  public:
    // Il metodo onWrite() viene invocato ogni volta che un client BLE scrive un nuovo valore
    // sulla caratteristica esposta
    void onWrite(BLECharacteristic *pCharacteristic) override {
     
      String nuovoTesto = pCharacteristic->getValue();
      
      // Verifichiamo se la stringa non è vuota.
      if(nuovoTesto.length() > 0){
        Serial.print("Testo ricevuto: ");
        Serial.println(nuovoTesto);
      }
      
      // Aggiornamento del display OLED
      display.clearDisplay();
      display.setTextSize(2);
      display.setTextColor(SSD1306_WHITE);
      display.setCursor(0, 0); //posizione del corsure in alto a sinistra dello schermo
      display.println(nuovoTesto); // stampa del testo sullo schermo
      display.display(); // aggiornamento del display
    }
}; 

// Funzione per l'inizializzazione 
void setup() {
  // Inizializziamo la comunicazione seriale per il debug
  Serial.begin(115200);
  Serial.println("Starting BLE and OLED work!");

  // Inizializzazione del display:
  // SSD1306_SWITCHCAPVCC : una modalità di alimentazione
  // 0x3C: indirizzo I2C che utilizza il diplay (l'ho verificato con un altro sketch)

  if (!display.begin(SSD1306_SWITCHCAPVCC, 0x3C)) { 
    Serial.println(F("SSD1306 allocation failed"));
    for (;;) ; // Se il display non si inizializza correttamente, fermiamo l'esecuzione
  }
  display.clearDisplay();
  display.display();

  // Inizializzazione del dispositivo BLE e definizione del nome del dispositivo 
  BLEDevice::init("EcoEye Glasses");  

  // Creazione del BLE server
  BLEServer *pServer = BLEDevice::createServer();
  // Creazione del servizio con l'UUID specificato
  BLEService *pService = pServer->createService(SERVICE_UUID);
  
  // Creazione di una caratteristica all'interno del servizio, con le proprietà di scrittura e di lettura
  BLECharacteristic *pCharacteristic = pService->createCharacteristic(
      CHARACTERISTIC_UUID,
      BLECharacteristic::PROPERTY_READ | BLECharacteristic::PROPERTY_WRITE
  );
  
  //  valore di default per la caratteristica che verrà restituito al client in caso di lettura della caratteristica
  pCharacteristic->setValue("Ciao Alessandro!");
  
  // Colleghiamo la callback alla caratteristica
  pCharacteristic->setCallbacks(new MyCallbacks());
  
  // Avviamo il servizio BLE
  pService->start();
  
  BLEAdvertisementData advData;
  advData.setName("EcoEye Glasses"); // mi è utile per visualizzare il nome del dispositivo BLE durante una scansione dei dispositivi vicini

  // Avvio dell'advertising BLE
  BLEAdvertising *pAdvertising = BLEDevice::getAdvertising();
  pAdvertising->setAdvertisementData(advData);    // Imposta i dati di advertising
  pAdvertising->addServiceUUID(SERVICE_UUID);
  pAdvertising->setScanResponse(true);
  // Queste impostazioni aiutano a risolvere alcuni problemi di connessione su dispositivi iOS
  pAdvertising->setMinPreferred(0x06);
  pAdvertising->setMinPreferred(0x12);
  BLEDevice::startAdvertising();

  // Stampiamo un messaggio per confermare che il BLE server è attivo
  Serial.println("Il BLE Server ha inizato a funzionare. Invia del nuovo testo dal tuo dispositivo.");
}


void loop() {
  delay(2000);
}

