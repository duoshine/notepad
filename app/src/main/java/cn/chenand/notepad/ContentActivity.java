package cn.chenand.notepad;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.EditText;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

import cn.chenand.notepad.db.MySqliteOpenHelper;
import cn.chenand.notepad.util.Logger;

/**
 * 问题1.第一次和每次都是添加数据 需要区分第一次和其他  第一次需要插入 之后更新即可
 * 思路：OnResume时查询如果内容Null 认为是第一次, 不为null为更新
 * 2.每次不管数据有没有修改都保存数据库了 如果用户改变了数据 则保存到数据库 如果用户没有改变则不做保存
 * 思路:首先用户如果修改了笔记，那么长度是最容易发生变化的，但是可能用户只是修改了错别字 符号 等等，优先判断长度，如果长度
 * 不变的情况下在判断每个字符是否改变
 */
public class ContentActivity extends AppCompatActivity {

    private MySqliteOpenHelper mMySqliteOpenHelper;
    private String mType; //首页进来的条目名 根据条目名去数据库拿数据 条目名在数据库唯一
    private EditText mEditText;
    private TextView tvTime;
    private boolean isOne;//是否是第一次编辑笔记
    private String mContent;
    private boolean isSaveDb;//是否有改动 是否需要保存到数据库

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content);
        initView();
        mType = getIntent().getStringExtra("type");
        mMySqliteOpenHelper = new MySqliteOpenHelper(ContentActivity.this, "notepad.db", null, 1);
    }

    private void initView() {
        mEditText = (EditText) findViewById(R.id.etUserInput);
        tvTime = (TextView) findViewById(R.id.tvTime);
    }

    @Override
    protected void onResume() {
        super.onResume();
        SQLiteDatabase writableDatabase = mMySqliteOpenHelper.getWritableDatabase();
        Cursor cursor = writableDatabase.query("Content", null, "type = ?", new String[]{mType}, null, null, null);
        mContent = null;
        String lastTime = null;
        if (cursor.moveToFirst()) {
            do {
                mContent = cursor.getString(cursor.getColumnIndex("content"));
                lastTime = cursor.getString(cursor.getColumnIndex("time"));
                mEditText.setText(mContent);
                tvTime.setText("上一次修改时间:" + lastTime);
                mEditText.setSelection(mContent.length());
            } while (cursor.moveToNext());
        }
        cursor.close();
        /**
         * 每次都插入数据 数据库臃肿  第一次是插入 之后就是更新
         * 解决思路:查询时 如果为null 说明是第一次进入记录笔记， 所以要插入，不为null 说明已经记录过笔记
         * 那么就直接更新掉即可
         */
        if (null == mContent) {
            Logger.d("content" + mContent);
            isOne = true;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        writeDb();
    }

    private void writeDb() {
        String userContent = mEditText.getText().toString();
        //保存最后修改时间
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss");
        String time = simpleDateFormat.format(new Date(System.currentTimeMillis()));
        Logger.d("最后修改时间:" + time);
        SQLiteDatabase writableDatabase = mMySqliteOpenHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("type", mType);
        values.put("content", userContent);
        values.put("time", time);
        /**
         * 每次都插入数据 数据库臃肿  第一次是插入 之后就是更新
         * 解决思路:先查询content
         */
        if (!isOne) {//不是第一次编辑笔记了 需要更新
            Logger.d("不是第一次编辑笔记了");
            //判断数据是否发生改变 不改变则不保存
            if ( mContent.equals(userContent)) {
                isSaveDb = true;
            }
            if (isSaveDb) {
                Logger.d("和之前的数据有不同 需要更新");
                writableDatabase.update("Content", values, "type = ?", new String[]{mType});
            } else {
                Logger.d("和之前的数据相同不需要更新");
            }
        } else {//是第一次编辑编辑 需要插入
            Logger.d("是第一次编辑编辑");
            writableDatabase.insert("Content", null, values);
        }
    }
}
