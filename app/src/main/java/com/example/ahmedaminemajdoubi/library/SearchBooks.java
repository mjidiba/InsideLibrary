package com.example.ahmedaminemajdoubi.library;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.support.v7.widget.SearchView;
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


public class SearchBooks extends AppCompatActivity {

    private static final String REGISTER_URL = "http://10.1.32.94:8888/Library/searchBooks.php";
    List<Book> bookList;
    private RecyclerView recyclerView;
    private BookAdapter bookAdapter;
    private SearchView searchView;
    private static final int REQUEST_CODE_ACCESS_COARSE_LOCATION = 1;


    public SearchBooks(){
        bookList = new ArrayList<>();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_books);
        recyclerView = (RecyclerView) findViewById(R.id.books_recycler_view);
        bookAdapter = new BookAdapter(getApplicationContext(), bookList);
        RecyclerView.LayoutManager myLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(myLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        recyclerView.setAdapter(bookAdapter);
        searchView = (SearchView) findViewById(R.id.search);
        searchView.setQueryHint("Entrez le nom du livre ou auteur");
        EditText searchEditText = searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
        searchEditText.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
        searchEditText.setHintTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                if (!isOnline())
                {
                    new SweetAlertDialog(getApplicationContext())
                            .setTitleText("Vérifiez votre connexion Wi-fi")
                            .show();
                }
                else {
                    try {
                        searchBooks(query);
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                return false;

            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return true;
            }
        });
        ensurePermissions();
    }

    private void searchBooks(String title) throws IOException {
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

        return !(netInfo == null || !netInfo.isConnected() || !netInfo.isAvailable());
    }
    private void ensurePermissions() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // we don't have access to coarse locations, hence we have not access to wifi either
            // check if this requires explanation to user
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)) {

                new AlertDialog.Builder(this)
                        .setTitle(R.string.location_permission_request_title)
                        .setMessage(R.string.location_permission_request_rationale)
                        .setPositiveButton("Accepter", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(SearchBooks.this,
                                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                                        REQUEST_CODE_ACCESS_COARSE_LOCATION);
                            }
                        })
                        .setNegativeButton("Deny", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(SearchBooks.this,
                                        "Accès à la position interdit",
                                        Toast.LENGTH_LONG).show();
                            }
                        })
                        .show();

            } else {

                // ask user for permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        REQUEST_CODE_ACCESS_COARSE_LOCATION);

            }

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        switch (requestCode) {
            case REQUEST_CODE_ACCESS_COARSE_LOCATION:

                if (grantResults.length == 0
                        || grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    Toast.makeText(this, "Accès à la position interdit",
                            Toast.LENGTH_LONG).show();
                }
                break;
        }

    }



}
