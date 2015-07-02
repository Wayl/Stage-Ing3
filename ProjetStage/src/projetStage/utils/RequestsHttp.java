package projetStage.utils;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class RequestsHttp {
    private final static String URL_REST = "http://10.10.6.169:8080/rest/select/transaction";

    // HTTP POST request
    public static String getData() {
        StringBuilder result = new StringBuilder();
        HttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost(URL_REST);
        String data = "{\"transaction_id\":\"2\"}";

        try {
            // Add header
            post.setHeader("User-Agent", "Mozilla/5.0");
            post.setHeader("Content-Type", "application/json; charset=utf-8");
            // Add data
            post.setEntity(new StringEntity(data));

            // Send request
            HttpResponse response = client.execute(post);
            System.out.println("\nSending 'POST' request to URL : " + URL_REST);
            System.out.println("Post parameters : " + post.getEntity());
            System.out.println("Response Code : " + response.getStatusLine().getStatusCode());

            BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

            String line;
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result.toString();
    }
}
