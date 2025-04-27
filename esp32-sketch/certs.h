#ifndef CERT_H
#define CERT_H

#include <pgmspace.h>

#define THINGNAME "EcoEye_esp32" 

const char WIFI_SSID[] = "__WIFI_SSID__" ;
const char WIFI_PASSWORD[] = "__WIFI_PASSWORD__";

const char AWS_IOT_ENDPOINT[] = "__AWS_IOT_ENDPOINT__" ;

// Amazon Root CA 1 : 
static const char AWS_CERT_CA[] PROGMEM = R"EOF(__AWS_CERT_CA__)EOF";

// Device Certificate
static const char AWS_CERT_CRT[] PROGMEM = R"KEY(__AWS_CERT_CRT__)KEY";

// Device Private key
static const char AWS_CERT_PRIVATE[] PROGMEM = R"KEY(__AWS_CERT_PRIVATE__)KEY";
#endif