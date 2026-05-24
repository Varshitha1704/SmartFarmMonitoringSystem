import os
import sys


BASE_DIR = os.path.dirname(os.path.abspath(__file__))
MODEL_PATH = os.path.join(BASE_DIR, "model.keras")
LABELS_PATH = os.path.join(BASE_DIR, "labels.txt")
DATASET_DIR = os.path.join(BASE_DIR, "dataset")


def main():
    print("Python:", sys.executable)
    print("Model exists:", os.path.exists(MODEL_PATH), MODEL_PATH)
    try:
        import tensorflow as tf
        print("TensorFlow:", tf.__version__)
        custom_objects = {
            "preprocess_input": tf.keras.applications.mobilenet_v2.preprocess_input
        }
        try:
            model = tf.keras.models.load_model(
                MODEL_PATH,
                safe_mode=False,
                custom_objects=custom_objects,
            )
        except TypeError:
            model = tf.keras.models.load_model(MODEL_PATH, custom_objects=custom_objects)
        print("Model loaded successfully.")
        print("Model output shape:", model.output_shape)
        if os.path.exists(LABELS_PATH):
            with open(LABELS_PATH, "r", encoding="utf-8") as handle:
                labels = [line.strip() for line in handle if line.strip()]
            print("labels.txt:", labels)
            output_count = model.output_shape[-1]
            if len(labels) != output_count:
                print(f"WARNING: labels.txt has {len(labels)} labels but model outputs {output_count} classes.")
        if os.path.isdir(DATASET_DIR):
            folders = sorted(
                name for name in os.listdir(DATASET_DIR)
                if os.path.isdir(os.path.join(DATASET_DIR, name))
            )
            print("dataset folders sorted:", folders)
    except Exception as exc:
        print("AI setup failed:", exc)
        sys.exit(1)


if __name__ == "__main__":
    main()
