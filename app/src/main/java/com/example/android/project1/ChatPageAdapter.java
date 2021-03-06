package com.example.android.project1;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by Joey on 2/21/2016.
 */
//TODO listview stutters, maybe compress images and find an alternative for toggling the imageview/textview
public class ChatPageAdapter extends CursorAdapter {

    Context context;
    Cursor cursor;

    String sender;
    String message;
    String timestamp;
    String userName;
    Bitmap image;
    String messageState;
    String messageType;

    SimpleDateFormat simpleDateFormat;
    SimpleDateFormat simpleDateFormatToDisplay;
    Date date, currentDate;

    int senderColumnIndex;
    int messageColumnIndex;
    int timestampColumnIndex;
    int messageStateColumnIndex;
    int messageTypeColumnIndex;
    int imagePathColumnIndex;

    ViewHolder viewHolder;
    ImageDecoder imageDecoder;
    static ImagePopup imgPopup;
    String imagePath;
    static boolean isImageOpened = false;
    public ChatPageAdapter(Context context, Cursor cursor) {
        super(context, cursor);
        this.context = context;
        this.cursor = cursor;
        messageColumnIndex = cursor.getColumnIndexOrThrow(DBMessagesContract.MessageEntry.COLUMN_NAME_CONTENT);
        senderColumnIndex = cursor.getColumnIndexOrThrow(DBMessagesContract.MessageEntry.COLUMN_NAME_SENDER);
        timestampColumnIndex = cursor.getColumnIndexOrThrow(DBMessagesContract.MessageEntry.COLUMN_NAME_TIME);
        messageStateColumnIndex = cursor.getColumnIndexOrThrow(DBMessagesContract.MessageEntry.COLUMN_NAME_MESSAGE_STATE);
        messageTypeColumnIndex = cursor.getColumnIndexOrThrow(DBMessagesContract.MessageEntry.COLUMN_NAME_MESSAGE_TYPE);
        imagePathColumnIndex = cursor.getColumnIndexOrThrow(DBMessagesContract.MessageEntry.COLUMN_NAME_IMAGE_PATH);

        SharedPreferences prefs = context.getSharedPreferences("com.example.android.project1.RegistrationPreferences", 0);
        userName = prefs.getString("userName","Me");
        simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd-HH:mm:ss", Locale.US);
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        simpleDateFormatToDisplay = new SimpleDateFormat("h:mm a", Locale.getDefault());
        simpleDateFormatToDisplay.setTimeZone(TimeZone.getDefault());
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent){
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.message_list, parent, false);

        ViewHolder viewHolder = new ViewHolder();
        viewHolder.messageView = (RelativeLayout) view.findViewById(R.id.message_view);
        viewHolder.messageText = (TextView) view.findViewById(R.id.message_text);
        viewHolder.senderText = (TextView) view.findViewById(R.id.sender_username_box);
        viewHolder.timeBox = (TextView) view.findViewById(R.id.time_box);
        viewHolder.messageImage = (ImageView) view.findViewById(R.id.message_image);
        viewHolder.progressBar = (ProgressBar) view.findViewById(R.id.image_loading_progress_bar);

        //set up the view parameters programmatically
        /*viewHolder.messageViewParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        viewHolder.messageViewParams.setMargins(4,4,4,4);
        viewHolder.messageView.setLayoutParams(viewHolder.messageViewParams);

        viewHolder.senderTextParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        viewHolder.senderTextParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
        viewHolder.senderTextParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, R.id.message_view);
        viewHolder.senderText.setLayoutParams(viewHolder.senderTextParams);

        viewHolder.messageTextParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        //viewHolder.messageTextParams.setMargins(4,0,0,0);
        viewHolder.messageTextParams.addRule(RelativeLayout.BELOW, R.id.sender_username_box);
        viewHolder.messageTextParams.addRule(RelativeLayout.ALIGN_PARENT_START, RelativeLayout.TRUE);
        viewHolder.messageTextParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
        viewHolder.messageText.setLayoutParams(viewHolder.messageTextParams);

        viewHolder.timeBoxParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        //viewHolder.timeBoxParams.setMargins(4,4,0,0);
        viewHolder.timeBoxParams.addRule(RelativeLayout.ALIGN_PARENT_END, RelativeLayout.TRUE);
        viewHolder.timeBoxParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        viewHolder.timeBoxParams.addRule(RelativeLayout.RIGHT_OF, R.id.message_text);
        viewHolder.timeBox.setLayoutParams(viewHolder.timeBoxParams);*/

        viewHolder.messageViewParams = (LinearLayout.LayoutParams) viewHolder.messageView.getLayoutParams();
        viewHolder.senderText.setVisibility(View.GONE);

        view.setTag(viewHolder);

