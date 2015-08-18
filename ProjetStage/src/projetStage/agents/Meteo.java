package projetStage.agents;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by wayl on 17/08/15 !
 */
public class Meteo {
    private static final String URL_GETFROMCASSANDRA = "http://10.10.6.169:8080/rest/getFromCassandraToCsv";
    private String BEGIN_DATE;
    private String END_DATE;
    private Map<String, Double> dataMeteo = new HashMap<>();

    public Meteo(String BEGIN_DATE, String END_DATE) {
        this.BEGIN_DATE = BEGIN_DATE;
        this.END_DATE = END_DATE;
        loadData();
    }

    public void loadData() {
        HttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost(URL_GETFROMCASSANDRA);
        String data = "timestart=" + BEGIN_DATE + "&timefinish=" + END_DATE + "&column=trans2_FD_Avg&filename=test.csv";

        try {
            // Add header
            post.setHeader("User-Agent", "Mozilla/5.0");
            post.setHeader("Accept", "text/csv");
            post.setHeader("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
            // Add data
            post.setEntity(new StringEntity(data));

            // Send request
            HttpResponse response = client.execute(post);
            System.out.println("\nSending 'POST' request to URL : " + URL_GETFROMCASSANDRA);
            System.out.println("Data : " + data);
            System.out.println("Response Code : " + response.getStatusLine().getStatusCode());

            BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            String line = rd.readLine();
            while ((line = rd.readLine()) != null) {
                System.out.println(line);
                String[] split = line.split(",");
                dataMeteo.put(split[0], Double.parseDouble(split[1]));
            }
            System.out.println(dataMeteo.size());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
