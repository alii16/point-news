package com.example.gonews;

import android.animation.Animator;
import android.content.Intent;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.airbnb.lottie.LottieAnimationView;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        LottieAnimationView lottieAnimationView = findViewById(R.id.lottieAnimationView);
        TextView goNewsTextView = findViewById(R.id.goNewsTextView);

        goNewsTextView.post(() -> {
            Shader textShader = new LinearGradient(
                    0,
                    0,
                    goNewsTextView.getMeasuredWidth(),
                    goNewsTextView.getMeasuredHeight(),
                    new int[]{
                            getResources().getColor(R.color.gradient_start_color, null),
                            getResources().getColor(R.color.gradient_end_color, null)
                    },
                    null,
                    Shader.TileMode.CLAMP
            );
            goNewsTextView.getPaint().setShader(textShader);
            goNewsTextView.invalidate();
        });

        lottieAnimationView.addAnimatorListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {}

            @Override
            public void onAnimationEnd(Animator animation) {
                Intent mainIntent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(mainIntent);
                finish();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                Intent mainIntent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(mainIntent);
                finish();
            }

            @Override
            public void onAnimationRepeat(Animator animation) {}
        });
    }
}