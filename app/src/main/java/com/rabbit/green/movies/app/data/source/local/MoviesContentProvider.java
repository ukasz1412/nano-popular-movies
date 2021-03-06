package com.rabbit.green.movies.app.data.source.local;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class MoviesContentProvider extends ContentProvider {

    private MoviesDBHelper dbHelper;

    private static final UriMatcher URI_MATCHER = buildUriMatcher();

    private static final int MATCH_CODE_MOVIES = 100;
    private static final int MATCH_CODE_MOVIE_WITH_ID = 101;

    private static final String CONTENT_TYPE_DIR = "vnd.android.cursor.dir";
    private static final String CONTENT_TYPE_ITEM = "vnd.android.cursor.item";

    private static UriMatcher buildUriMatcher() {
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(MoviesContract.AUTHORITY, MoviesContract.PATH_MOVIE, MATCH_CODE_MOVIES);
        uriMatcher.addURI(MoviesContract.AUTHORITY, MoviesContract.PATH_MOVIE + "/#", MATCH_CODE_MOVIE_WITH_ID);
        return uriMatcher;
    }

    @Override
    public boolean onCreate() {
        dbHelper = new MoviesDBHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                        @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor;
        switch (URI_MATCHER.match(uri)) {
            case MATCH_CODE_MOVIES:
                cursor = db.query(MoviesContract.MovieEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case MATCH_CODE_MOVIE_WITH_ID:
                cursor = db.query(MoviesContract.MovieEntry.TABLE_NAME,
                        projection,
                        BaseColumns._ID + "=?",
                        new String[]{uri.getPathSegments().get(1)},
                        null,
                        null,
                        sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("Unsupported URI " + uri);
        }
        if (getContext() != null) {
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
        }
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        switch (URI_MATCHER.match(uri)) {
            case MATCH_CODE_MOVIES:
                return CONTENT_TYPE_DIR + "/" + MoviesContract.AUTHORITY + "/" + MoviesContract.PATH_MOVIE;
            case MATCH_CODE_MOVIE_WITH_ID:
                return CONTENT_TYPE_ITEM + "/" + MoviesContract.AUTHORITY + "/" + MoviesContract.PATH_MOVIE;
            default:
                throw new UnsupportedOperationException("Unsupported URI " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        if (values == null) {
            throw new UnsupportedOperationException("Content values cannot be null");
        }
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Uri returnUri;
        long id;
        switch (URI_MATCHER.match(uri)) {
            case MATCH_CODE_MOVIE_WITH_ID:
                values.put(BaseColumns._ID, uri.getPathSegments().get(1));
            case MATCH_CODE_MOVIES:
                id = db.insert(MoviesContract.MovieEntry.TABLE_NAME, null, values);
                break;
            default:
                throw new UnsupportedOperationException("Unsupported URI " + uri);
        }
        if (id > 0) {
            returnUri = ContentUris.withAppendedId(MoviesContract.MovieEntry.CONTENT_URI, id);
        } else {
            throw new SQLException("Failed insert a row for uri: " + uri);
        }
        if (getContext() != null) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return returnUri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int numRows;
        switch (URI_MATCHER.match(uri)) {
            case MATCH_CODE_MOVIE_WITH_ID:
                numRows = db.delete(MoviesContract.MovieEntry.TABLE_NAME,
                        BaseColumns._ID + "=?",
                        new String[]{uri.getPathSegments().get(1)});
                break;
            case MATCH_CODE_MOVIES:
                numRows = db.delete(MoviesContract.MovieEntry.TABLE_NAME,
                        selection,
                        selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unsupported URI " + uri);
        }
        if (numRows > 0 && getContext() != null) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return numRows;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection,
                      @Nullable String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int numRows;
        switch (URI_MATCHER.match(uri)) {
            case MATCH_CODE_MOVIES:
                numRows = db.update(MoviesContract.MovieEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unsupported URI " + uri);
        }
        if (numRows > 0 && getContext() != null) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return numRows;
    }
}
