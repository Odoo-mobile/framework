package com.odoo.core.account;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.odoo.R;
import com.odoo.config.IntroSliderItems;
import com.odoo.widgets.slider.SliderView;

public class AppIntro extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_intro);
        SliderView sliderView = (SliderView) findViewById(R.id.sliderView);
        IntroSliderItems sliderItems = new IntroSliderItems();
        if (!sliderItems.getItems().isEmpty()) {
            sliderView.setItems(getSupportFragmentManager(), sliderItems.getItems());
        } else {
            finish();
        }
    }
}
