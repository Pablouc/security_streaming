const int ledPin = 13; // Pin connected to the LED

void setup() {
  pinMode(ledPin, OUTPUT); // Set the LED pin as an output
  Serial.begin(9600); // Initialize serial communication
}

void loop() {
  if (Serial.available() > 0) {
    char command = Serial.read(); // Read the incoming command

    if (command == '1') {
      digitalWrite(ledPin, HIGH); // Turn on the LED
      Serial.println("LED is ON");
    } else if (command == '0') {
      digitalWrite(ledPin, LOW); // Turn off the LED
      Serial.println("LED is OFF");
    }
  }
}
