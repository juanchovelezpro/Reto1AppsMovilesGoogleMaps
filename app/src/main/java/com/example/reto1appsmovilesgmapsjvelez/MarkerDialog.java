package com.example.reto1appsmovilesgmapsjvelez;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MarkerDialog extends Dialog {

    private MainActivity activity;
    private ImageView imageViewMarker;
    private Button butCancelar, butAceptar;
    private TextView txtViewCrearMarker, txtViewNombreMarker;
    private EditText editTextNombreMark;



    public MarkerDialog(MainActivity activity) {
        super(activity);
        this.activity = activity;

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.marker_dialog);
        imageViewMarker = findViewById(R.id.imageViewMarker);
        butCancelar = findViewById(R.id.butCancelar);
        butAceptar = findViewById(R.id.butAceptar);
        txtViewCrearMarker = findViewById(R.id.textViewCrearMarker);
        txtViewNombreMarker = findViewById(R.id.txtViewNombreMarker);
        editTextNombreMark = findViewById(R.id.editTextNombreMark);

        butCancelar.setOnClickListener((v) -> {

            cancel();

        });


        butAceptar.setOnClickListener((v) -> {

            if(!editTextNombreMark.getText().toString().isEmpty()) {
                activity.addMarker(editTextNombreMark.getText().toString());
                dismiss();
            }else{

                Toast.makeText(activity, "Introduzca por favor un nombre para el marcador.", Toast.LENGTH_LONG).show();

            }
        });


    }

}
