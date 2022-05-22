package ru.etysoft.telefeed.activities.main;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import org.drinkless.td.libcore.telegram.TdApi;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;

import ru.etysoft.telefeed.R;
import ru.etysoft.telefeed.api.NewsGetter;
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
        private List<Bitmap> imagesList;
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
        public Article(List<Bitmap> imagesList, String text, String channelName, int date, String commentsCount, TdApi.Message message
        , int views, int comments) {
            this.imagesList = imagesList;
            this.text = text;
            this.channelName = channelName;
            this.date = date;
            this.message = message;
            this.commentsCount = commentsCount;
            this.views = views;
            this.comments = comments;
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

        public void setImagesList(List<Bitmap> imagesList) {
            this.imagesList = imagesList;
        }

        public List<Bitmap> getImagesList() {
            return imagesList;
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

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Article article = articles.get(position);
       // holder.viewPager2.setVisibility(View.GONE);
        //holder.imagesPager.resetPivot();

        if(article.getImagesList().size() == 0)
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
        }

        holder.channelView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try
                {

                    holder.textView.setText(article.getMessage().toString());
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

        List<Bitmap> images = articles.get(position).getImagesList();
        Collections.reverse(images);
        holder.imagesPager.setImages(images);

        if(article.getImagesList().size() > 1)
        {
            holder.mediaCount.setVisibility(View.VISIBLE);
            holder.mediaCount.setText(mInflater.getContext().getResources().getString(R.string.media_counter)
            .replace("%s", String.valueOf(holder.viewPager2.getCurrentItem() + 1))
            .replace("%p", String.valueOf(article.getImagesList().size())));
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
                                .replace("%p", String.valueOf(article.getImagesList().size())));
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
        holder.channelView.setText(article.getChannelName());
        holder.textView.setText(article.getText());
        holder.viewPager2.setCurrentItem(0);
        holder.avatar.setImageBitmap(NewsGetter.supergroupsAvatars.get(
                article.getMessage().chatId));

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

        ViewHolder(View itemView) {
            super(itemView);
            viewPager2 = itemView.findViewById(R.id.imagesPager);
            channelView = itemView.findViewById(R.id.channelNameView);
            textView = itemView.findViewById(R.id.textView);
            viewsCount = itemView.findViewById(R.id.viewCount);
            avatar = itemView.findViewById(R.id.channelAvatar);
            viewsLayout = itemView.findViewById(R.id.viewsContainer);
            commentsBox = itemView.findViewById(R.id.commentsBox);
            mediaCount = itemView.findViewById(R.id.mediaCount);
            dateView = itemView.findViewById(R.id.dateView);
            commentsCount = itemView.findViewById(R.id.commentsCountView);
            imagesPager = new ImagesPager(viewPager2, activity);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            // if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }


}
