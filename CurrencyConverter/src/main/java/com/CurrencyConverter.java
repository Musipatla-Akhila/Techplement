package com;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class CurrencyConverter {
    private static final String API_BASE_URL = "https://api.exchangerate.host/convert";
    private static final Map<String, BigDecimal> exchangeRatesCache = new HashMap<>();

    // Assuming you've obtained an API key and it's required now
    private static final String API_KEY = "1b18c87c5b77683adca1b170a90af942";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            displayOptions();

            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume the newline

            switch (choice) {
                case 1:
                    addFavoriteCurrency(scanner);
                    break;
                case 2:
                    viewFavoriteCurrencies();
                    break;
                case 3:
                    convertCurrency(scanner);
                    break;
                case 4:
                    System.out.println("Exiting...");
                    System.exit(0);
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    private static void displayOptions() {
        System.out.println("Options:");
        System.out.println("1. Add Favorite Currency");
        System.out.println("2. View Favorite Currencies");
        System.out.println("3. Convert Currency");
        System.out.println("4. Exit");
        System.out.print("Select an option: ");
    }

    private static void addFavoriteCurrency(Scanner scanner) {
        System.out.print("Enter the currency code to add to favorites (e.g., USD): ");
        String currencyCode = scanner.nextLine().toUpperCase();
        BigDecimal exchangeRate = getExchangeRate(currencyCode);

        if (exchangeRate != null) {
            exchangeRatesCache.put(currencyCode, exchangeRate);
            System.out.println(currencyCode + " added to favorites.");
        } else {
            System.out.println("Error fetching exchange rate. Please check your input and try again.");
        }
    }

    private static void viewFavoriteCurrencies() {
        System.out.println("Favorite Currencies:");
        for (Map.Entry<String, BigDecimal> entry : exchangeRatesCache.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
    }

    private static void convertCurrency(Scanner scanner) {
        System.out.print("Enter the amount to convert: ");
        BigDecimal amount = scanner.nextBigDecimal();

        System.out.print("Enter the source currency code: ");
        String sourceCurrency = scanner.next().toUpperCase();

        System.out.print("Enter the target currency code: ");
        String targetCurrency = scanner.next().toUpperCase();

        BigDecimal sourceRate = exchangeRatesCache.get(sourceCurrency);
        BigDecimal targetRate = exchangeRatesCache.get(targetCurrency);

        if (sourceRate != null && targetRate != null) {
            // Perform the conversion
            BigDecimal result = amount.multiply(targetRate).divide(sourceRate, 2, BigDecimal.ROUND_HALF_UP);
            System.out.println(amount + " " + sourceCurrency + " is equal to " + result + " " + targetCurrency);
        } else {
            // Handle the case where one or both rates are not found
            System.out.println("Error: Exchange rates not found for the specified currencies.");
        }
    }


    private static BigDecimal getExchangeRate(String currencyCode) {
        try {
            String apiUrl = API_BASE_URL + "?base=" + currencyCode;
            JSONObject response = makeApiRequest(apiUrl);

            if (response.has("rates")) {
                return response.getJSONObject("rates").getBigDecimal(currencyCode);
            } else {
                throw new IOException("Key 'rates' not found in the JSON response");
            }
        } catch (IOException e) {
            System.out.println("Error fetching exchange rate data: " + e.getMessage());
            return null;
        }
    }

    private static JSONObject makeApiRequest(String url) throws IOException {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected HTTP response: " + response);
            }

            String responseBody = response.body().string();
            return new JSONObject(responseBody);
        }
    }
}
