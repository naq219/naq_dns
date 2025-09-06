package dnsfilter.android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Random;

// It seems like the R class is in a different package.
// I'll try to guess it based on the project structure.
// If this is incorrect, you'll need to adjust the import.
import dnsfilter.android.R;


public class LoginActivity extends Activity {

    private TextView tvCaptcha;
    private EditText etInput;
    private Button btnLogin;
    private String captchaText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        tvCaptcha = findViewById(R.id.tvCaptcha);
        etInput = findViewById(R.id.etInput);
        btnLogin = findViewById(R.id.btnLogin);

        captchaText = generateCaptchaText();
        tvCaptcha.setText(captchaText);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String inputText = etInput.getText().toString();
                // Compare ignoring case and after removing underscores from the captcha
                if (inputText.equalsIgnoreCase(captchaText.replace("_", ""))) {
                    // Successful login, start the main activity
                    Intent intent = new Intent(LoginActivity.this, DNSProxyActivity.class);
                    startActivity(intent);
                    finish(); // Close the login activity
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
    }

    private String generateCaptchaText() {
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        int letterCount = 0; // Tracks consecutive letters to insert '_'

        while (sb.length() < 100) {
            if (letterCount == 20 && sb.length() <= 48) { // Ensure space for '_' and at least one more char
                sb.append("_");
                letterCount = 0; // Reset letter count after inserting '_'
                                 // Do not increment i here, as '_' takes a character spot
            } else {
                sb.append((char) ('A' + random.nextInt(26)));
                letterCount++;
            }
        }
        Log.i("Captcha Text: " , sb.toString());
        return sb.toString();
    }
}