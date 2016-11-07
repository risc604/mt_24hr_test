package com.microlife.software.a24hr_mt;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;

public class CreateUserProfileActivity extends AppCompatActivity
{
    ImageButton saveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_user_profile);

        saveButton = (ImageButton) findViewById(R.id.imgbtnSave);


        saveButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent mainIntent = new Intent(getBaseContext(), MainActivity.class);
                startActivity(mainIntent);
            }
        });
        //*/
    }
}
