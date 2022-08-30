package ru.etysoft.telefeed.bottomsheet;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.drinkless.td.libcore.telegram.TdApi;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

import ru.etysoft.telefeed.CacheUtils;
import ru.etysoft.telefeed.R;
import ru.etysoft.telefeed.activities.auth.WelcomeActivity;
import ru.etysoft.telefeed.api.NewsGetter;
import ru.etysoft.telefeed.views.AutoGridLayout;

public class IgnoreBottomSheet extends BottomSheetDialogFragment {
    private BottomSheetListener mListener;


    private View view;

    public static long postId = 0;
    public static long channelId = 0;


    private ArrayList<Long> toIgnore = new ArrayList<>();
    private boolean isPlayingAnimation = false;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Пустой фон
        setStyle(BottomSheetDialogFragment.STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme);

        JSONArray jsonArray = new JSONArray();

        if(CacheUtils.getInstance().hasKey("ignoreList", getContext()))
        {
            try {
                jsonArray = new JSONArray(CacheUtils.getInstance().getString("ignoreList", getContext()));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }


        for(int i = 0; i < jsonArray.length(); i++)
        {
            try {
                toIgnore.add(jsonArray.getLong(i));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

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

    public void updateCounter()
    {
        TextView textView = view.findViewById(R.id.selectedCounter);

        String text = getResources().getString(R.string.ignore_count).replace("%s", String.valueOf(toIgnore.size()));
        textView.setText(text);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.ignore_bottom_sheet, container, true);

        view = v;
        updateCounter();
        AutoGridLayout gridView = v.findViewById(R.id.grid);
        ImageView sendButton = v.findViewById(R.id.sendButton);


        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                JSONArray jsonArray = new JSONArray();

                for(long chatId : toIgnore)
                {
                    jsonArray.put(chatId);
                }
                try {
                    CacheUtils.getInstance().setString("ignoreList", jsonArray.toString(), getContext());
                    IgnoreBottomSheet.this.dismiss();
                    Toast toast = Toast.makeText(getActivity(),
                            getResources().getString(R.string.ignore_apply), Toast.LENGTH_SHORT);
                    toast.show();
                }
                catch (Exception e)
                {

                }



            }
        });

        for (long chatId : NewsGetter.supergroupsAvatars.keySet()) {

            Bitmap bitmap = NewsGetter.supergroupsAvatars.get(chatId);

            LinearLayout grid = (LinearLayout) inflater.inflate(R.layout.share_item, gridView, false);
            gridView.addView(grid);
            ImageView check = grid.findViewById(R.id.check);
            ImageView avatar = grid.findViewById(R.id.avatar);
            TextView title = grid.findViewById(R.id.name);

            if (bitmap != null) {
                avatar.setImageBitmap(bitmap);
            } else {
                avatar.setImageResource(R.drawable.ic_unknown);
            }

            TdApi.Chat chat = NewsGetter.supergroups.get(chatId);
            title.setText(chat.title);


            if(toIgnore.contains(chatId))
            {
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
                                updateCounter();
                            }
                        });
            }

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
                                        toIgnore.remove(chatId);
                                        updateCounter();
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
                                        toIgnore.add(chatId);
                                        updateCounter();
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