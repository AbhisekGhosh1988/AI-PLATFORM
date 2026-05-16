import os
import joblib
import pandas as pd

from sklearn.ensemble import IsolationForest

MODEL_PATH = "model/isolation_model.pkl"

FEATURES = [
    "error_count",
    "warn_count",
    "critical_count",
    "avg_response_time",
    "unique_exception_count"
]

def train_model(data):

    try:

        df = pd.DataFrame(data)

        if len(df) < 5:
            return {
                "status": "failed",
                "error": "Minimum 5 records required"
            }

        model = IsolationForest(
            contamination=0.02,
            random_state=42
        )

        model.fit(df[FEATURES])

        os.makedirs("model", exist_ok=True)

        joblib.dump(model, MODEL_PATH)

        return {
            "status": "trained",
            "records": len(df)
        }

    except Exception as e:

        return {
            "status": "failed",
            "error": str(e)
        }


def predict_anomaly(data):

    try:

        if not os.path.exists(MODEL_PATH):

            return {
                "status": "failed",
                "error": "Model not trained"
            }

        model = joblib.load(MODEL_PATH)

        df = pd.DataFrame([data])

        prediction = int(
            model.predict(df[FEATURES])[0]
        )

        score = float(
            model.decision_function(df[FEATURES])[0]
        )

        anomaly = prediction == -1

        reasons = []

        if data["error_count"] > 50:
            reasons.append(
                "High error count"
            )

        if data["avg_response_time"] > 2000:
            reasons.append(
                "High response time"
            )

        if data["critical_count"] > 10:
            reasons.append(
                "Critical errors increased"
            )

        if not reasons:
            reasons.append(
                "Normal behavior"
            )

        return {
            "anomaly": bool(anomaly),
            "score": float(score),
            "reasons": reasons
        }

    except Exception as e:

        return {
            "status": "failed",
            "error": str(e)
        }