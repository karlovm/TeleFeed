package ru.etysoft.telefeed.activities.main;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.text.format.DateUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import org.drinkless.td.libcore.telegram.TdApi;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;

import ru.etysoft.telefeed.CacheUtils;
import ru.etysoft.telefeed.R;
import ru.etysoft.telefeed.SearchActivity;
import ru.etysoft.telefeed.api.MediaInfo;
import ru.etysoft.telefeed.api.NewsGetter;
import ru.etysoft.telefeed.bottomsheet.ShareBottomSheet;
import ru.etysoft.telefeed.views.FeedVideoPlayer;
import ru.etysoft.telefeed.views.ImagesPager;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.ViewHolder> {
    private LayoutInflater mInflater;
    private List<Article> articles;
    private Activity activity;



    public NewsAdapter(Activity context, List<Article> data) {
        this.mInflater = LayoutInflater.from(context);
        this.articles = data;
        this.activity = context;

    }




    public static class Article implements Cloneable {
        private List<MediaInfo> mediaList;
        private Bitmap avatar;
        private String text;
        private int views;
        private int comments;
        private String channelName;
        private int date;
        private String commentsCount;


        private long messageId;
        private TdApi.Message message;

        @Override
        public Object clone() throws CloneNotSupportedException {
            return super.clone();    // return shallow copy
        }
        public Article(List<MediaInfo> mediaList, String text, String channelName, int date, String commentsCount, TdApi.Message message
        , int views, int comments) {
            this.mediaList = mediaList;
            this.text = text;
            this.channelName = channelName;
            this.date = date;
            this.message = message;
            this.commentsCount = commentsCount;
            this.views = views;
            this.comments = comments;
        }

        public int getViews() {
            return views;
        }

        public void setViews(int views) {
            this.views = views;
        }

        public int getComments() {
            return comments;
        }

        public void setComments(int comments) {
            this.comments = comments;
        }

        public void setChannelName(String channelName) {
            this.channelName = channelName;
        }

        public void setDate(int date) {
            this.date = date;
        }

        public void setCommentsCount(String commentsCount) {
            this.commentsCount = commentsCount;
        }

        public TdApi.Message getMessage() {
            return message;
        }

        public Bitmap getAvatar() {
            return avatar;
        }

        public void setAvatar(Bitmap avatar) {
            this.avatar = avatar;
        }

        public void setMessage(TdApi.Message message) {
            this.message = message;
        }

        public long getMessageId() {
            return messageId;
        }

        public void setMessageId(long messageId) {
            this.messageId = messageId;
        }

        public void setText(String text) {
            this.text = text;
        }

        public void setMediaList(List<MediaInfo> mediaList) {
            this.mediaList = mediaList;
        }

        public List<MediaInfo> getMediaList() {
            return mediaList;
        }

        public String getText() {
            return text;
        }

        public String getChannelName() {
            return channelName;
        }

        public int getDate() {
            return date;
        }



        public String getCommentsCount() {
            return commentsCount;
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.article, parent, false);
        return new ViewHolder(view);
    }
    public static float convertPixelsToDp(float px, Context context){
        return px / ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    public static float convertDpToPixel(float dp, Context context){
        return dp * ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);

        FeedVideoPlayer.resetAllFocus();



    }



    @Override
    public void onViewAttachedToWindow(@NonNull ViewHolder holder) {
        super.onViewAttachedToWindow(holder);

        holder.imagesPager.setImages(holder.mediaInfos);




    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Article article = articles.get(position);
       // holder.viewPager2.setVisibility(View.GONE);
        //holder.imagesPager.resetPivot();

        if(!CacheUtils.getInstance().getBoolean("dynamicArt", activity.getApplicationContext()))
        {
            holder.viewPager2.getLayoutParams().height = (int) convertDpToPixel(300, activity);
        }

        if(article.getMediaList().size() == 0)
        {
            holder.viewPager2.setVisibility(View.GONE);
        }
        else
        {
            holder.viewPager2.setVisibility(View.VISIBLE);
        }

        if(article.getText().length() == 0)
        {
            holder.textView.setVisibility(View.GONE);
        }
        else
        {
            holder.textView.setVisibility(View.VISIBLE);
            holder.textView.setTextIsSelectable(false);
            holder.textView.measure(-1, -1);
            holder.textView.setTextIsSelectable(true);
        }

        holder.channelView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try
                {

                    if(MainActivity.isDebug) {
                        holder.textView.setText(article.getMessage().toString());
                    }
                    SearchActivity.query = "channel:" + article.getMessage().chatId;


                    if(!(activity instanceof SearchActivity))
                    {
                        SearchActivity.articles = articles;
                        Intent intent = new Intent(activity, SearchActivity.class);
                        activity.startActivity(intent);
                    }
                    else
                    {
                        SearchActivity searchActivity = (SearchActivity) activity;
                        searchActivity.processSearch();

                    }

                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });

        if(article.views == 0)
        {
            holder.viewsLayout.setVisibility(View.GONE);
        }
        else
        {
            holder.viewsLayout.setVisibility(View.VISIBLE);
            holder.viewsCount.setText(String.valueOf(article.views));
        }



        List<MediaInfo> images = articles.get(position).getMediaList();
        Collections.reverse(images);
        holder.mediaInfos = images;
        holder.imagesPager.setImages(images);

        if(article.getMediaList().size() > 1)
        {
            holder.viewPager2.setOffscreenPageLimit(5);
            holder.mediaCount.setVisibility(View.VISIBLE);
            holder.mediaCount.setText(mInflater.getContext().getResources().getString(R.string.media_counter)
            .replace("%s", String.valueOf(holder.viewPager2.getCurrentItem() + 1))
            .replace("%p", String.valueOf(article.getMediaList().size())));
        }
        else
        {
            holder.mediaCount.setVisibility(View.GONE);
        }
        SimpleDateFormat hours = new SimpleDateFormat("HH:ss");
        SimpleDateFormat year = new SimpleDateFormat("dd.MM.yy");
        long time = (long)article.date*1000;
        String hoursString = hours.format(time);
        String yearString = year.format(time);
        if( DateUtils.isToday(time))
        {
            holder.dateView.setText(mInflater.getContext().getResources().getString(R.string.today)
                    + " " + hoursString);
        }
        else if( DateUtils.isToday(time + DateUtils.DAY_IN_MILLIS))
        {
            holder.dateView.setText(mInflater.getContext().getResources().getString(R.string.yesterday)
                    + " " +hoursString);
        }
        else
        {
            holder.dateView.setText(yearString + ", " + hoursString);
        }


        holder.viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                holder.mediaCount.setText(mInflater.getContext().getResources().getString(R.string.media_counter)
                        .replace("%s", String.valueOf(holder.viewPager2.getCurrentItem() + 1))
                                .replace("%p", String.valueOf(article.getMediaList().size())));
            }
        });


        if(article.comments == 0)
        {
            holder.commentsCount.setVisibility(View.GONE);
        }
        else
        {
            holder.commentsCount.setVisibility(View.VISIBLE);
            holder.commentsCount.setText(String.valueOf(article.comments));
        }


        holder.commentsBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CommentsActivity.message = article.message;
                Intent intent = new Intent(activity, CommentsActivity.class);
                activity.startActivity(intent);
            }
        });


        holder.share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ShareBottomSheet.postId = article.getMessage().id;
                ShareBottomSheet.channelId = article.getMessage().chatId;
                ShareBottomSheet bottomSheet = new ShareBottomSheet();

                bottomSheet.show(((FragmentActivity)activity).getSupportFragmentManager(), "news");
            }
        });
        holder.channelView.setText(article.getChannelName());

        String text = article.getText();

        if(text.length() > 200)
        {
            text = text.substring(0, 190) + "...";
            holder.readMore.setVisibility(View.VISIBLE);
        }
        else
        {
            holder.readMore.setVisibility(View.GONE);
        }

        holder.readMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.textView.setText(article.getText());
                holder.readMore.setVisibility(View.GONE);
            }
        });

        holder.textView.setText(text);
        holder.viewPager2.setCurrentItem(0);
        holder.avatar.setImageBitmap(NewsGetter.supergroupsAvatars.get(
                article.getMessage().chatId));
        holder.rootView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN ||
                        motionEvent.getAction() == MotionEvent.ACTION_HOVER_ENTER )
                {
                    //holder.focus();
                }
                return true;
            }
        });

    }

    @Override
    public int getItemCount() {
        return articles.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ViewPager2 viewPager2;
        ImagesPager imagesPager;
        TextView textView;
        TextView channelView;
        LinearLayout viewsLayout;
        TextView viewsCount;
        TextView commentsCount;
        TextView mediaCount;
        TextView dateView;
        ImageView avatar;
        LinearLayout commentsBox;
        LinearLayout share;
        TextView readMore;
        List<MediaInfo> mediaInfos;
        View rootView;

        ViewHolder(View itemView) {
            super(itemView);
            rootView = itemView;
            viewPager2 = itemView.findViewById(R.id.imagesPager);
            channelView = itemView.findViewById(R.id.channelNameView);
            textView = itemView.findViewById(R.id.textView);
            viewsCount = itemView.findViewById(R.id.viewCount);
            avatar = itemView.findViewById(R.id.channelAvatar);
            viewsLayout = itemView.findViewById(R.id.viewsContainer);
            commentsBox = itemView.findViewById(R.id.commentsBox);
            mediaCount = itemView.findViewById(R.id.mediaCount);
            dateView = itemView.findViewById(R.id.dateView);
            readMore = itemView.findViewById(R.id.readMore);
            commentsCount = itemView.findViewById(R.id.commentsCountView);
            share = itemView.findViewById(R.id.share);
            imagesPager = new ImagesPager(viewPager2, activity);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            // if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }


}
