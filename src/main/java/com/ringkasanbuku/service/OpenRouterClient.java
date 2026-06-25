package com.ringkasanbuku.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class OpenRouterClient {

  private final String apiKey;

  public OpenRouterClient(String apiKey) {
    this.apiKey = apiKey;
  }

  public String summarize(String text, String lengthOption) throws Exception {

    String percentage = "35%";
    if ("Biasa".equalsIgnoreCase(lengthOption))
      percentage = "20%";
    else if ("Tinggi".equalsIgnoreCase(lengthOption))
      percentage = "50%";

    String prompt = """
        Buat ringkasan yang detail dan informatif dari teks berikut.

        Aturan:
        - Buatlah ringkasan dengan panjang sekitar %s dari teks asli.
        - Pastikan ringkasan mencakup poin-poin penting berikut (jika ada dalam teks):
          1. Topik utama atau gagasan inti.
          2. Penjelasan pendukung yang relevan.
          3. Manfaat atau keunggulan.
          4. Masalah, tantangan, atau kekurangan.
          5. Kesimpulan akhir.
        - Catatan penting, jangan tampilkan ketentuannya.

        Teks:
        %s
        """.formatted(percentage, text);

    String body = """
        {
          "model": "google/gemma-3-4b-it",
          "messages": [
            {
              "role": "user",
              "content": "%s"
            }
          ]
        }
        """.formatted(
        prompt.replace("\"", "\\\"")
            .replace("\n", "\\n"));

    System.out.println("Authorization = Bearer " + apiKey);

    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create("https://openrouter.ai/api/v1/chat/completions"))
        .header("Authorization", "Bearer " + apiKey)
        .header("Content-Type", "application/json")
        .POST(HttpRequest.BodyPublishers.ofString(body))
        .build();

    HttpClient client = HttpClient.newHttpClient();

    HttpResponse<String> response = client.send(request,
        HttpResponse.BodyHandlers.ofString());

    ObjectMapper mapper = new ObjectMapper();

    JsonNode root = mapper.readTree(response.body());

    String summary = root
        .get("choices")
        .get(0)
        .get("message")
        .get("content")
        .asText();

    return summary;
  }
}