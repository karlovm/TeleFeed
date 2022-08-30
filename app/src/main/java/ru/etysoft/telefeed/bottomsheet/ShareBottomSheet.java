package ru.etysoft.telefeed.bottomsheet;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.drinkless.td.libcore.telegram.Client;
import org.drinkless.td.libcore.telegram.TdApi;

import java.util.ArrayList;
import java.util.HashMap;

import ru.etysoft.telefeed.R;
import ru.etysoft.telefeed.api.ImageCropper;
import ru.etysoft.telefeed.api.Telegram;
import ru.etysoft.telefeed.views.AutoGridLayout;

public class ShareBottomSheet extends BottomSheetDialogFragment {
    private BottomSheetListener mListener;

    public static HashMap<TdApi.Chat, Bitmap> avatars = new HashMap<>();

    public static long postId = 0;
    public static long channelId = 0;

    private View view;

    private EditText inputMessage;
    private ArrayList<Long> toShare = new ArrayList<>();
    private boolean isPlayingAnimation = false;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Пустой фон
        setStyle(BottomSheetDialogFragment.STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme);
    }


    @Override
    public void onActivityCreated(Bundle arg0) {
        super.onActivityCreated(arg0);

        // Кастомная анимация
        getDialog().getWindow()
                .getAttributes().windowAnimations = R.style.DialogAnimation;
        BottomSheetDialog dialog = (BottomSheetDialog) getDialog();

        final FrameLayout bottomSheet = dialog.findViewById(R.id.design_bottom_sheet);

        final BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        bottomSheetBehavior.setPeekHeight(0);
        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View view, int i) {
                if (i == BottomSheetBehavior.STATE_DRAGGING) {
                    if (!isCancelable()) {
                        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                    }
                }
                if (i == BottomSheetBehavior.STATE_COLLAPSED) {
                    if (isCancelable()) {
                        getDialog().cancel();
                        getDialog().dismiss();
                    } else {
                        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                    }
                } else if (i == BottomSheetBehavior.STATE_HIDDEN) {
                    getDialog().cancel();
                    getDialog().dismiss();
                }
            }

            @Override
            public void onSlide(@NonNull View view, float v) {

            }
        });


    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.share_bottom_sheet, container, true);
        view = v;

        AutoGridLayout gridView = v.findViewById(R.id.grid);
        ImageView sendButton = v.findViewById(R.id.sendButton);
        inputMessage = v.findViewById(R.id.messageInput);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                for (long chatId : toShare) {
                    TdApi.SendMessage sendForward = new TdApi.SendMessage();
                    sendForward.chatId = chatId;
                    TdApi.InputMessageForwarded inputMessageForwarded = new TdApi.InputMessageForwarded();
                    inputMessageForwarded.messageId = postId;
                    inputMessageForwarded.fromChatId = channelId;



                    sendForward.inputMessageContent = inputMessageForwarded;

                    Telegram.getClient().send(sendForward, new Client.ResultHandler() {
                        @Override
                        public void onResult(TdApi.Object object) {

                        }
                    });

                    String message = inputMessage.getText().toString();

                    if(message.length() > 0)
                    {
                        TdApi.SendMessage sendMessage = new TdApi.SendMessage();
                        sendMessage.chatId = chatId;

                        TdApi.InputMessageText inputMessageText = new TdApi.InputMessageText();

                        TdApi.FormattedText formattedText = new TdApi.FormattedText();
                        formattedText.text = message;

                        inputMessageText.text = formattedText;

                        sendMessage.inputMessageContent = inputMessageText;

                        Telegram.getClient().send(sendMessage, new Client.ResultHandler() {
                            @Override
                            public void onResult(TdApi.Object object) {

                            }
                        });
                    }
                }
                ShareBottomSheet.this.dismiss();

            }
        });

        for (TdApi.Chat chat : avatars.keySet()) {

            Bitmap bitmap = avatars.get(chat);

            LinearLayout grid = (LinearLayout) inflater.inflate(R.layout.share_item, gridView, false);
            gridView.addView(grid);
            ImageView check = grid.findViewById(R.id.check);
            ImageView avatar = grid.findViewById(R.id.avatar);
            TextView title = grid.findViewById(R.id.name);
            long id = ((TdApi.ChatTypePrivate) chat.type).userId;
            if (bitmap != null) {
                avatar.setImageBitmap(bitmap);
            } else {
                avatar.setImageResource(R.drawable.ic_unknown);
            }

            title.setText(chat.title);

            grid.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (isPlayingAnimation) return;
                    isPlayingAnimation = true;
                    if (check.getAlpha() == 1f) {

                        AlphaAnimation alphaAnimation = new AlphaAnimation(0.5f, 1);
                        alphaAnimation.setDuration(200);
                        alphaAnimation.setFillAfter(true);
                        avatar.startAnimation(alphaAnimation);

                        scaleView(check, 1f, 0f,
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        check.setAlpha(0f);
                                        toShare.remove(id);
                                        isPlayingAnimation = false;
                                    }
                                });

                    } else {
                        AlphaAnimation alphaAnimation = new AlphaAnimation(1f, 0.5f);
                        alphaAnimation.setDuration(200);
                        alphaAnimation.setFillAfter(true);
                        avatar.startAnimation(alphaAnimation);

                        check.setAlpha(1f);
                        scaleView(check, 0f, 1f,
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        isPlayingAnimation = false;
                                        toShare.add(id);
                                    }
                                });

                    }
                }
            });
        }

        return v;
    }



    public interface BottomSheetListener {
    }

    public void scaleView(View v, float startScale, float endScale, Runnable onFinished) {
        Animation anim = new ScaleAnimation(
                startScale, endScale, // Start and end values for the X axis scaling
                startScale, endScale, // Start and end values for the Y axis scaling
                Animation.RELATIVE_TO_SELF, 0.5f, // Pivot point of X scaling
                Animation.RELATIVE_TO_SELF, 0.5f); // Pivot point of Y scaling
        anim.setFillAfter(true); // Needed to keep the result of the animation
        anim.setDuration(200);
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                onFinished.run();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        anim.setInterpolator(new DecelerateInterpolator(3f));
        v.startAnimation(anim);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (BottomSheetListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement BottomSheetListener");
        }
    }


}