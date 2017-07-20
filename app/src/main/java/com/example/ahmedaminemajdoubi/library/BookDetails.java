package com.example.ahmedaminemajdoubi.library;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import android.content.Intent;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class BookDetails extends AppCompatActivity implements View.OnClickListener {

    private static final String REGISTER_URL = "http://10.1.32.94:8888/Library/findBooklocation.php";
    private Button buttonDestination;
    private ImageView bookCover;
    private TextView bookTitle, bookAuthor, bookEditor, bookSection, bookCote, bookIsbn, bookSummary;
    Book myBook;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_details);
        bookCover = (ImageView) findViewById(R.id.bookCover);
        bookTitle = (TextView) findViewById(R.id.bookTitle);
        bookAuthor = (TextView) findViewById(R.id.bookAuthor);
        bookEditor = (TextView) findViewById(R.id.bookEditor);
        bookSection = (TextView) findViewById(R.id.bookSection);
        bookCote = (TextView) findViewById(R.id.bookCote);
        bookIsbn = (TextView) findViewById(R.id.bookIsbn);
        bookSummary = (TextView) findViewById(R.id.bookSummary);
        buttonDestination = (Button) findViewById(R.id.buttonDestination);
        buttonDestination.setOnClickListener(this);

        myBook = getIntent().getParcelableExtra("book");
        displayBook();

    }

    @Override
    public void onClick(View v) {

         if(v == buttonDestination){
             try {
                 setDestination();
             } catch (IOException e) {
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
        String coverImageUri = bookCoverLink(myBook.getIsbn());
        Context context = this;
        Picasso.with(context).load(coverImageUri).resize(0, 700).into(bookCover);
        String fullTitle = myBook.getTitle();
        if (!TextUtils.isEmpty(myBook.getAdditionalTitle()))
            fullTitle += " - " + myBook.getAdditionalTitle();
        bookTitle.setText("Titre : " + fullTitle);
        bookEditor.setText("Editeur : " + myBook.getEditor());
        bookSection.setText("Section : " + myBook.getSection());
        bookCote.setText("Cote : " + myBook.getCote());
        bookSummary.setText("Resume : \n" + myBook.getSummary());
        bookCote.setText("Cote : " + myBook.getCote());
        bookIsbn.setText("ISBN : " + myBook.getIsbn());
        String allAuthors = new String();
        if (myBook.getAuthors().length != 0) {
            allAuthors = myBook.getAuthors()[0];
        }
        for (int i = 1; i < myBook.getAuthors().length; i++) {
            allAuthors += ", " + myBook.getAuthors()[i];
        }
        bookAuthor.setText("Auteurs : " + allAuthors);
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
        startActivity(intent);
    }

    public String bookCoverLink(String isbn13){
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
        return "https://images-na.ssl-images-amazon.com/images/P/"+isbn10+".01._SCRM";//SCLZZZZZZZ.jpg";
    }

    @Override
    public void onBackPressed(){
        Log.e("a","a");
        this.finish();
    }
}
