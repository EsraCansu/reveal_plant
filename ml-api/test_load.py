import tensorflow as tf
print(f"TensorFlow: {tf.__version__}")

from tensorflow.keras.models import load_model
import pathlib

model_path = pathlib.Path(__file__).parent / "model" / "PlantVillage_Resnet101_FineTuning.keras"
print(f"Model path: {model_path}")
print(f"Model exists: {model_path.exists()}")
print(f"Model size: {model_path.stat().st_size / (1024*1024):.2f} MB")

try:
    print("\nLoading model...")
    model = load_model(str(model_path))
    print("✅ MODEL LOADED!")
    print(f"Output shape: {model.output_shape}")
except Exception as e:
    print(f"❌ ERROR: {e}")
    import traceback
    traceback.print_exc()
