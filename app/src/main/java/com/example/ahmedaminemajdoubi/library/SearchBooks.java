package com.example.ahmedaminemajdoubi.library;

import android.content.Context;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import com.google.gson.Gson;

import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import java.io.IOException;
import android.content.Intent;
import android.widget.Toast;

import cn.pedant.SweetAlert.SweetAlertDialog;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class SearchBooks extends AppCompatActivity implements View.OnClickListener {

    private static final String REGISTER_URL = "http://10.1.32.94:8888/Library/searchBooks.php";
    private EditText editText;
    private Button button;
    List<Book> bookList;
    private RecyclerView recyclerView;
    private BookAdapter bookAdapter;
    AlertDialog.Builder builder;

    public SearchBooks(){
        bookList = new ArrayList<>();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_books);
        editText = (EditText) findViewById(R.id.editText);
        button = (Button) findViewById(R.id.button);
        button.setOnClickListener(this);
        recyclerView = (RecyclerView) findViewById(R.id.books_recycler_view);
        bookAdapter = new BookAdapter(getApplicationContext(), bookList);
        RecyclerView.LayoutManager myLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(myLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        recyclerView.setAdapter(bookAdapter);
    }

    @Override
    public void onClick(View v) {
        if(v == button){
            if (!isOnline())
            {
                new SweetAlertDialog(this)
                        .setTitleText("Vérifiez votre connexion Wi-fi")
                        .show();
            }
            else {
                try {
                    searchBooks();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void searchBooks() throws IOException {
        String title = editText.getText().toString().trim().toLowerCase();
        if(title.isEmpty())
            Toast.makeText(getApplicationContext(), "Entrez un mot dans la barre de recherche",
                Toast.LENGTH_SHORT).show();
        else {
            final SweetAlertDialog pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
            pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
            pDialog.setTitleText("Chargement des résultats");
            pDialog.setCancelable(false);
            pDialog.show();
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(REGISTER_URL + "?title=%" + title + "%").build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    call.cancel();
                    Toast.makeText(getApplicationContext(), "Echec de la recherche, veuillez essayer plus tard",
                            Toast.LENGTH_LONG).show();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    final Gson gson = new Gson();
                    final Book[] books = gson.fromJson(response.body().charStream(), Book[].class);
                    bookList.clear();
                    for (int i = 0; i < books.length; i++) {
                        bookList.add(books[i]);
                    }
                    SearchBooks.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            bookAdapter.notifyDataSetChanged();
                            pDialog.cancel();
                            if(bookList.isEmpty())
                                Toast.makeText(getApplicationContext(), "Aucun resultat trouvé",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });

            recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), recyclerView, new RecyclerTouchListener.ClickListener() {
                @Override
                public void onClick(View view, int position) {
                    if (!isOnline())
                    {
                        new SweetAlertDialog(getApplicationContext())
                                .setTitleText("Vérifiez votre connexion Wi-fi")
                                .show();
                    }
                    else {
                        Book myBook = bookList.get(position);
                        Intent intent = new Intent(getApplicationContext(), BookDetails.class);
                        Bundle bundle = new Bundle();
                        bundle.putParcelable("book", myBook);
                        intent.putExtras(bundle);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    }
                }

                @Override
                public void onLongClick(View view, int position) {

                }
            }));
        }
    }

    public boolean isOnline() {
        ConnectivityManager conMgr = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = conMgr.getActiveNetworkInfo();

        if(netInfo == null || !netInfo.isConnected() || !netInfo.isAvailable()){
            return false;
        }
        return true;
    }
}
