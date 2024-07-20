

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

public class Randomizer {
    static ArrayList<Integer> selected = new ArrayList<>();


    public static JSONArray randomize(JSONArray arr) {
        JSONArray sample = new JSONArray();


        int maxSize = (int) Math.ceil(arr.length() / 10);

        while (sample.length() != maxSize) {
            int randomIndex = getRandomIndex(arr.length());
            sample.put(arr.get(randomIndex));
        }

        return sample;
    }

    private static int getRandomIndex(int bound) {
        Random rand = new Random();
        int sample = rand.nextInt(bound);

        while (selected.contains(sample)) {
            sample = rand.nextInt(bound);
        }

        selected.add(sample);
        return sample;
    }
}

