# Smart Farm Monitoring and Disease Prediction System

Java Swing desktop application for smart farm sensor monitoring, weather prediction, analytics, pump automation, alerts, and AI-based plant disease prediction.

## Run

```powershell
cd C:\Users\admin\OneDrive\Desktop\java\SmartFarmSystem
javac *.java
java Login
```

Or double-click `run.bat`.

If Maven is installed, you can also use:

```powershell
mvn compile exec:java
```

Demo login:

```text
admin / admin123
```

## Optional integrations

- MySQL: import `database_schema.sql`, add MySQL Connector/J to the classpath, then edit `config.properties`.
- OpenWeather: set `openweather.key` and `weather.city`.
- Twilio SMS: set `twilio.sid`, `twilio.token`, `twilio.from`, and `twilio.to`.
- FreeTTS: add FreeTTS jars to the classpath. The app uses FreeTTS automatically when available.
- JFreeChart: add JFreeChart jars to the classpath. The graph module automatically switches from the built-in chart to JFreeChart when available.
- TensorFlow/Keras: replace `AIModel/model.h5.placeholder` with `AIModel/model.h5` and update `AIModel/predict.py` class labels/preprocessing for your trained model.

## Features

- Attractive login page and modern dashboard
- Random simulated sensor readings
- Automatic pump ON/OFF control based on soil moisture
- Live or demo weather display
- Soil moisture, temperature, and water usage analytics
- SMS and voice alert hooks
- Leaf image upload preview
- Disease result with cause, pesticide, and prevention tips
- MySQL persistence hooks with demo fallback
