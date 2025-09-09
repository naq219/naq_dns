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
    private static final String PREF_30M_LAST_DAY = "throttle_30m_last_day"; // yyyymmdd last used day
    private static final int REQUIRED_SUCCESSFUL_LOGINS = 50;
    int currentSuccessCount =0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        tvCaptcha = findViewById(R.id.tvCaptcha);
        tvCaptcha.setLongClickable(false);
        tvCaptcha.setTextIsSelectable(false);
        etInput = findViewById(R.id.etInput);
        btnLogin = findViewById(R.id.btnLogin);
        btnPause30m = findViewById(R.id.btnPause30m);
        btnPause1d = findViewById(R.id.btnPause1d);

        // Disable 30m button if already used today
        SharedPreferences prefsForBtn = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean canUse30m = canUse30mToday(prefsForBtn);
        //btnPause30m.setEnabled(canUse30m);

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
                SharedPreferences sp = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                if (!canUse30mToday(sp)) {
                    long ms = millisUntilTomorrow();
                    long hours = ms / (60L * 60L * 1000L);
                    long minutes = (ms / (60L * 1000L)) % 60L;
                    String msg = String.format("Bạn đã dùng 30 phút hôm nay. Vui lòng thử lại sau %d giờ %d phút.", hours, minutes);
                    Toast.makeText(LoginActivity.this, msg, Toast.LENGTH_LONG).show();
                    return;
                }

                long duration = 30L * 60L * 1000L;
                // Mark used today
                sp.edit().putInt(PREF_30M_LAST_DAY, localDayKey()).apply();

                // Notify running service via broadcast
                Intent i = new Intent("pause_for");
                i.putExtra("duration", duration);
                sendBroadcast(i);
                Toast.makeText(LoginActivity.this, "Đã tạm dừng DNS 30 phút", Toast.LENGTH_SHORT).show();

                // Update UI state
                btnPause30m.setEnabled(false);
                //navigateToDNSProxyActivity();
            }
        });

        // Pause for 1 day
        btnPause1d.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                long duration = 24L * 60L * 60L * 1000L;
//                long until = System.currentTimeMillis() + duration;
//                getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit().putLong("pause_until", until).apply();
//                Intent i = new Intent("pause_for");
//                i.putExtra("duration", duration);
//                sendBroadcast(i);
//                Toast.makeText(LoginActivity.this, "Paused for 1 day", Toast.LENGTH_SHORT).show();
                //navigateToDNSProxyActivity();
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

    // ---- Helpers for daily throttle (local day reset at 00:00) ----
    private int localDayKey() {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        int y = cal.get(java.util.Calendar.YEAR);
        int m = cal.get(java.util.Calendar.MONTH) + 1; // 1..12
        int d = cal.get(java.util.Calendar.DAY_OF_MONTH);
        return y * 10000 + m * 100 + d; // yyyymmdd
    }

    private boolean canUse30mToday(SharedPreferences sp) {
        int last = sp.getInt(PREF_30M_LAST_DAY, -1);
        return last != localDayKey();
    }

    private long millisUntilTomorrow() {
        java.util.Calendar now = java.util.Calendar.getInstance();
        java.util.Calendar tomorrow = (java.util.Calendar) now.clone();
        tomorrow.add(java.util.Calendar.DAY_OF_YEAR, 1);
        tomorrow.set(java.util.Calendar.HOUR_OF_DAY, 0);
        tomorrow.set(java.util.Calendar.MINUTE, 0);
        tomorrow.set(java.util.Calendar.SECOND, 0);
        tomorrow.set(java.util.Calendar.MILLISECOND, 0);
        return tomorrow.getTimeInMillis() - System.currentTimeMillis();
    }
}
