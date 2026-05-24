import os
import sys


CATALOG = {
    "rust": (
        "Leaf Rust",
        "Fungal spores spread quickly in humid weather.",
        "Propiconazole spray",
        "Improve airflow, remove infected leaves, and avoid overhead irrigation.",
    ),
    "blight": (
        "Early Blight",
        "Alternaria fungus usually affects older leaves first.",
        "Mancozeb or copper fungicide",
        "Rotate crops, mulch soil, and keep leaves dry.",
    ),
    "mildew": (
        "Powdery Mildew",
        "White fungal growth encouraged by crowded plants.",
        "Sulfur-based fungicide",
        "Use resistant varieties and prune crowded foliage.",
    ),
}


def predict_with_tensorflow(image_path):
    # Replace this demo block with a real Keras model when model.h5 is available:
    # import tensorflow as tf
    # model = tf.keras.models.load_model(os.path.join(os.path.dirname(__file__), "model.h5"))
    # image = tf.keras.utils.load_img(image_path, target_size=(224, 224))
    # arr = tf.keras.utils.img_to_array(image)[None, ...] / 255.0
    # index = int(model.predict(arr).argmax())
    # return class_catalog[index]
    filename = os.path.basename(image_path).lower()
    for key, result in CATALOG.items():
        if key in filename:
            return result
    return (
        "Healthy / No Severe Disease",
        "No strong disease pattern was detected in demo mode.",
        "No pesticide required",
        "Continue monitoring moisture, temperature, and leaf color.",
    )


if __name__ == "__main__":
    image = sys.argv[1] if len(sys.argv) > 1 else ""
    print("|".join(predict_with_tensorflow(image)))
