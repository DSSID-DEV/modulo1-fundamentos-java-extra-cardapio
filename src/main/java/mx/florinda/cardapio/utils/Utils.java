package mx.florinda.cardapio.utils;

import java.util.HashMap;
import java.util.Map;

public class Utils {


    public static Map<String, Object> extractVariables(String endpoint) {
        if (endpoint == null || endpoint.trim().isEmpty()) {
            return new HashMap<>();
        }

        Map<String, Object> result = new HashMap<>();

        String cleaned = endpoint.trim();

        String pathPart;
        String queryPart = null;

        int indiceDeInterrogacao = cleaned.indexOf('?');
        if (indiceDeInterrogacao != -1) {
            pathPart = cleaned.substring(0, indiceDeInterrogacao);
            queryPart = cleaned.substring(indiceDeInterrogacao + 1);
        } else {
            pathPart = cleaned;
        }

        if (pathPart.startsWith("/")) {
            pathPart = pathPart.substring(1);
        }

        String[] paths = pathPart.split("/");
        int indice = 1;

        for (String segmento : paths) {
            if (!segmento.isEmpty() && isLikelyVariable(segmento)) {
               result.put("path" + indice, segmento);
               indice++;
            }
        }

        if (queryPart != null && !queryPart.isEmpty()) {
            String[] pairs = queryPart.split("&");
            for (String pair : pairs) {
                if (pair.contains("=")) {
                    String[] keyValue = pair.split("=", 2);
                    String key = keyValue[0].trim();
                    String value = keyValue.length > 1 ? keyValue[1].trim() : "";
                    result.put(key, value);
                } else if (!pair.isEmpty()) {
                    result.put(pair.trim(), "true");
                }
            }
        }

        return result;
    }

    private static boolean isLikelyVariable(String segmento) {
        if (segmento == null || segmento.isEmpty()) {
            return false;
        }

        if (segmento.matches("^[0-9a-fA-F]{8}(-?[0-9a-fA-F]{4}){3}-?[0-9a-fA-F]{12}$")) {
            return true;
        }

        return segmento.matches("^\\d+$");
    }
}
