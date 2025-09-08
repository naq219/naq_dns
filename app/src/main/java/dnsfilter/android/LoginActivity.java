package dnsfilter.android;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Random;

// It seems like the R class is in a different package.
// I'''ll try to guess it based on the project structure.
// If this is incorrect, you'''ll need to adjust the import.
import dnsfilter.android.R;


public class LoginActivity extends Activity {

    private TextView tvCaptcha;
    private EditText etInput;
    private Button btnLogin;
    private Button btnPause30m;
    private Button btnPause1d;
    private String captchaText;

    private static final String PREFS_NAME = "app_prefs";
    private static final String PREF_FIRST_RUN = "first_run";
    private static final String PREF_LOGIN_SUCCESS_COUNT = "login_success_count";
    private static final int REQUIRED_SUCCESSFUL_LOGINS = -1;
    int currentSuccessCount =0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        tvCaptcha = findViewById(R.id.tvCaptcha);
        etInput = findViewById(R.id.etInput);
        btnLogin = findViewById(R.id.btnLogin);
        btnPause30m = findViewById(R.id.btnPause30m);
        btnPause1d = findViewById(R.id.btnPause1d);

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean firstRun = prefs.getBoolean(PREF_FIRST_RUN, true);

        if (firstRun) {
            // Mark as not first run anymore
            prefs.edit().putBoolean(PREF_FIRST_RUN, false).apply();
            // Go directly to DNSProxyActivity
            navigateToDNSProxyActivity();
            return; // Skip further login logic
        }



        // Proceed with login screen
        captchaText = generateCaptchaText();
        tvCaptcha.setText(captchaText);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String inputText = etInput.getText().toString();
                if (inputText.equalsIgnoreCase(captchaText.replace("_", ""))) {
                    SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                    //int currentSuccessCount = prefs.getInt(PREF_LOGIN_SUCCESS_COUNT, 0);
                    currentSuccessCount++;
                    //prefs.edit().putInt(PREF_LOGIN_SUCCESS_COUNT, currentSuccessCount).apply();

                    if (currentSuccessCount >= REQUIRED_SUCCESSFUL_LOGINS) {
                        Toast.makeText(LoginActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();
                        navigateToDNSProxyActivity();
                    } else {
                        int remainingLogins = REQUIRED_SUCCESSFUL_LOGINS - currentSuccessCount;
                        Toast.makeText(LoginActivity.this, "Login successful. " + remainingLogins + " more required.", Toast.LENGTH_SHORT).show();
                        // Regenerate captcha
                        captchaText = generateCaptchaText();
                        tvCaptcha.setText(captchaText);
                        etInput.setText(""); // Clear input
                    }
                } else {
                    // Failed login
                    Toast.makeText(LoginActivity.this, "Nội dung nhập vào không khớp!", Toast.LENGTH_SHORT).show();
                    // Regenerate captcha
                    captchaText = generateCaptchaText();
                    tvCaptcha.setText(captchaText);
                    etInput.setText(""); // Clear input
                }
            }
        });

        // Pause for 30 minutes
        btnPause30m.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long duration = 30L * 60L * 1000L;
                // Persist intent in prefs so service can restore after restart
                long until = System.currentTimeMillis() + duration;
                getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit().putLong("pause_until", until).apply();
                // Notify running service via broadcast
                Intent i = new Intent("pause_for");
                i.putExtra("duration", duration);
                sendBroadcast(i);
                Toast.makeText(LoginActivity.this, "Paused for 30 minutes", Toast.LENGTH_SHORT).show();
                //navigateToDNSProxyActivity();
            }
        });

        // Pause for 1 day
        btnPause1d.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long duration = 24L * 60L * 60L * 1000L;
                long until = System.currentTimeMillis() + duration;
                getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit().putLong("pause_until", until).apply();
                Intent i = new Intent("pause_for");
                i.putExtra("duration", duration);
                sendBroadcast(i);
                Toast.makeText(LoginActivity.this, "Paused for 1 day", Toast.LENGTH_SHORT).show();
                navigateToDNSProxyActivity();
            }
        });
    }

    private void navigateToDNSProxyActivity() {
        Intent intent = new Intent(LoginActivity.this, DNSProxyActivity.class);
        startActivity(intent);
        finish(); // Close the login activity
    }

    private String generateCaptchaText() {
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        int letterCount = 0; // Tracks consecutive letters to insert '_'

        while (sb.length() < 10) {
            if (letterCount == 20 && sb.length() <= 148) { // Ensure space for '_' and at least one more char (150 - 1 = 149, if sb.length is 148, then 148 + 1 = 149 for '_', 1 for char)
                sb.append("_");
                letterCount = 0; // Reset letter count after inserting '_'
            } else {
                sb.append((char) ('A' + random.nextInt(26)));
                letterCount++;
            }
        }
        return sb.toString();
    }
}
