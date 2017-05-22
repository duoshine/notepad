package cn.chenand.notepad;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import cn.chenand.notepad.adapter.FinalAdapter;
import cn.chenand.notepad.db.MySqliteOpenHelper;
import cn.chenand.notepad.util.Logger;
import cn.chenand.notepad.util.ToastUtils;
import cn.chenand.notepad.widget.SelectPicPopupWindow;

/**
 * 剩下的问题  1.item的名字还是可以重复  带来的问题是去数据库中拿到的数据也是相同的
 * 解决思路：修改名称的时候，去数据库中查询是否已经存在 不存在则修改 存在则不允许修改
 * 2.已经保存过数据的item 如果修改了名称 进去就拿不到数据 因为数据是根据之前的名称存的
 * 解决思路：修改名称的时候 也用旧名称去数据库中将数据拿出来，再用新名称存进去
 * 3.没有记录修改日志的时间
 * 4.没有插入图片功能 没有分享功能
 * 5.ui很丑
 */
public class MainActivity extends AppCompatActivity implements FinalAdapter.OnRecycleViewListener<String>, View.OnClickListener {
    private static final String TAG = "MainActivity1";
    private RecyclerView mRecyclerView;
    private Toolbar toolbar_behavior;
    private SelectPicPopupWindow menuWindow;
    private TextView mYes;
    private TextView mNo;
    private EditText mEtName;
    private TextView mTvName;
    private FinalAdapter<String> mFinalAdapter;
    private List<String> mDatas = new ArrayList<>();
    private static int mPosition;
    private MySqliteOpenHelper mMySqliteOpenHelper;
    private boolean isNew = false;//是新添加的还是更新旧的
    private Snackbar mSnackbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        //1.上下文  2.数据库名 3.null即可 4.数据库版本 可以用来升级数据库
        mMySqliteOpenHelper = new MySqliteOpenHelper(MainActivity.this, "notepad.db", null, 1);
        getDbDate();
    }

    private void initView() {
        toolbar_behavior = (Toolbar) findViewById(R.id.toolbar_behavior);
        //设置ActionBar的主题 注意要在setSupportActionBar之前
        toolbar_behavior.setTitle("notepad");
        //前提是主题是NoActionBar
        setSupportActionBar(toolbar_behavior);
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
        mFinalAdapter = new FinalAdapter<>(MainActivity.this, mDatas,
                R.layout.main_item, this);
        mRecyclerView.setAdapter(mFinalAdapter);
    }

    //处理adapter的数据绑定和初始化
    @Override
    public void bindView(FinalAdapter.FinalViewHolder finalAdapter, String s, final int position) {
        mTvName = (TextView) finalAdapter.autoView(R.id.tvName);
        ImageView tvInsertName = (ImageView) finalAdapter.autoView(R.id.tvInsertName);
        tvInsertName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initPwOnclick(position);
                MainActivity.this.mPosition = position;
            }
        });
        mTvName.setText(s);
        if (position == mDatas.size() - 1 && mDatas.get(position).equals("+")) {
            tvInsertName.setVisibility(View.GONE);
        } else {
            tvInsertName.setVisibility(View.VISIBLE);
        }
    }

    //初始化pw
    private void initPwOnclick(int position) {
        int width = getWindowManager().getDefaultDisplay().getWidth();
        int newWidth = width / 2 + width / 3;
        //实例化SelectPicPopupWindow
        menuWindow = new SelectPicPopupWindow(MainActivity.this,
                newWidth, R.layout.main_pw);
        //显示窗口
        menuWindow.showAtLocation(MainActivity.this.findViewById(R.id.toolbar_behavior),
                Gravity.CENTER | Gravity.CENTER_HORIZONTAL, 0, 0); //设置layout在PopupWindow中显示的位置
        mEtName = (EditText) menuWindow.autoView(R.id.etName);
        mYes = (TextView) menuWindow.autoView(R.id.yes);
        mNo = (TextView) menuWindow.autoView(R.id.no);
        mYes.setOnClickListener(MainActivity.this);
        mNo.setOnClickListener(MainActivity.this);
        mEtName.setHint(mDatas.get(position));
    }

    //item点击事件
    @Override
    public void itemOnclick(int position) {
        String itemName = mDatas.get(position);
        if (position == mDatas.size() - 1) {
            mDatas.add(mDatas.size() - 1, "新添加");
            mRecyclerView.scrollToPosition(mDatas.size() - 1);
            mFinalAdapter.notifyDataSetChanged();
        } else {
            if ("新添加".equals(itemName)) {
                ToastUtils.showToast(MainActivity.this, "修改名称才可以记事哦");
            } else {
                Intent intent = new Intent(MainActivity.this, ContentActivity.class);
                intent.putExtra("type", itemName);
                startActivity(intent);
            }
        }
    }

    //item长按删除事件
    @Override
    public void itemOnLongClick(final int position) {
        mSnackbar = Snackbar.make(mRecyclerView, "是否确定删除", Snackbar.LENGTH_LONG);
        mSnackbar.setAction("delete", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String deleteName = mDatas.get(position);
                Logger.d("判断是否能删除的item:" + deleteName);
                //+这个条目不能被删除
                if ("+".equals(deleteName)) {
                    Snackbar.make(mRecyclerView, "delete defeated", Snackbar.LENGTH_LONG).show();
                    return;
                }
                String remove = mDatas.remove(position);
                Logger.d("删除的item：" + remove);
                mFinalAdapter.notifyDataSetChanged();
                //数据库删除
                deleteDb(remove);
                Snackbar.make(mRecyclerView, "delete succeeded!", Snackbar.LENGTH_LONG).show();
            }
        });
        mSnackbar.show();
    }

    //删除数据库中用户选定的item
    private void deleteDb(String remove) {
        SQLiteDatabase writableDatabase = mMySqliteOpenHelper.getWritableDatabase();
        //1、表名  2.条件 3. 2中的问号----删除home表中 所有name = remove的内容
        writableDatabase.delete("Home", "name = ?", new String[]{remove});
        //同样删除内容表中 数据
        writableDatabase.delete("Content", "type = ?", new String[]{remove});
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.yes:
                String oldName = mDatas.get(mPosition);
                if ("新添加".equals(oldName)) {
                    Logger.d("旧的名称：" + oldName);
                    isNew = true;
                }
                String newName = mEtName.getText().toString().trim();
                Logger.d("新的名称：" + newName);
                if (TextUtils.isEmpty(newName)) {
                    Toast.makeText(this, "不能为空", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (newName.length() > 6) {
                    Toast.makeText(this, "长度不能大于六个字符", Toast.LENGTH_SHORT).show();
                    return;
                }

                menuWindow.dismiss();
                //保存到数据库
                saveDb(newName, oldName);
                break;
            case R.id.no:
                menuWindow.dismiss();
                break;
            default:
                break;
        }
    }

    //数据库添加
    private void saveDb(String newname, String oldName) {
        SQLiteDatabase writableDatabase = mMySqliteOpenHelper.getWritableDatabase();
        //添加之前为了避免名称重复  先去数据库查询
        Cursor cursor = writableDatabase.query("Home", null, "name = ?", new String[]{newname}, null, null, null, null);
        String name = null;
        if (cursor.moveToFirst()) {
            do {
                name = cursor.getString(cursor.getColumnIndex("name"));
                Logger.d("name:"+name);
            } while (cursor.moveToNext());
        }
        cursor.close();
        if (newname.equals(name)) {
            ToastUtils.showToast(MainActivity.this,"名称重复啦 换一个呗");
            return;
        }
        //如果确定不重复就 替换名称
        mDatas.remove(mPosition);
        mDatas.add(mPosition, newname);
        mFinalAdapter.notifyDataSetChanged();
        //如果是新添加就是插入 如果不是新添加就是更新
        ContentValues values = new ContentValues();
        values.put("name", newname);
        if (isNew) {//是新添加的就插入
            writableDatabase.insert("Home", null, values);
        } else {//不是新添加的就是更新
            //将旧名称更换掉
            writableDatabase.update("Home", values, "name = ?", new String[]{oldName});
            //并将对应的笔记key也替换掉 这样数据库就可以继续根据item的名称取出对应的数据
            values.clear();
            values.put("type",newname);
            writableDatabase.update("Content", values,"type = ?" ,new String[]{oldName});
        }
        isNew = false;
    }

    //进入首页时  从数据库拿到数据填充
    private void getDbDate() {
        SQLiteDatabase writableDatabase = mMySqliteOpenHelper.getWritableDatabase();
        Cursor home = writableDatabase.query("Home", null, null, null, null, null, null);
        String name = "";
        if (home.moveToFirst()) {
            do {
                name = home.getString(home.getColumnIndex("name"));
                mDatas.add(name);
            } while (home.moveToNext());
        }
        home.close();
        mDatas.add("+");
        mFinalAdapter.notifyDataSetChanged();
    }
}
