package cn.chenand.notepad.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by chen on 2017
 */

public class MySqliteOpenHelper extends SQLiteOpenHelper {
    private Context mContext;
    //建表语句  create table info(_id integer primary key,username varchar(20),contact varchar(20))");
    public static final String CREATE_HOME= "create table Home ("//首页的表
            +"_id integer primary key autoincrement, "//id设为主键  并自动增长
            +"name varchar(20)) ";//文本类型
     public static final String CREATE_CONTENT= "create table Content ("//存储记事的表
            +"_id integer primary key autoincrement, "//id设为主键  并自动增长
             +"type varchar(20),"
             +"time varchar(20),"
             +"content varchar(8000)) ";//文本类型

    public MySqliteOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_HOME);
        db.execSQL(CREATE_CONTENT);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
