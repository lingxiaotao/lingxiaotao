package com.example.qrencrypt;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;

import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.X509EncodedKeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.crypto.Cipher;

public class MainActivity extends Activity {

    private static final long DEFAULT_THIRD_MILLIS = 4102502399000L;

    private static final String publicKeyStr = "MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAIj0O11NWyCHIuMjuZK+NPqbbOQQMnO3uxP8kVbpWL1jtsWusPYLTUvv1ZoO4jekazCJ9ulGdVqYA8wzhXaWPqsCAwEAAQ==";
    private static final String privateKeyStr = "MIIBUwIBADANBgkqhkiG9w0BAQEFAASCAT0wggE5AgEAAkEAiPQ7XU1bIIci4yO5kr40+pts5BAyc7e7E/yRVulYvWO2xa6w9gtNS+/Vmg7iN6RrMIn26UZ1WpgDzDOFdpY+qwIDAQABAkBKcS7/hTe+U5Y0DswlJnyXp/olcWRrH/hxYb0Cl+4jawlriiSaGRTWzdmgztgMccsh4jH3ZsTkQnOU4RZKkBgxAiEA7rk1VxZxtLHoglEReFVnCTK2Re52DSGLvrlwFh/VGWkCIQCS3Y6vtn8JJfE3in9YHBsFRnqPVDFe5Q0axZ7BJNIg8wIgPRjPqwNGhK2HyVqAMyEAZt1AwzMdqYytXFnPcwogmwkCIAYHNg4cTYaQmiAPfT6emLVUuT8c05HzkOAicGu5GS7lAiAPjPTiL3pK5zSZsc/N03zZtqdkkyhOsgE/D7T8c08WUg==";

    private static final SimpleDateFormat TIME_FORMAT =
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

    private EditText etFirst;
    private EditText etTimestamp;
    private EditText etThird;
    private TextView tvEncrypted;
    private ImageView ivQr;
    private Button btnMode;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean liveMode = true;

    private final Runnable ticker = new Runnable() {
        @Override
        public void run() {
            if (liveMode) {
                etTimestamp.setText(TIME_FORMAT.format(new Date()));
            }
            handler.postDelayed(this, 1000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etFirst = findViewById(R.id.etFirst);
        etTimestamp = findViewById(R.id.etTimestamp);
        etThird = findViewById(R.id.etThird);
        tvEncrypted = findViewById(R.id.tvEncrypted);
        ivQr = findViewById(R.id.ivQr);
        btnMode = findViewById(R.id.btnMode);

        etFirst.setText("05ac4a");
        etTimestamp.setText(TIME_FORMAT.format(new Date()));
        etThird.setText(formatTime(DEFAULT_THIRD_MILLIS));

        btnMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setLiveMode(!liveMode);
            }
        });

        Button btn = findViewById(R.id.btnGenerate);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generate();
            }
        });

        applyMode();
    }

    private void setLiveMode(boolean live) {
        liveMode = live;
        if (live) {
            etTimestamp.setText(TIME_FORMAT.format(new Date()));
        }
        applyMode();
    }

    private void applyMode() {
        btnMode.setText(liveMode ? "自定义" : "实时");
        etTimestamp.setFocusable(!liveMode);
        etTimestamp.setFocusableInTouchMode(!liveMode);
        etTimestamp.setCursorVisible(!liveMode);
        etTimestamp.setClickable(!liveMode);
    }

    @Override
    protected void onResume() {
        super.onResume();
        handler.post(ticker);
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(ticker);
    }

    private void generate() {
        String first = etFirst.getText().toString().trim();
        if (first.isEmpty()) {
            Toast.makeText(this, "请输入设备号", Toast.LENGTH_SHORT).show();
            return;
        }

        long startMillis = liveMode ? System.currentTimeMillis() : parseTimeToMillis(etTimestamp, System.currentTimeMillis());
        long thirdMillis = parseTimeToMillis(etThird, DEFAULT_THIRD_MILLIS);

        String plainText = first + "," + startMillis + "," + thirdMillis;
        try {
            String encrypted = encrypt(plainText, publicKeyStr);
            tvEncrypted.setText(encrypted);

            Bitmap qr = encodeQr(encrypted, 260);
            if (qr != null) {
                ivQr.setImageBitmap(qr);
            } else {
                ivQr.setImageBitmap(null);
                Toast.makeText(this, "内容过长，无法生成二维码", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            tvEncrypted.setText("加密失败: " + e.getMessage());
            ivQr.setImageBitmap(null);
            Toast.makeText(this, "加密失败", Toast.LENGTH_SHORT).show();
        }
    }

    private long parseTimeToMillis(EditText field, long fallback) {
        String text = field.getText().toString().trim();
        if (text.isEmpty()) {
            field.setText(formatTime(fallback));
            return fallback;
        }
        try {
            return Long.parseLong(text);
        } catch (NumberFormatException ignored) {
        }
        try {
            Date parsed = TIME_FORMAT.parse(text);
            if (parsed != null) {
                return parsed.getTime();
            }
        } catch (Exception ignored) {
        }
        return fallback;
    }

    private static String formatTime(long millis) {
        return TIME_FORMAT.format(new Date(millis));
    }

    private Bitmap encodeQr(String content, int size) {
        try {
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.MARGIN, 1);
            BitMatrix matrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, size, size, hints);
            Bitmap bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
            for (int x = 0; x < size; x++) {
                for (int y = 0; y < size; y++) {
                    bmp.setPixel(x, y, matrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            return bmp;
        } catch (Exception e) {
            return null;
        }
    }

    public static String decrypt(String encryptedText, String privateKeyStr) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, getPrivateKey(privateKeyStr));
        return new String(cipher.doFinal(Base64.decode(encryptedText, Base64.DEFAULT)), "UTF-8");
    }

    public static String encrypt(String plainText, String publicKeyStr) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, getPublicKey(publicKeyStr));
        return Base64.encodeToString(cipher.doFinal(padData(plainText.getBytes("UTF-8"), getPublicKey(publicKeyStr).getModulus().bitLength() / 8)), Base64.DEFAULT);
    }

    private static RSAPublicKey getPublicKey(String key) throws Exception {
        return (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(Base64.decode(key, Base64.DEFAULT)));
    }

    private static RSAPrivateKey getPrivateKey(String key) throws Exception {
        return (RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(Base64.decode(key, Base64.DEFAULT)));
    }

    private static byte[] padData(byte[] data, int length) {
        byte[] paddedData = new byte[length];
        System.arraycopy(data, 0, paddedData, length - data.length, data.length);
        return paddedData;
    }
}
