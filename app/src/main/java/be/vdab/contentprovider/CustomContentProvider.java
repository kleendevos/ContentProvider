package be.vdab.contentprovider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import java.util.HashMap;

/**
 * Created by vdabcursist on 05/10/2017.
 */

public class CustomContentProvider extends ContentProvider {

    static final String PROVIDER_NAME = "com.androidatc.provider";
    static final String URL = "content://" + PROVIDER_NAME + "/nicknames";
    static final Uri CONTENT_URI = Uri.parse(URL);

    static final String ID = "id";
    static final String NAME = "name";
    static final String NICK_NAME = "nickname";
    static final int NICKNAME = 1;
    static final int NICKNAME_ID = 2;

    private static HashMap<String, String> nicknameMap;
    static UriMatcher uriMatcher;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(PROVIDER_NAME, "nicknames", NICKNAME);
        uriMatcher.addURI(PROVIDER_NAME, "nicknames/#", NICKNAME_ID);

    }

    private SQLiteDatabase database;
    static final String DATABASE_NAME = "nicknamesDirectory";
    static final String TABLE_NAME = "nicknames";
    static final int DATABASE_VERSION = 1;
    static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME
                    + " (id INTEGER PRIMARY KEY AUTOINCREMENT, Kat" +
                    "name TEXT NOT NULL, "
                    + "nickname TEXT NOT NULL);";




    @Override
    public boolean onCreate() {
        Context context = getContext();
        DBHelper dbHelper = new DBHelper(context);
        database = dbHelper.getWritableDatabase();
        if(database == null)
            return false;
        else
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selctionArgs, String sortOrder) {
        SQLiteQueryBuilder querybuilder = new SQLiteQueryBuilder();
        querybuilder.setTables(TABLE_NAME);

        switch (uriMatcher.match(uri)){
            case NICKNAME:
                querybuilder.setProjectionMap(nicknameMap);
                break;
            case NICKNAME_ID:
                querybuilder.appendWhere(ID + " =" + uri.getLastPathSegment());
                break;
            default:
                throw new IllegalArgumentException("Unknown URI" + uri);
        }
        if (sortOrder == null || sortOrder == ""){
            sortOrder = NAME;
        }
        Cursor cursor = querybuilder.query(database, projection, selection, selctionArgs, null,null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType( Uri uri) {
        switch (uriMatcher.match(uri)){
            case NICKNAME:
                return "vnd.android.cursor.dir/vnd.example.nicknames";
            case NICKNAME_ID:
                return "vnd.android.cursor.item/vnd.example.nicknames";
            default:
                throw new IllegalArgumentException("unsupported Uri" + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        long row = database.insert(TABLE_NAME, "", contentValues);

        if (row>0){
            Uri newUri = ContentUris.withAppendedId(CONTENT_URI,row);
            getContext().getContentResolver().notifyChange(newUri, null);
            return newUri;
        } throw new SQLException("Fail to add a new record into" + uri);
    }

    @Override
    public int delete( Uri uri, String selection, String[] selectionArgs) {
        int count = 0;
        switch (uriMatcher.match(uri)){
            case NICKNAME:
                count = database.delete(TABLE_NAME,selection,selectionArgs);
                break;
            case NICKNAME_ID:
                count = database.delete(TABLE_NAME,ID + " =" + (!TextUtils.isEmpty(selection)? "AND(" + selection + ')':""),selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("unsupported URI" + uri);
        }
        getContext().getContentResolver().notifyChange(uri,null);
        return count;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {

        int count = 0;
        switch (uriMatcher.match(uri)){
            case NICKNAME:
                count = database.update(TABLE_NAME, contentValues,selection,selectionArgs);
                break;
            case NICKNAME_ID:
                count = database.update(TABLE_NAME,contentValues,ID + " =" + uri.getLastPathSegment() + (!TextUtils.isEmpty(selection) ? " AND(" + selection + ")" : ""),selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("unsupported URI" + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    private class DBHelper extends SQLiteOpenHelper {
        public DBHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
            sqLiteDatabase.execSQL(CREATE_TABLE);

        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
            Log.w(DBHelper.class.getName(),"upgrading Database from version" + oldVersion + "to" + newVersion + ".Old data will be destroyed");
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS" + TABLE_NAME);
            onCreate(sqLiteDatabase);

        }
    }
}
