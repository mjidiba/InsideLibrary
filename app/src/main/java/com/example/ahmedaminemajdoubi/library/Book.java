package com.example.ahmedaminemajdoubi.library;

import android.os.Parcelable;
import android.os.Parcel;

/**
 * Created by AhmedAmineMajdoubi on 16/07/2017.
 */

public class Book implements Parcelable{

    private int id;
    private String title;
    private String additionalTitle;
    private String[] authors;
    private String editor;
    private String section;
    private String cote;
    private String isbn;
    private String summary;

    public Book() {

    }

    public Book(int id, String title) {
        this.id = id;
        this.title = title;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setAdditionalTitle(String additionalTitle) {
        this.additionalTitle = additionalTitle;
    }

    public void setAuthors(String[] authors) {
        this.authors = authors;
    }

    public void setEditor(String editor) {
        this.editor = editor;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public void setCote(String cote) {
        this.cote = cote;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getAdditionalTitle() { return additionalTitle; }

    public String[] getAuthors() {
        return authors;
    }

    public String getEditor() {
        return editor;
    }

    public String getSection() {
        return section;
    }

    public String getSummary() {
        return summary;
    }

    public String getCote() {
        return cote;
    }

    public String getIsbn() { return isbn; }

    public static final Parcelable.Creator<Book> CREATOR = new Creator<Book>() {
        public Book createFromParcel(Parcel source) {
            Book mBook = new Book();
            mBook.id = source.readInt();
            mBook.title = source.readString();
            mBook.additionalTitle = source.readString();
            mBook.authors = source.createStringArray();
            //source.readStringArray(mBook.authors);
            mBook.editor = source.readString();
            mBook.section = source.readString();
            mBook.summary = source.readString();
            mBook.cote = source.readString();
            mBook.isbn = source.readString();
            return mBook;
        }
        public Book[] newArray(int size) {
            return new Book[size];
        }
    };

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeInt(id);
        parcel.writeString(title);
        parcel.writeString(additionalTitle);
        parcel.writeStringArray(authors);
        parcel.writeString(editor);
        parcel.writeString(section);
        parcel.writeString(summary);
        parcel.writeString(cote);
        parcel.writeString(isbn);
    }
}

