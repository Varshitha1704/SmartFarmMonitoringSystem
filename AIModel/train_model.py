import os

import tensorflow as tf


BASE_DIR = os.path.dirname(os.path.abspath(__file__))
DATA_DIR = os.path.join(BASE_DIR, "dataset")
MODEL_OUT = os.path.join(BASE_DIR, "model.keras")
LABELS_OUT = os.path.join(BASE_DIR, "labels.txt")
IMAGE_SIZE = (224, 224)
BATCH_SIZE = 16
EPOCHS = 8


def main():
    if not os.path.isdir(DATA_DIR):
        raise SystemExit(
            "Dataset folder not found. Create AIModel/dataset/<Disease Name>/image.jpg folders first."
        )

    train_ds = tf.keras.utils.image_dataset_from_directory(
        DATA_DIR,
        validation_split=0.2,
        subset="training",
        seed=42,
        image_size=IMAGE_SIZE,
        batch_size=BATCH_SIZE,
    )
    val_ds = tf.keras.utils.image_dataset_from_directory(
        DATA_DIR,
        validation_split=0.2,
        subset="validation",
        seed=42,
        image_size=IMAGE_SIZE,
        batch_size=BATCH_SIZE,
    )

    class_names = train_ds.class_names
    with open(LABELS_OUT, "w", encoding="utf-8") as handle:
        handle.write("\n".join(class_names) + "\n")

    train_ds = train_ds.prefetch(tf.data.AUTOTUNE)
    val_ds = val_ds.prefetch(tf.data.AUTOTUNE)

    base = tf.keras.applications.MobileNetV2(
        input_shape=IMAGE_SIZE + (3,),
        include_top=False,
        weights="imagenet",
    )
    base.trainable = False

    model = tf.keras.Sequential(
        [
            tf.keras.layers.Rescaling(1.0 / 127.5, offset=-1, input_shape=IMAGE_SIZE + (3,)),
            tf.keras.layers.RandomFlip("horizontal"),
            tf.keras.layers.RandomRotation(0.08),
            base,
            tf.keras.layers.GlobalAveragePooling2D(),
            tf.keras.layers.Dropout(0.25),
            tf.keras.layers.Dense(len(class_names), activation="softmax"),
        ]
    )
    model.compile(
        optimizer=tf.keras.optimizers.Adam(learning_rate=0.0005),
        loss="sparse_categorical_crossentropy",
        metrics=["accuracy"],
    )
    model.fit(train_ds, validation_data=val_ds, epochs=EPOCHS)
    model.save(MODEL_OUT)
    print(f"Saved model: {MODEL_OUT}")
    print(f"Saved labels: {LABELS_OUT}")


if __name__ == "__main__":
    main()
