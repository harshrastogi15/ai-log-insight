package log_parser_service.service;


public interface EmbeddingClient {

    /**
     * @param text input text to embed
     * @return float[] vector representation
     */
    float[] embed(String text);

    /**
     * Converts float[] to pgvector literal: "[0.1,0.2,...]"
     */
    default String toVectorLiteral(float[] vector) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < vector.length; i++) {
            sb.append(vector[i]);
            if (i < vector.length - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }
}