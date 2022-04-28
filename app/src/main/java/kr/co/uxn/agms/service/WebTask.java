package kr.co.uxn.agms.service;

import android.icu.text.SimpleDateFormat;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

import kr.co.uxn.agms.CommonConstant;
import kr.co.uxn.agms.data.room.GlucoseData;
import kr.co.uxn.agms.data.room.SensorRepository;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class WebTask {
    private static final String TAG="WebTask";


    private static WebTask instance;
    private static SensorRepository mRepository;
    private static SimpleDateFormat formatter=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


    WebTask(SensorRepository repository) {
        mRepository=repository;
    }

    public static void startTask(final String patient_id, final long patient_number) {
        if(mRepository!=null) new Check_data_Task(patient_id, patient_number).execute();
    }

    public static WebTask getInstance(SensorRepository repository) {
        if (instance == null) {

            instance=new WebTask(repository);
        }
        return instance;
    }

    static class Insert_DB_Task extends AsyncTask<Void, Void, JSONObject> {
        String patient_id;
        long patient_number;
        List<GlucoseData> glucoseDataList;

        public Insert_DB_Task(List<GlucoseData> glucoseDataList, String patient_id, long patient_number) {
            this.glucoseDataList=glucoseDataList;
            this.patient_id=patient_id;
            this.patient_number=patient_number;
        }


        @Override
        protected JSONObject doInBackground(Void... voids) {
            try {
                HttpUrl httpUrl=new HttpUrl.Builder()
                        .scheme("http")
                        .host(CommonConstant.HOST)
                        .port(CommonConstant.PORT)
                        .addPathSegment("insert_db")
                        .build();

                OkHttpClient client=new OkHttpClient();
                JSONArray jsonInput=new JSONArray();

                if (glucoseDataList != null && glucoseDataList.size() > 0) {
                    for (int i=0; i < glucoseDataList.size(); i++) {
                        JSONObject json_Data=new JSONObject();
                        GlucoseData data=glucoseDataList.get(i);
                        json_Data.put("patient_id", data.getUser());
                        json_Data.put("experiment_date", formatter.format(new Date(data.getData_date())));
                        json_Data.put("value_current", data.getWe_current());
                        json_Data.put("value_battery_level", data.getBatteryLevel());

                        jsonInput.put(json_Data);
                    }
                }

                RequestBody reqBody=RequestBody.create(
                        MediaType.parse("application/json; charset=utf-8"),
                        jsonInput.toString()
                );
                Log.d(TAG, jsonInput.toString());
                Request request=new Request.Builder()
                        .post(reqBody)
                        .url(httpUrl)
                        .build();

                Response responses=null;
                responses=client.newCall(request).execute();
                JSONObject receive_Json=new JSONObject(responses.body().string());
                Log.d(TAG, receive_Json.toString());
                return receive_Json;

            } catch (JSONException | IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);

            if (jsonObject != null) {
                boolean success=false;
                try {
                    success=jsonObject.getBoolean("success");
                } catch (JSONException e) {
                    e.printStackTrace();
                    startTask(patient_id, patient_number);
                    return;
                }

                if (!success) {

                    startTask(patient_id, patient_number);
                    return;
                }

            } else {
                startTask(patient_id, patient_number);
                return;
            }
        }
    }

    static class Check_data_Task extends AsyncTask<Void, Void, JSONObject> {
        String patient_id;
        long patient_number;


        Check_data_Task(String patient_id, long patient_number) {
            this.patient_id=patient_id;
            this.patient_number=patient_number;
        }

        @Override
        protected JSONObject doInBackground(Void... voids) {
            try {
                HttpUrl httpUrl=new HttpUrl.Builder()
                        .scheme("http")
                        .host(CommonConstant.HOST)
                        .port(CommonConstant.PORT)
                        .addPathSegment("chek_data")
                        .build();

                OkHttpClient client=new OkHttpClient();
                JSONObject jsonInput=new JSONObject();


                jsonInput.put("patient_id", patient_id);

                RequestBody reqBody=RequestBody.create(
                        MediaType.parse("application/json; charset=utf-8"),
                        jsonInput.toString()
                );

                Request request=new Request.Builder()
                        .post(reqBody)
                        .url(httpUrl)
                        .build();

                Response responses=null;
                responses=client.newCall(request).execute();

                JSONObject receive_Json=new JSONObject(responses.body().string());
                Log.d(TAG, receive_Json.toString());
                return receive_Json;
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);
            boolean success=false;
            String s_date=null;
            Date experiment_date=null;
            if (jsonObject != null) {
                try {
                    success=jsonObject.getBoolean("success");

                } catch (JSONException e) {
                    e.printStackTrace();
                }


                if (success) {
                    try {
                        s_date=jsonObject.getString("experiment_date");
                        experiment_date=formatter.parse(s_date);
                    } catch (ParseException | JSONException e) {
                        e.printStackTrace();
                    }
                    final long finalExperiment_date=experiment_date.getTime() + 1000;
                    Thread thread=new Thread(() -> {
                        List<GlucoseData> glucoseDataList=mRepository.getSendDataList(patient_number, finalExperiment_date);
                        if (glucoseDataList.size() > 0) {
                            new Insert_DB_Task(glucoseDataList, patient_id, patient_number).execute();
                        }
                    });
                    thread.start();
                } else {
                    try {
                        s_date=jsonObject.getString("experiment_date");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                    if (s_date != null) {
                        Thread thread=new Thread(() -> {
                            List<GlucoseData> glucoseDataList=mRepository.getGlucoseAllDataArrayList(patient_number);
                            if (glucoseDataList.size() > 0) {
                                new Insert_DB_Task(glucoseDataList, patient_id, patient_number).execute();
                            }
                        });
                        thread.start();
                    } else {
                        startTask(patient_id, patient_number);
                        return;
                    }

                }
            } else {
                startTask(patient_id, patient_number);
                return;
            }
        }


    }
}

