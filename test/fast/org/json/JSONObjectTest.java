package org.json;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class JSONObjectTest {
    @Test
    public void testJSON() {
        String response = "{\"results\":[{\"version\":\"1.0\",\"integration\":false},{\"version\":\"1.1\",\"integration\":false}]}";
        JSONArray array = new JSONObject(response).getJSONArray("results");
        assertThat(((JSONObject) array.get(0)).getString("version"), is("1.0"));
        assertThat(((JSONObject) array.get(1)).getString("version"), is("1.1"));
    }
}