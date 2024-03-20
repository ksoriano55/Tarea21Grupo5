package com.aplicacion.ejercicio21tarea;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.VideoView;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.aplicacion.ejercicio21tarea.config.videos;
import com.aplicacion.ejercicio21tarea.config.RestApiMethods;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

public class MainActivity extends AppCompatActivity {

    private static final int TOMA_VIDEO = 1;
    private VideoView vv1;
    Button btnTomarVideo;
    Button btnVerVideo;
    Button btnIrGaleria;
    Spinner sp1;
    private String[] lista;
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        vv1 =(VideoView) findViewById(R.id.videoView);
        btnVerVideo =(Button) findViewById(R.id.btnVerVideo);
        sp1 =(Spinner) findViewById(R.id.spinner);
        btnTomarVideo =(Button) findViewById(R.id.btnTomarVideo);
        btnIrGaleria =(Button) findViewById(R.id.btnIrGaleria);

        lista = fileList();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, lista);
        sp1.setAdapter(adapter);




        btnTomarVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                startActivityForResult(intent, TOMA_VIDEO);
            }
        });


        btnVerVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int pos = sp1.getSelectedItemPosition();
                vv1.setVideoPath(getFilesDir()+ "/" + lista[pos]);
                vv1.start();
            }
        });


        btnIrGaleria.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent in = new Intent(MainActivity.this, ActivityVisualizar.class);
                startActivity(in);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == TOMA_VIDEO && resultCode == RESULT_OK){
            Uri videoUri = data.getData();
            vv1.setVideoURI(videoUri);
            vv1.start();

            try {
                AssetFileDescriptor videoAsset = getContentResolver().openAssetFileDescriptor(data.getData(),"r");
                FileInputStream in = videoAsset.createInputStream();
                FileOutputStream archivo = openFileOutput(crerNombreArchivoMP4(), Context.MODE_PRIVATE);
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                byte[] buf = new byte[1024];
                int len;

                while ((len = in.read(buf))>0){
                    archivo.write(buf, 0, len);
                    byteArrayOutputStream.write(buf, 0, len);
                }

                byte[] videoBytes = byteArrayOutputStream.toByteArray();

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);

                        JSONObject requestBody = new JSONObject();
                        try {
                            requestBody.put("videos", videoBytes);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(com.android.volley.Request.Method.POST,
                                RestApiMethods.EndpointPostVideos, requestBody,
                                new com.android.volley.Response.Listener<JSONObject>() {
                                    @Override
                                    public void onResponse(JSONObject response) {
                                        try
                                        {
                                            String mensaje = response.getString("message");
                                            Toast.makeText(getApplicationContext(), mensaje, Toast.LENGTH_LONG).show();
                                        }
                                        catch (Exception ex)
                                        {
                                            ex.printStackTrace();
                                        }
                                    }
                                }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Toast.makeText(getApplicationContext(), error.getMessage().toString(),
                                        Toast.LENGTH_LONG).show();
                            }
                        }) {
                            @Override
                            public Map<String, String> getHeaders() throws AuthFailureError {
                                Map<String, String> headers = new HashMap<>();
                                headers.put("Content-Type", "application/json");
                                return headers;
                            }
                        };

                        requestQueue.add(jsonObjectRequest);
                    }
                }).start();
            } catch (IOException e){
                Toast.makeText(this, "Problemas en la grabación", Toast.LENGTH_LONG).show();
            }
        }
    }

    private String crerNombreArchivoMP4(){
        String fecha = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String nombre = fecha + ".mp4";
        return nombre;
    }
}