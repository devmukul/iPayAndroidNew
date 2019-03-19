package bd.com.ipay.ipayskeleton.CustomView.Dialogs;

import android.animation.Animator;
import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;

import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.Constants;

/*

Used lottie json files as animations

link :  https://www.lottiefiles.com
Library Github Link:   https://github.com/airbnb/lottie-android

*/


public class AnimatedProgressDialog extends android.support.v7.app.AlertDialog {
    private Context context;
    private LottieAnimationView animationView;
    private TextView progressDialogTextView;
    private String currentState;


    public AnimatedProgressDialog(Context context) {
        super(context);
        this.context = context;
        currentState = Constants.STATE_LOADING;
        createView();
    }

    private void createView() {
        View customView = LayoutInflater.from(context).inflate(R.layout.view_animated_progress_dialog, null, false);
        progressDialogTextView = customView.findViewById(R.id.progress_dialog_text_view);
        animationView = customView.findViewById(R.id.view_animation);
        animationView.setSpeed(1.1f);
        setUpAnimationAction();
        animationView.playAnimation();
        this.setView(customView);
        this.setCancelable(false);
        this.setCanceledOnTouchOutside(false);
    }

    public void showDialog() {
        currentState = Constants.STATE_LOADING;
        animationView.setAnimation(R.raw.spinner);
        animationView.setMinAndMaxProgress(0.0f, 1.0f);
        animationView.playAnimation();
        this.show();
    }

    public void dismissDialog() {
        this.clearAnimation();
        this.hide();
    }

    private void setUpAnimationAction() {
        animationView.addAnimatorListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {

            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {
                if (currentState.equals(Constants.STATE_SUCCESS) || currentState.equals(Constants.STATE_FAILED)) {
                    animationView.pauseAnimation();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            AnimatedProgressDialog.this.animationView.setMinAndMaxProgress(0.0f, 1.0f);
                            AnimatedProgressDialog.this.dismissDialog();
                        }
                    }, 1000);
                }

            }
        });
    }

    public void showSuccessAnimationAndMessage(final String successMessage) {
        if (!this.isShowing()) {
            this.show();
        }
        animationView.pauseAnimation();
        animationView.setAnimation(R.raw.check_success);
        animationView.setMinAndMaxProgress(0.5f, 1.0f);
        animationView.playAnimation();
        progressDialogTextView.setText(successMessage);
        currentState = Constants.STATE_SUCCESS;
    }

    public void showFailureAnimationAndMessage(String failureMessage) {
        animationView.setMinAndMaxProgress(0.0f, 0.3f);
        progressDialogTextView.setText(failureMessage);
        animationView.setAnimation(R.raw.cruz);
        currentState = Constants.STATE_FAILED;
        animationView.playAnimation();
    }

    private void clearAnimation() {
        animationView.clearAnimation();
        progressDialogTextView.setText(context.getString(R.string.please_wait));
    }
}

