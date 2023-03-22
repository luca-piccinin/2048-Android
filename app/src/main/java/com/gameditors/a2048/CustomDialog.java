package com.gameditors.a2048;

import static android.support.v4.content.ContextCompat.startActivity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class CustomDialog extends Dialog implements View.OnClickListener {

    private Button continueButton;
    private Button backButton;
    private TextView messageTextView;

    private Context context;
    private String message = "";

    public CustomDialog(Context context) {
        super(context);
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.custom_dialog);
        continueButton = (Button) findViewById(R.id.continue_button);
        backButton = (Button) findViewById(R.id.back_button);
        messageTextView = (TextView) findViewById(R.id.message_textview);
        messageTextView.setText(message);
        continueButton.setOnClickListener(this);
        backButton.setOnClickListener(this);
    }

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
