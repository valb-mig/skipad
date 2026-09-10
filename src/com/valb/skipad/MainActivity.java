package com.valb.skipad;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Tela única: explica o app e abre as configurações de Acessibilidade
 * para o usuário ativar o serviço. Construída em código, sem layout XML.
 */
public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setBackgroundColor(Color.parseColor("#0F0F0F"));
        int pad = dp(24);
        root.setPadding(pad, pad, pad, pad);

        TextView title = new TextView(this);
        title.setText("Skip Ad");
        title.setTextColor(Color.WHITE);
        title.setTextSize(28);
        title.setGravity(Gravity.CENTER);

        TextView info = new TextView(this);
        info.setText("Pula automaticamente os anúncios do YouTube.\n\n"
                + "Ative o serviço \"Skip Ad\" na tela de Acessibilidade "
                + "que vai abrir. Depois é só usar o YouTube normalmente.");
        info.setTextColor(Color.parseColor("#BBBBBB"));
        info.setTextSize(16);
        info.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams infoLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        infoLp.topMargin = dp(20);
        infoLp.bottomMargin = dp(28);
        info.setLayoutParams(infoLp);

        Button btn = new Button(this);
        btn.setText("Abrir Acessibilidade");
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
            }
        });

        root.addView(title);
        root.addView(info);
        root.addView(btn);
        setContentView(root);
    }

    private int dp(int value) {
        float d = getResources().getDisplayMetrics().density;
        return Math.round(value * d);
    }
}
