package com.wifidirect.milan.wifidirect.utils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by milan on 1.12.15..
 */
public class MessageUtils {
    public static final int TYPE_FILE = 1;
    public static final int TYPE_MESSAGE = 2;
    public static final String TYPE_VIDEO = "video";
    public static final String TYPE_IMAGE = "image";

    /** Convert to json object. */
    public static String createMessage(String name, String message, int type, int size, String mediaType) {
        try {
            JSONObject jsonObject = new JSONObject();
            if (type == 1) {
                jsonObject.put("sendername", name);
                jsonObject.put("filetype", "file");
                jsonObject.put("mediatype", mediaType);
                jsonObject.put("message", message);
                jsonObject.put("size", size);

                return jsonObject.toString();

            } else if (type == 2) {
                jsonObject.put("sendername", name);
                jsonObject.put("filetype", "message");
                jsonObject.put("mediatype", "text");
                jsonObject.put("message", message);
                jsonObject.put("size", size);

                return jsonObject.toString();
            }
        } catch (JSONException e){}
        return null;
    }


    /** Deserialize json object. */
    public static String[] parseMessage(String message) {
        try{
            String response[] = new String[6];
            JSONObject jsonObject = new JSONObject(message);

            // sender name
            response[0] = jsonObject.getString("sendername");
            // file type
            response[1] = jsonObject.getString("filetype");
            // media type
            response[2] = jsonObject.getString("mediatype");
            // message
            response[3] = jsonObject.getString("message");
            // size
            response[4] = jsonObject.getString("size");

            return response;

        } catch (JSONException | ArrayIndexOutOfBoundsException e){}
        return null;
    }



}
