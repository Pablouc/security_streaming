#include "pins_arduino.h"
const int ledPin = 13; // Pin connected to the LED

volatile byte operation;
volatile boolean process_it;
bool report_error;

void setup (void)
{
  Serial.begin (9600);   // debugging
  pinMode(ledPin, OUTPUT); // Set the LED pin as an output
  Serial.begin(9600); // Initialize serial communication

  // have to send on master in, *slave out*
  pinMode(MISO, OUTPUT);
  
  // turn on SPI in slave mode
  SPCR |= _BV(SPE);
  
  // turn on interrupts
  SPCR |= _BV(SPIE);
  
  operation = 0;
  process_it = false;
}  // end of setup


// SPI interrupt routine
ISR (SPI_STC_vect)
{
  operation = SPDR;
  Serial.println("Received something...");
  process_it = true;
}

// main loop - wait for flag set in interrupt routine
void loop (void)
{
  delay(1000);
  if(process_it) {
    report_error = false;
    switch (operation) {
      case 10:
        digitalWrite(ledPin, HIGH); // Turn on the LED
        Serial.println("LED is ON");
        break;
      case 1:
        digitalWrite(ledPin, LOW); // Turn off the LED
        Serial.println("LED is OFF");
        break;
      default:
        Serial.println("Non expected value");
        Serial.println(operation);
        report_error = true;
        break;
    }
    process_it = false;
    SPDR = 10;
    if (report_error) {
      SPDR = 1;
    }
  }
}  // end of loop
