package projetStage.agents.controller;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import repast.simphony.engine.schedule.ScheduledMethod;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by wayl on 17/08/15 !
 */
public class Meteo {
    private static final String URL_GETFROMCASSANDRA = "http://10.10.6.169:8080/rest/getSensorData";
    private static final SimpleDateFormat universalFullDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final int INTERVAL = 20; // Indique à quel interval de temps (en minutes) on doit récupérer les données depuis Cassandra

    private Calendar date = Calendar.getInstance();
    private Calendar endDate = Calendar.getInstance();
    private Map<String, Double> dataMeteo = new HashMap<>();

    /**
     * Constructeur par défaut
     * Initialisation des dates et chargement des premieres données des gisements solaires
     *
     * @param BEGIN_DATE Date de départ de la simulation
     */
    public Meteo(Calendar BEGIN_DATE) {
        universalFullDateFormat.setTimeZone(TimeZone.getTimeZone("GMT+04"));
        date.setTime(BEGIN_DATE.getTime());
        endDate.setTime(BEGIN_DATE.getTime());
        System.out.println("BEGIN_DATE : " + universalFullDateFormat.format(date.getTime()));
        loadNextSolarData();
    }

    /**
     * Actions effectuées à chaque étape de la simulation
     */
    @ScheduledMethod(start = 0, interval = 1)
    public void step() {
        date.add(Calendar.MINUTE, 1);
    }

    /**
     * Chargement des prochains données des gisements solaires
     */
    @ScheduledMethod(start = INTERVAL, interval = INTERVAL)
    public void loadNextSolarData() {
        endDate.add(Calendar.MINUTE, INTERVAL);
        dataMeteo.clear();
        loadSolarData();
    }

    /**
     * Envoie de la requête pour récupérer les données des gisements solaires
     */
    public void loadSolarData() {
        HttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost(URL_GETFROMCASSANDRA);
        String data = "timeStart=" + universalFullDateFormat.format(date.getTime()) + "&timeEnd=" + universalFullDateFormat.format(endDate.getTime()) + "&column=1441949924:FD_Avg";

        try {
            // Add header
            post.setHeader("User-Agent", "Mozilla/5.0");
            post.setHeader("Accept", "text/csv");
            post.setHeader("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
            // Add data
            post.setEntity(new StringEntity(data));

            // Send request
            HttpResponse response = client.execute(post);
            System.out.println("\nSending 'POST' request to URL : " + URL_GETFROMCASSANDRA + " - Response Code : " + response.getStatusLine().getStatusCode());
            System.out.println("Data : " + data);

            // Process response
            BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            String line = rd.readLine(); // We don't read the header
            while ((line = rd.readLine()) != null) {
                String[] split = line.split(",");
                dataMeteo.put(split[0], Double.parseDouble(split[1]));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public Calendar getDate() {
        return date;
    }

    public double getDataMeteo() {
        return dataMeteo.get(universalFullDateFormat.format(date.getTime()));
    }
}
