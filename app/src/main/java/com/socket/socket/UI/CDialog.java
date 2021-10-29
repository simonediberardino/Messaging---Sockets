package com.socket.socket.UI;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.socket.socket.R;
import com.socket.socket.utility.Utility;

public class CDialog extends Dialog implements View.OnClickListener {
    public Activity c;
    public String title, option1, option2;
    public Runnable firstCallback, secondCallback, dismissCallback;

    public CDialog(Activity c, String title, Runnable firstCallback) {
        this(c, title, c.getString(R.string.ok), c.getString(R.string.cancel), firstCallback, null, null);
    }

    public CDialog(Activity c, String title, String option1, String option2, Runnable firstCallback, Runnable secondCallback, Runnable dismissCallback){
        super(c);
        this.c = c;
        this.title = title;
        this.option1 = option1;
        this.option2 = option2;

        this.dismissCallback = () -> {
            if(dismissCallback != null)
                dismissCallback.run();

            this.dismiss();
        };

        this.firstCallback = () -> {
            if(firstCallback != null)
                firstCallback.run();

            this.dismissCallback = null;

            this.dismiss();
        };

        this.secondCallback = () -> {
            if(secondCallback != null)
                secondCallback.run();

            this.dismissCallback = null;

            this.dismiss();
        };
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog);

        ViewGroup parentView = this.findViewById(R.id.dialog_parent);
        TextView title = this.findViewById(R.id.dialog_title);
        Button btn1 = this.findViewById(R.id.dialog_btn1);
        Button btn2 = this.findViewById(R.id.dialog_btn2);
        View btnClose = this.findViewById(R.id.dialog_close);

        title.setText(this.title);

        btn1.setOnClickListener(v -> this.firstCallback.run());
        btn1.setText(this.option1);

        btn2.setOnClickListener(v -> this.secondCallback.run());
        btn2.setText(this.option2);

        this.setOnDismissListener(dialog -> {
            if(this.dismissCallback != null)
                this.dismissCallback.run();
        });

        btnClose.setOnClickListener(v -> {
            if(this.dismissCallback != null)
                this.dismissCallback.run();
        });

        Utility.ridimensionamento((AppCompatActivity) c, parentView);
    }

    @Override public void onClick(View v){}
}