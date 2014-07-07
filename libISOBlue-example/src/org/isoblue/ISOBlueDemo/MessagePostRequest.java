/*
 * Author: Alex Layton <alex@layton.in>
 * 
 * Copyright (c) 2014 Purdue University
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.isoblue.ISOBlueDemo;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;

/**
 * Uses Google Volley to post to a Google form as a way to upload data. TODO:
 * Use response to check if the data was actually uploaded.
 * 
 * @author Alex Layton <alex@layton.in>
 */
public class MessagePostRequest extends Request<Void> {

    private static final String URL = "https://docs.google.com/forms/d/1mszF-1Dvk18ajb6WhP22ctkDqizNI9-wMjylDznPKjs/formResponse";
    private static final String BUS_ENTRY = "entry.2120486804";
    private static final String DEST_ENTRY = "entry.363860839";
    private static final String SRC_ENTRY = "entry.80712516";
    private static final String DATA_ENTRY = "entry.1759126747";
    private static final String TIME_ENTRY = "entry.478698627";

    private final Map<String, String> mParams;

    public MessagePostRequest(String bus, String dest, String src, String data,
            String time) {
        super(Method.POST, URL, new ErrorListener());

        Map<String, String> params = new HashMap<String, String>();
        params.put(BUS_ENTRY, bus);
        params.put(DEST_ENTRY, dest);
        params.put(SRC_ENTRY, src);
        params.put(DATA_ENTRY, data);
        params.put(TIME_ENTRY, time);
        mParams = Collections.unmodifiableMap(params);
    }

    @Override
    protected Response<Void> parseNetworkResponse(NetworkResponse response) {
        // Don't Care about response
        return null;
    }

    @Override
    protected void deliverResponse(Void response) {
        // Don't Care about response
    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        return mParams;
    }

    private static class ErrorListener implements Response.ErrorListener {

        @Override
        public void onErrorResponse(VolleyError error) {
            VolleyLog.d("Error: " + error.getMessage());
        }
    }
}
