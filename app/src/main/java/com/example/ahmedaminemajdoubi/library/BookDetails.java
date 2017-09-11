package com.example.ahmedaminemajdoubi.library;

import android.content.Context;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import android.content.Intent;

import cn.pedant.SweetAlert.SweetAlertDialog;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class BookDetails extends AppCompatActivity{

    //private static final String REGISTER_URL = "http://10.1.32.94:8888/Library/findBooklocation.php";
    private static final String REGISTER_URL = "http://webeleves.emines.um6p.ma/php_library/findBooklocation.php";
    private Button buttonDestination;
    private ImageView bookCover, bigImage;
    private CardView summaryCardView;
    private TextView bookTitle, bookAuthor, bookTitle2, bookAuthor2, bookEditor, bookSection, bookCote, bookIsbn, bookSummary;
    Book myBook;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_details);
        bookCover = (ImageView) findViewById(R.id.book_cover);
        bigImage = (ImageView) findViewById(R.id.bookCoverLarge);
        bookTitle = (TextView) findViewById(R.id.bookTitle);
        bookAuthor = (TextView) findViewById(R.id.bookAuthor);
        bookTitle2 = (TextView) findViewById(R.id.bookTitle2);
        bookAuthor2 = (TextView) findViewById(R.id.bookAuthor2);
        bookEditor = (TextView) findViewById(R.id.bookEditor);
        bookSection = (TextView) findViewById(R.id.bookSection);
        bookCote = (TextView) findViewById(R.id.bookCote);
        bookIsbn = (TextView) findViewById(R.id.bookIsbn);
        bookSummary = (TextView) findViewById(R.id.bookSummary);
        buttonDestination = (Button) findViewById(R.id.buttonDestination);
        summaryCardView = (CardView) findViewById(R.id.SummaryCardView);

        myBook = getIntent().getParcelableExtra("book");
        displayBook();

    }

    public void zoomImage(View view){
        ImageView image = (ImageView) view;
        if(image.getId() == R.id.book_cover){
            String bigCoverImageUri = bookCoverLink(myBook.getIsbn(),true);
            Picasso.with(this).load(bigCoverImageUri).into(bigImage);
            bigImage.setVisibility(View.VISIBLE);
        }
        else
            bigImage.setVisibility(View.INVISIBLE);
    }

     public void findBook(View view){
         if (!wifiAndLocation())
         {
             new SweetAlertDialog(this)
                     .setTitleText("Activez votre Wifi et GPS")
                     .show();
         }
         else {
             try {
                 setDestination();
             }
             catch (IOException e) {
                 e.printStackTrace();
             }
         }
     }

    private void setDestination() throws IOException {
        String cote = myBook.getCote();
        if(cote.contains(" "))
            cote = cote.split(" ")[0];
        else
            cote = cote.split("/")[0];
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(REGISTER_URL + "?cote=" + Double.parseDouble(cote)).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                call.cancel();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final Gson gson = new Gson();
                final Destination[] destinations = gson.fromJson(response.body().charStream(), Destination[].class);
                BookDetails.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        findBook(destinations);
                    }
                });
            }
        });
    }

    private void displayBook() {
        String coverImageUri = bookCoverLink(myBook.getIsbn(),false);
        Context context = this;
        Picasso.with(context).load(coverImageUri).into(bookCover);
        String fullTitle = myBook.getTitle();
        if (!TextUtils.isEmpty(myBook.getAdditionalTitle()))
            fullTitle += " - " + myBook.getAdditionalTitle();
        bookTitle.setText(fullTitle);
        bookTitle2.setText(Html.fromHtml("<b>Titre : </b>" + fullTitle));
        bookEditor.setText(Html.fromHtml("<b>Editeur : </b>"+ myBook.getEditor()));
        bookSection.setText(Html.fromHtml("<b>Section : </b>"+ myBook.getSection()));
        bookCote.setText(Html.fromHtml("<b>Côte : </b>"+ myBook.getCote()));
        if(myBook.getSummary().isEmpty())
            summaryCardView.setVisibility(View.INVISIBLE);
        else
            summaryCardView.setVisibility(View.VISIBLE);
        bookSummary.setText(Html.fromHtml("<b>Résumé : </b>" + myBook.getSummary()));
        bookIsbn.setText(Html.fromHtml("<b>ISBN : </b>" + myBook.getIsbn()));
        String allAuthors = new String();
        if (myBook.getAuthors().length != 0) {
            allAuthors = myBook.getAuthors()[0];
        }
        for (int i = 1; i < myBook.getAuthors().length; i++) {
            allAuthors += ", " + myBook.getAuthors()[i];
        }
        bookAuthor.setText(allAuthors);
        bookAuthor2.setText(Html.fromHtml("<b>Auteurs : </b>" + allAuthors));

    }

    private void findBook(Destination[] destinations){
        Intent intent = new Intent(getApplicationContext(), MapActivity.class);
        Bundle bundle1 = new Bundle();
        bundle1.putParcelable("Destination1", destinations[0]);
        intent.putExtras(bundle1);
        int length = 1;
        if(destinations.length>1){
            length=2;
            Bundle Bundle3 = new Bundle();
            Bundle3.putParcelable("Destination2", destinations[1]);
            intent.putExtras(Bundle3);
        }
        Bundle Bundle2 = new Bundle();
        Bundle2.putInt("length", length);
        intent.putExtras(Bundle2);
        String bookString = new String();
        bookString = myBook.getTitle()+" - "+ myBook.getAdditionalTitle() + "\n";
        if (myBook.getAuthors().length != 0) {
            bookString += myBook.getAuthors()[0];
        }
        for (int i = 1; i < myBook.getAuthors().length; i++) {
            bookString += ", " + myBook.getAuthors()[i];
        }
        intent.putExtra("book",bookString);
        intent.putExtra("isbn",myBook.getIsbn().toString());
        startActivity(intent);
    }

    public static String bookCoverLink(String isbn13, boolean length){
        isbn13 = isbn13.replaceAll("-","");
        isbn13 = isbn13.substring(3);
        isbn13 = isbn13.substring(0, isbn13.length() - 1);
        int[] digits = new int[9];
        for(int i=0;i<9;i++) {
            digits[i] = Character.getNumericValue(isbn13.charAt(i));
        }
        int lastDigit = 0;
        for(int i=10;i>1;i--)
            lastDigit += i * digits[10-i];
        lastDigit = 11 - (lastDigit%11);
        String isbn10 = isbn13 + String.valueOf(lastDigit);
        String l = new String();
        if(length) l = "L";
        else l ="T";

        return "https://images-na.ssl-images-amazon.com/images/P/"+isbn10+".01."+l+".jpg";//SCLZZZZZZZ.jpg";
    }

    @Override
    public void onBackPressed(){
        this.finish();
    }

    public boolean wifiAndLocation() {
        ConnectivityManager conMgr = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = conMgr.getActiveNetworkInfo();
        final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );
        return !(netInfo == null || !netInfo.isConnected() || !netInfo.isAvailable() || !manager.isProviderEnabled(LocationManager.GPS_PROVIDER));
    }
    @SuppressWarnings("deprecation")
    public static Spanned fromHtml(String html){
        Spanned result;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            result = Html.fromHtml(html,Html.FROM_HTML_MODE_LEGACY);
        } else {
            result = Html.fromHtml(html);
        }
        return result;
    }
}
