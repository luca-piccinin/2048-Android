package com.gameditors.a2048;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class CustomDialog extends Dialog implements View.OnClickListener {

    private final Context context;

    public CustomDialog(Context context) {
        super(context);
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.custom_dialog);
        Button continueButton = (Button) findViewById(R.id.continue_button);
        Button backButton = (Button) findViewById(R.id.back_button);
        TextView messageTextView = (TextView) findViewById(R.id.message_textview);
        String message = "Are you sure to continue?";
        messageTextView.setText(message);
        continueButton.setOnClickListener(this);
        backButton.setOnClickListener(this);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.continue_button:
                dismiss();
                Intent intent = new Intent(context, MainMenuActivityMadness.class);
                context.startActivity(intent);
                break;
            case R.id.back_button:
                dismiss();
                break;
            default:
                break;
        }
    }
}
