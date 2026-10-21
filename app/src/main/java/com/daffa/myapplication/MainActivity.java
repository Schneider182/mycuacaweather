package com.daffa.myapplication;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private EditText inputKota;
    private Button btnCek;
    private TextView txtHasil;

    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();

    private static final String API_KEY = "84d62e9777e4e706e3771c9a4561ada0";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        inputKota = findViewById(R.id.inputKota);
        btnCek = findViewById(R.id.btnCek);
        txtHasil = findViewById(R.id.txtHasil);

        btnCek.setOnClickListener(v -> {
            String kota = inputKota.getText().toString().trim();

            if (kota.isEmpty()) {
                inputKota.setError("Nama kota harus diisi");
                return;
            }

            txtHasil.setText("Mengambil data cuaca...");
            btnCek.setEnabled(false);

            executor.execute(() -> ambilCuaca(kota));
        });
    }

    private void ambilCuaca(String kota) {
        HttpURLConnection connection = null;

        try {
            String kotaEncoded = URLEncoder.encode(
                    kota, StandardCharsets.UTF_8.name());

            String alamat =
                    "https://api.openweathermap.org/data/2.5/weather"
                            + "?q=" + kotaEncoded
                            + "&appid=" + API_KEY
                            + "&units=metric&lang=id";

            URL url = new URL(alamat);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);

            int status = connection.getResponseCode();

            InputStream stream = status >= 200 && status < 300
                    ? connection.getInputStream()
                    : connection.getErrorStream();

            StringBuilder response = new StringBuilder();

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(
                            stream, StandardCharsets.UTF_8))) {
                String baris;

                while ((baris = reader.readLine()) != null) {
                    response.append(baris);
                }
            }

            if (status != 200) {
                String pesan = "Request gagal. HTTP " + status;

                try {
                    JsonObject error = JsonParser
                            .parseString(response.toString())
                            .getAsJsonObject();

                    if (error.has("message")) {
                        pesan = error.get("message").getAsString();
                    }
                } catch (Exception ignored) {
                }

                tampilkanHasil("Error: " + pesan);
                return;
            }

            JsonObject data = JsonParser
                    .parseString(response.toString())
                    .getAsJsonObject();

            String namaKota = data.get("name").getAsString();

            JsonObject main = data.getAsJsonObject("main");
            double suhu = main.get("temp").getAsDouble();
            double terasa = main.get("feels_like").getAsDouble();
            int kelembapan = main.get("humidity").getAsInt();

            JsonArray weather = data.getAsJsonArray("weather");
            String kondisi = weather.get(0)
                    .getAsJsonObject()
                    .get("description")
                    .getAsString();

            JsonObject wind = data.getAsJsonObject("wind");
            double kecepatanAngin = wind.get("speed").getAsDouble();

            String hasil = String.format(
                    Locale.getDefault(),
                    "Kota: %s\n\n"
                            + "Kondisi: %s\n"
                            + "Suhu: %.1f °C\n"
                            + "Terasa seperti: %.1f °C\n"
                            + "Kelembapan: %d%%\n"
                            + "Kecepatan angin: %.1f m/s",
                    namaKota, kondisi, suhu,
                    terasa, kelembapan, kecepatanAngin
            );

            tampilkanHasil(hasil);

        } catch (Exception e) {
            tampilkanHasil(
                    "Gagal mengambil data. Periksa koneksi internet dan API key."
            );
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private void tampilkanHasil(String hasil) {
        runOnUiThread(() -> {
            txtHasil.setText(hasil);
            btnCek.setEnabled(true);
        });
    }

    @Override
    protected void onDestroy() {
        executor.shutdownNow();
        super.onDestroy();
    }
}
