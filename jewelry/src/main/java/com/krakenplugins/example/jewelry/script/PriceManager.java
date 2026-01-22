package com.krakenplugins.example.jewelry.script;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Singleton
public class PriceManager {

    private static final String API_BASE = "https://prices.runescape.wiki/api/v1/osrs/latest";
    private static final String USER_AGENT = "PriceManager-API/1.0";

    private final OkHttpClient okHttpClient;
    private final Gson gson;

    private final Map<Integer, ItemPrice> priceCache = new ConcurrentHashMap<>();

    @Inject
    public PriceManager(OkHttpClient okHttpClient, Gson gson) {
        this.okHttpClient = okHttpClient;
        this.gson = gson;
    }

    /**
     * Retrieves the price for a specific item.
     * <p>
     * 1. Checks the local cache.
     * 2. If missing, performs a BLOCKING network request for that specific item.
     * </p>
     * * @param itemId The OSRS Item ID
     * @return ItemPrice or null if the item has no trade data/fails to load.
     * @throws RuntimeException if called on the main client thread (optional safety check you could add)
     */
    public ItemPrice getItemPrice(int itemId) {
        if (priceCache.containsKey(itemId)) {
            return priceCache.get(itemId);
        }

        log.debug("Item {} missing from cache, fetching...", itemId);
        return fetchSingleItem(itemId);
    }

    /**
     * Synchronous lookup for a single item using the 'id' query parameter.
     */
    private ItemPrice fetchSingleItem(int itemId) {
        HttpUrl url = HttpUrl.parse(API_BASE).newBuilder()
                .addQueryParameter("id", String.valueOf(itemId))
                .build();

        Request request = new Request.Builder()
                .url(url)
                .header("User-Agent", USER_AGENT)
                .build();

        try (Response response = okHttpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                log.warn("Failed to lookup item {}: HTTP {}", itemId, response.code());
                return null;
            }

            if (response.body() == null) return null;

            String jsonString = response.body().string();
            parseAndCache(jsonString);

            return priceCache.get(itemId);
        } catch (IOException e) {
            log.error("Network error looking up item {}", itemId, e);
            return null;
        }
    }

    /**
     * Asynchronously fetches prices for ALL items to populate the cache.
     * Useful for plugin startup.
     */
    public void refreshAllPrices() {
        Request request = new Request.Builder()
                .url(API_BASE) // No ID param = All items
                .header("User-Agent", USER_AGENT)
                .build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                log.warn("Failed to bulk fetch prices", e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    response.close();
                    return;
                }
                try (var body = response.body()) {
                    if (body != null) {
                        parseAndCache(body.string());
                        log.debug("Bulk price refresh complete. Cache size: {}", priceCache.size());
                    }
                }
            }
        });
    }

    /**
     * Shared parsing logic for both Bulk and Single responses.
     */
    private void parseAndCache(String jsonString) {
        try {
            JsonObject root = gson.fromJson(jsonString, JsonObject.class);
            if (!root.has("data")) return;

            JsonObject dataObject = root.getAsJsonObject("data");
            Type type = new TypeToken<Map<String, WikiPriceDTO>>() {}.getType();
            Map<String, WikiPriceDTO> parsedData = gson.fromJson(dataObject, type);

            if (parsedData == null) return;

            parsedData.forEach((idStr, dto) -> {
                try {
                    int id = Integer.parseInt(idStr);
                    ItemPrice price = ItemPrice.builder()
                            .itemId(id)
                            .high(dto.high)
                            .low(dto.low)
                            .highTimestamp(dto.highTime)
                            .lowTimestamp(dto.lowTime)
                            .build();

                    priceCache.put(id, price);
                } catch (NumberFormatException ignored) {}
            });
        } catch (JsonParseException e) {
            log.error("Error parsing price JSON", e);
        }
    }

    @Data
    private static class WikiPriceDTO {
        private int high;
        private long highTime;
        private int low;
        private long lowTime;
    }
}