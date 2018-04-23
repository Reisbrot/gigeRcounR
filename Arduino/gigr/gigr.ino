  int f = 100000;
  int geygrValue = 0;
  int i = 0;
 unsigned long previousMillis = 0; // last time update
 long interval = 2000; // interval at which to do something (milliseconds)

void setup()
{
  Serial.begin(9600);
  pinMode(13, OUTPUT);
  pinMode(2, INPUT_PULLUP);
  attachInterrupt(digitalPinToInterrupt(2), dingdong, CHANGE);
}

void loop()
{
  unsigned long currentMillis = millis();
  if(currentMillis - previousMillis > interval) {
     previousMillis = currentMillis;
     spamSerial();
  }
  i++;
  //Serial.println(f);

  digitalWrite(13, HIGH);
  delayMicroseconds(24000); // Approximately 10% duty cycle @ 1KHz
  digitalWrite(13, LOW);
  delayMicroseconds(24000);
  f = f -10;

  
  //geygrValue = analogRead(A0);
  //if(i%10==0)
  //Serial.println(geygrValue);
  
}

void dingdong(){
  Serial.println("DingDong");
  geygrValue++;
}

void spamSerial(){
  //Serial.println(geygrValue);
  geygrValue = 0;
}

