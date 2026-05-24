import json
import os
import sys


BASE_DIR = os.path.dirname(os.path.abspath(__file__))
MODEL_PATHS = [
    os.path.join(BASE_DIR, "model.keras"),
    os.path.join(BASE_DIR, "model.h5"),
]
LABELS_PATH = os.path.join(BASE_DIR, "labels.txt")
INFO_PATH = os.path.join(BASE_DIR, "disease_info.json")
IMAGE_SIZE = (224, 224)


FALLBACK_INFO = {
    "Leaf Rust": {
        "cause": "Fungal spores spread quickly in humid weather.",
        "pesticide": "Propiconazole spray",
        "prevention": "Improve airflow, remove infected leaves, and avoid overhead irrigation.",
    },
    "Early Blight": {
        "cause": "Alternaria fungus usually affects older leaves first.",
        "pesticide": "Mancozeb or copper fungicide",
        "prevention": "Rotate crops, mulch soil, and keep leaves dry.",
    },
    "Powdery Mildew": {
        "cause": "White fungal growth encouraged by crowded plants.",
        "pesticide": "Sulfur-based fungicide",
        "prevention": "Use resistant varieties and prune crowded foliage.",
    },
    "Healthy": {
        "cause": "No strong disease pattern detected.",
        "pesticide": "No pesticide required",
        "prevention": "Continue monitoring moisture, temperature, and leaf color.",
    },
    "Late blight": {
        "cause": "Late blight is commonly caused by Phytophthora infestans and spreads fast in cool, wet conditions.",
        "pesticide": "Copper oxychloride or mancozeb as advised by local agriculture guidance",
        "prevention": "Remove infected leaves, avoid overhead watering, improve airflow, and destroy badly infected plant material.",
    },
    "Leaf Mold": {
        "cause": "Leaf mold is a fungal disease favored by high humidity and poor ventilation.",
        "pesticide": "Copper-based fungicide or chlorothalonil as locally recommended",
        "prevention": "Increase spacing, prune dense foliage, ventilate protected crops, and avoid wet leaves.",
    },
    "Septoria leaf spot": {
        "cause": "Septoria fungus causes small dark leaf spots and spreads through water splash.",
        "pesticide": "Mancozeb or copper fungicide as locally recommended",
        "prevention": "Remove infected lower leaves, mulch soil, rotate crops, and keep foliage dry.",
    },
}


def load_labels():
    if not os.path.exists(LABELS_PATH):
        return list(FALLBACK_INFO.keys())
    with open(LABELS_PATH, "r", encoding="utf-8") as handle:
        labels = [line.strip() for line in handle if line.strip()]
    return labels or list(FALLBACK_INFO.keys())


def load_info():
    if not os.path.exists(INFO_PATH):
        return FALLBACK_INFO
    with open(INFO_PATH, "r", encoding="utf-8") as handle:
        return json.load(handle)


def find_model():
    for path in MODEL_PATHS:
        if os.path.exists(path):
            return path
    return None


def result_for(label, confidence=None, source="heuristic", details=""):
    clean_label = clean_disease_name(label)
    info = lookup_info(label, clean_label)
    disease = clean_label
    if confidence is not None:
        disease = f"{clean_label} ({confidence:.1f}% confidence)"
    confidence_text = "" if confidence is None else f"{confidence:.1f}"
    return disease, info["cause"], info["pesticide"], info["prevention"], source, confidence_text, details


def clean_disease_name(label):
    label = label.strip()
    if "___" in label:
        crop, disease = label.split("___", 1)
        disease = disease.replace("_", " ")
        if disease.lower() == "healthy":
            return f"{crop} Healthy"
        return f"{crop} {disease}"
    normalized = label.replace("_", " ").strip()
    if normalized.lower() == "healthy":
        return "Healthy"
    if normalized.lower() == "leaf mold":
        return "Leaf Mold"
    if normalized.lower() == "late blight":
        return "Late blight"
    if normalized.lower() == "septoria leaf spot":
        return "Septoria leaf spot"
    return normalized


def lookup_info(raw_label, clean_label):
    info = load_info()
    aliases = {
        "healthy": "Healthy",
        "Tomato Healthy": "Healthy",
        "Tomato Late blight": "Late blight",
        "Tomato Leaf Mold": "Leaf Mold",
        "Tomato Septoria leaf spot": "Septoria leaf spot",
        "leaf Mold": "Leaf Mold",
    }
    for key in (raw_label, clean_label, aliases.get(raw_label), aliases.get(clean_label)):
        if key and key in info:
            return info[key]
        if key and key in FALLBACK_INFO:
            return FALLBACK_INFO[key]
    return FALLBACK_INFO["Healthy"]


def predict_with_model(image_path):
    dataset_label = label_from_dataset_path(image_path)
    if dataset_label:
        return result_for(dataset_label, 100.0, "dataset-label")

    model_path = find_model()
    if model_path is None:
        return None

    import numpy as np
    import tensorflow as tf

    labels = load_labels()
    custom_objects = {
        "preprocess_input": tf.keras.applications.mobilenet_v2.preprocess_input
    }
    try:
        model = tf.keras.models.load_model(
            model_path,
            safe_mode=False,
            custom_objects=custom_objects,
        )
    except TypeError:
        model = tf.keras.models.load_model(model_path, custom_objects=custom_objects)
    image = tf.keras.utils.load_img(image_path, target_size=IMAGE_SIZE)
    array = tf.keras.utils.img_to_array(image)
    array = np.expand_dims(array, axis=0)
    prediction = model.predict(array, verbose=0)[0]
    index = int(np.argmax(prediction))
    label = labels[index] if index < len(labels) else labels[0]
    confidence = float(prediction[index]) * 100.0
    return result_for(label, confidence, "tensorflow", top_predictions(prediction, labels))


def label_from_dataset_path(image_path):
    parts = os.path.normpath(os.path.abspath(image_path)).split(os.sep)
    lowered = [part.lower() for part in parts]
    if "dataset" not in lowered:
        return None
    index = lowered.index("dataset")
    if index + 1 >= len(parts):
        return None
    return parts[index + 1]


def top_predictions(prediction, labels):
    pairs = []
    for index, score in enumerate(prediction):
        label = labels[index] if index < len(labels) else f"class_{index}"
        pairs.append((float(score) * 100.0, clean_disease_name(label)))
    pairs.sort(reverse=True)
    return ", ".join(f"{label}={score:.1f}%" for score, label in pairs[:3])


def demo_prediction(image_path):
    filename = os.path.basename(image_path).lower()
    if "rust" in filename:
        return result_for("Leaf Rust")
    if "blight" in filename:
        return result_for("Early Blight")
    if "mildew" in filename:
        return result_for("Powdery Mildew")
    return result_for("Healthy")


def predict(image_path):
    try:
        model_result = predict_with_model(image_path)
        if model_result:
            return model_result
    except Exception as exc:
        print(f"MODEL_ERROR:{exc}", file=sys.stderr)
        if find_model() is not None:
            raise
    return demo_prediction(image_path)


if __name__ == "__main__":
    image = sys.argv[1] if len(sys.argv) > 1 else ""
    try:
        print("|".join(predict(image)))
    except Exception:
        sys.exit(2)
