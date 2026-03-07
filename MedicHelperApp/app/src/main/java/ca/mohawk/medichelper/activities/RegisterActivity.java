package ca.mohawk.medichelper.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import ca.mohawk.medichelper.MyApp;
import ca.mohawk.medichelper.R;
import ca.mohawk.medichelper.ui.register.RegisterFragment;

public class RegisterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ((MyApp) getApplication()).setCurrentActivity(this);
    }

}