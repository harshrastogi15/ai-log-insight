from fastapi import FastAPI
from pydantic import BaseModel
from sentence_transformers import SentenceTransformer

app = FastAPI()

# Load model once on startup — not on every request
model = SentenceTransformer("all-MiniLM-L6-v2")  # 384 dims, only 80MB

class EmbedRequest(BaseModel):
    text: str

class EmbedResponse(BaseModel):
    embedding: list[float]
    dimensions: int

@app.post("/embed", response_model=EmbedResponse)
def embed(request: EmbedRequest):
    vector = model.encode(request.text).tolist()
    return EmbedResponse(embedding=vector, dimensions=len(vector))

@app.get("/health")
def health():
    return {"status": "ok", "model": "all-MiniLM-L6-v2"}