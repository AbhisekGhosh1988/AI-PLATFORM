from typing import List

from fastapi import FastAPI
from pydantic import BaseModel

from services.anomaly_service import (
    train_model,
    predict_anomaly
)

app = FastAPI(
    title="AI Anomaly Detection Service"
)


class MetricData(BaseModel):

    error_count: int
    warn_count: int
    critical_count: int
    avg_response_time: float
    unique_exception_count: int


@app.get("/health")
def health():

    return {
        "status": "UP"
    }


@app.post("/train")
def train(data: List[MetricData]):

    records = [d.dict() for d in data]

    return train_model(records)


@app.post("/predict")
def predict(data: MetricData):

    return predict_anomaly(data.dict())