        return view;
    }

    @Override
    public void bindView(View view, final Context context, final Cursor cursor){

        viewHolder = (ViewHolder) view.getTag();

        sender = cursor.getString(senderColumnIndex);
        timestamp = cursor.getString(timestampColumnIndex);
        messageState = cursor.getString(messageStateColumnIndex);

        //Check type of message, text or image
        messageType = cursor.getString(messageTypeColumnIndex);
        //If the current message is a text message
        if(messageType.equals("text")){
            viewHolder.progressBar.setVisibility(View.GONE);
            viewHolder.messageText.setVisibility(View.VISIBLE);
            message = cursor.getString(messageColumnIndex);
            if(message != null){
                viewHolder.messageText.setText(message);
            }
            else{
                viewHolder.messageText.setText("null message");
            }
            if(messageState.equals("unsent")){
                viewHolder.messageText.setTextColor(Color.LTGRAY);
            }
            else{
                viewHolder.messageText.setTextColor(Color.BLACK);
            }
            viewHolder.messageImage.setVisibility(View.GONE);
        }
        //if the current message is an image
        else if(messageType.equals("image")){
            viewHolder.messageImage.setVisibility(View.VISIBLE);
            viewHolder.messageText.setText("Loading picture...");
            imagePath = cursor.getString(imagePathColumnIndex);
            if( messageState.equals("downloaded") || messageState.equals("sent") || messageState.equals("unsent") ){
                viewHolder.progressBar.setVisibility(View.GONE);
                image = decodeSampledBitmap(imagePath, 400, 400);
                viewHolder.messageImage.setImageBitmap(image);

                //TODO fix the asynctask populating imageviews in wrong order
                /*//Cancel loading an image if it's already in progress
                if(imageDecoder != null)
                    imageDecoder.cancel(true);*/
                /*imageDecoder = new ImageDecoder(viewHolder.messageImage);
                imageDecoder.execute(imagePath);*/
            }
            else if(messageState.equals("sending")){
                viewHolder.progressBar.setVisibility(View.VISIBLE);
                image = decodeSampledBitmap(imagePath, 400, 400);
                viewHolder.messageImage.setImageBitmap(image);
            }
            else if(messageState.equals("errorInDownload")){
                viewHolder.progressBar.setVisibility(View.GONE);
                viewHolder.messageImage.setImageResource(R.drawable.image_broken);
            }
            else {
                viewHolder.progressBar.setVisibility(View.GONE);
                viewHolder.messageImage.setImageResource(R.drawable.image_broken);
            }
            viewHolder.messageText.setVisibility(View.GONE);
        }
        viewHolder.senderText.setText(sender);

        if(userName.equals(sender)) {
            viewHolder.messageViewParams.gravity = Gravity.RIGHT;
            viewHolder.messageView.setBackgroundResource(R.drawable.background_with_shadow);

            //viewHolder.senderText.setVisibility(View.GONE);
        }
        else {
            viewHolder.messageViewParams.gravity = Gravity.LEFT;
            viewHolder.messageView.setBackgroundResource(R.drawable.background_with_shadow_1);
            //viewHolder.senderText.setVisibility(View.VISIBLE);
        }

        if(timestamp != null){
            try {
                date = simpleDateFormat.parse(timestamp);
                currentDate = new Date();
                if(date.compareTo(currentDate) > 0){
                    viewHolder.messageView.setBackgroundColor(Color.YELLOW);
                }
                else{
                    //viewHolder.messageView.setBackgroundColor(Color.GRAY);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
            timestamp = simpleDateFormatToDisplay.format(date);
            viewHolder.timeBox.setText(timestamp);
        }
        else {
            viewHolder.timeBox.setText("null time");
        }

        //use the tag to determine which row is selected in the onclicklistener below
        viewHolder.tag = cursor.getPosition();
        viewHolder.messageImage.setTag(viewHolder);

        viewHolder.messageImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewHolder = (ViewHolder) v.getTag();
                int pos = (int) viewHolder.tag;
                cursor.moveToPosition(pos);
                String imgPath = cursor.getString(imagePathColumnIndex);
                imgPopup = new ImagePopup(context, v, imgPath);
                isImageOpened = true;
            }
        });
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        //Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            //Calculate the largest inSampleSize value that is a power of 2 and keeps both
            //height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    public static Bitmap decodeSampledBitmap(String imagePath, int reqWidth, int reqHeight) {
        //First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath, options);

        //Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        //Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(imagePath, options);
    }

    static class ViewHolder {
        RelativeLayout messageView;
        TextView messageText;
        TextView senderText;
        TextView timeBox;
        ImageView messageImage;
        ProgressBar progressBar;
        int tag;
        LinearLayout.LayoutParams messageViewParams;
        /*RelativeLayout.LayoutParams timeBoxParams;
        RelativeLayout.LayoutParams messageTextParams;
        RelativeLayout.LayoutParams senderTextParams;*/
    }

    public class ImageDecoder extends AsyncTask<String, Void, Bitmap>{

        private final WeakReference<ImageView> imageViewWeakReference;
        public ImageDecoder(ImageView imgView){
            imageViewWeakReference = new WeakReference<>(imgView);
        }

        @Override
        protected Bitmap doInBackground(String... path){
            Bitmap image = BitmapFactory.decodeFile(path[0]);
            return image;
        }

        @Override
        protected void onPostExecute(Bitmap decodedImage){
            if(imageViewWeakReference != null){
                ImageView imageView = imageViewWeakReference.get();
                if(imageView != null){
                    imageView.setImageBitmap(decodedImage);
                }
            }
        }
    }
}
