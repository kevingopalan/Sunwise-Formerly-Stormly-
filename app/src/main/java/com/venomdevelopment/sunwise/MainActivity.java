package com.venomdevelopment.sunwise;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.venomdevelopment.sunwise.Adapters.HourlyAdapters;
import com.venomdevelopment.sunwise.Domains.Hourly;

import java.util.ArrayList;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private RecyclerView.Adapter adapterHourly;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        Objects.requireNonNull(getSupportActionBar()).hide();
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        initRecyclerView();
        TextView tempText, tempText2, temp1text, descText , humidityText;
        Button search;
        search = findViewById(R.id.search);
        CharSequence toasttext = "I am rewriting the forecast code, just wait a bit lol";
        int duration = Toast.LENGTH_SHORT;
        Toast wiptoast = Toast.makeText(this, toasttext, duration);
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // calling api
                wiptoast.show();
            }
        });
    }

    private void initRecyclerView() {
        ArrayList<Hourly> items = new ArrayList<>();
        items.add(new Hourly("9 pm", 28, "cloudy"));
        items.add(new Hourly("10 pm", 29, "sun"));
        items.add(new Hourly("11 pm", 30, "windy"));
        items.add(new Hourly("12 am", 29, "rain"));
        items.add(new Hourly("1 am", 27, "tstorm"));

        recyclerView = findViewById(R.id.view1);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        adapterHourly = new HourlyAdapters(items);
        recyclerView.setAdapter(adapterHourly);
    }
}