/*
 * Copyright (C) 2018 John Fischer
 */

package com.udacity.sandwichclub.utils;

import android.util.Log;

import com.udacity.sandwichclub.model.Sandwich;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Module to handle json data
 */

public class JsonUtils {

    /**
     * Returns a Sandwich by parsing json data for known key/value pairs
     * @param json String
     * @return Sandwich
     */
    public static Sandwich parseSandwichJson(String json) {
        // Empty list for values where a list is expect but has no values (ingredients, alsoKnownAs)
        List<String> emptyList = new ArrayList<>();
        emptyList.add("-");

        // HashMap to store Json key/value properties
        HashMap<String, Object> sandwichMap = new HashMap<>();
        // All known keys and default value(s)
        sandwichMap.put("mainName", "-");
        sandwichMap.put("description", "-");
        sandwichMap.put("placeOfOrigin", "-");
        sandwichMap.put("image", "-");
        sandwichMap.put("ingredients", emptyList);
        sandwichMap.put("alsoKnownAs", emptyList);

        // Remove the escape characters from the json string
        json = json.replace("\\", "");

        for (String key: sandwichMap.keySet()) {
            int index = json.indexOf(key + "\":");  // keys are always followed by " and :
            if (index < 0) continue ;  // key not found
            int start = index + key.length() + 2;  // add 2 characters for " and :

            // check if the current location is a list or a simple value
            if (json.charAt(start) == '[') {
                // check if next character indicates an empty list
                if (json.charAt(start+1) != ']') {
                    // not an empty list find the closing character
                    int end = json.indexOf(']', start + 2);  // skip [ and "
                    if (end < 0) continue;  // malformed key/value pair
                    String substr = json.substring(start + 2, end - 1);  // skip [, " and ]
                    // create a list of values split at ", "
                    List<String> values =
                            new ArrayList<>(Arrays.asList(substr.split("\",\"")));
                    // update the key/value pair in the hashmap
                    sandwichMap.put(key, values);
                }
            } else {
                // find the end of a simple key/value pair
                int end = json.indexOf('"', start+1);
                if (end < 0) continue ;  // malformed key/value pair
                // check if the next character is a key/value pairs separater or the length of the string
                while (json.charAt(end + 1) != ',' && end != json.length() && end != -1) {
                    end = json.indexOf('"', end + 1);
                }
                if (end < 0) continue ;  // malformed key/value pair
                // check if there is a value
                if (start + 1 != end) {
                    // Ensure even number of quotes within the value
                    String substr = json.substring(start+1, end);
                    int count = substr.length() - substr.replace("\"", "").length();
                    if (count % 2 != 0)
                        end++;
                    // update the key/value pair in the hashmap
                    sandwichMap.put(key, json.substring(start + 1, end));
                }
            }
        }

        // ensure completely empty sandwich is not returned
        int count = 0;
        for (String key: sandwichMap.keySet()) {
            Object value = sandwichMap.get(key);
            if (value instanceof String) {
                if (! value.equals("-")) {
                    count++;
                }
            } else {
                try {
                    List vlist = (List)value;
                    if (!vlist.isEmpty()) {
                        String string_value = (String)vlist.get(0);
                        if (! string_value.equals("-")) {
                            count++;
                        }
                    }
                } catch (ClassCastException e) {
                    Log.e("Sandwich", "Unable to validate sandwich");
                    Log.e("Sandwich", e.getMessage());
                    Log.i("Json", json);
                    return null;
                }
            }
        }

        if (count == 0) {
            return null;
        }

        // ensure class cast exception are caught
        List<String> alsoKnownAs = null;
        if (sandwichMap.containsKey("alsoKnownAs")) {
            try {
                alsoKnownAs = (List<String>) sandwichMap.get("alsoKnownAs");
            } catch (ClassCastException e) {
                Log.e("Sandwich", "Unable to cast alsoKnownAs");
                Log.e("Sandwich", e.getMessage());
                return null;
            }
        }

        List<String>ingredients = null;
        if (sandwichMap.containsKey("ingredients")) {
            try {
                ingredients = (List<String>) sandwichMap.get("ingredients");
            } catch (ClassCastException e) {
                Log.e("Sandwich", "Unable to cast ingredients");
                Log.e("Sandwich", e.getMessage());
                return null;
            }
        }

        // create and return the sandwich that was found
        return new Sandwich((String)sandwichMap.get("mainName"), alsoKnownAs,
                (String)sandwichMap.get("placeOfOrigin"), (String)sandwichMap.get("description"),
                (String)sandwichMap.get("image"), ingredients);
    }
}
