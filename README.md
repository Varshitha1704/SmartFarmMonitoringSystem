<<<<<<< HEAD
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
Farmer: farmer / farmer123
Admin: admin / admin123
```

## Optional integrations

- MySQL: import `database_schema.sql`, add MySQL Connector/J to the classpath, then edit `config.properties`.
- OpenWeather: set `openweather.key` and `weather.city`.
- Twilio SMS: set `twilio.sid`, `twilio.token`, `twilio.from`, and `twilio.to`.
- FreeTTS: add FreeTTS jars to the classpath. The app uses FreeTTS automatically when available.
- JFreeChart: add JFreeChart jars to the classpath. The graph module automatically switches from the built-in chart to JFreeChart when available.
- TensorFlow/Keras: replace `AIModel/model.h5.placeholder` with `AIModel/model.h5` and update `AIModel/predict.py` class labels/preprocessing for your trained model.
- AI model: install Python requirements from `AIModel/requirements.txt`, put training images in `AIModel/dataset/<Disease Name>/`, then run `python AIModel/train_model.py`. The app will use `AIModel/model.keras` automatically.
- If predictions keep falling back to suspected/healthy demo results, install Python and TensorFlow, then set `python.command` in `config.properties` to the exact interpreter command. Use forward slashes for Windows paths, for example `C:/Users/admin/AppData/Local/Programs/Python/Python311/python.exe`.

## Features

- Attractive login page and modern dashboard
- Separate farmer and admin login pages
- Farmer registration portal with profile details
- Random simulated sensor readings
- Automatic pump ON/OFF control based on soil moisture
- Live or demo weather display
- Soil moisture, temperature, and water usage analytics
- SMS and voice alert hooks
- Leaf image upload preview
- Disease result with cause, pesticide, and prevention tips
- MySQL persistence hooks with demo fallback
=======
# SmartFarmMonitoringSystem
AI-based Smart Farm Monitoring System using Java, TensorFlow, and Python
>>>>>>> cb8c5b453dad939ce6d09096fccbc8557bd39acc
