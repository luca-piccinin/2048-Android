package com.gameditors.a2048;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class CustomDialogMadness extends Dialog implements View.OnClickListener {

    private final Context context;
    private final String message;

    public CustomDialogMadness(Context context, String message) {
        super(context);
        this.context = context;
        this.message = message;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.custom_dialog);
        Button continueButton = (Button) findViewById(R.id.continue_button);
        Button backButton = (Button) findViewById(R.id.back_button);
        TextView messageTextView = (TextView) findViewById(R.id.message_textview);
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
                Intent intent = new Intent(context, MainMenuActivity.class);
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
