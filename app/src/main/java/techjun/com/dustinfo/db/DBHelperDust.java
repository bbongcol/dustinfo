package techjun.com.dustinfo.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by leebongjun on 2017. 6. 13..
 */

public class DBHelperDust extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "DustDB.db";
    private static final int DATABASE_VERSION = 1;
    Context context;

    // DBHelper 생성자로 관리할 DB 이름과 버전 정보를 받음
    public DBHelperDust(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        // TODO Auto-generated constructor stub
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE dust_data (_id Integer PRIMARY KEY AUTOINCREMENT, datatime TEXT, city TEXT, sido TEXT, dong TEXT, covalue REAL, no2value REAL, o3value REAL, pm10value REAL, pm25value REAL, so2value REAL);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void insertDustList(String create_at, String item, int price) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("INSERT INTO dust_data VALUES(null, '" + item + "', " + price + ", '" + create_at + "');");
        db.close();
    }

    public void update(String item, int price) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("UPDATE dust_data SET price=" + price + " WHERE item='" + item + "';");
        db.close();
    }

    public void delete(String item) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM dust_data WHERE item='" + item + "';");
        db.close();
    }

    public String getResult() {
        // 읽기가 가능하게 DB 열기
        SQLiteDatabase db = getReadableDatabase();
        String result = "";

        // DB에 있는 데이터를 쉽게 처리하기 위해 Cursor를 사용하여 테이블에 있는 모든 데이터 출력
        Cursor cursor = db.rawQuery("SELECT * FROM MONEYBOOK", null);
        while (cursor.moveToNext()) {
            result += cursor.getString(0)
                    + " : "
                    + cursor.getString(1)
                    + " | "
                    + cursor.getInt(2)
                    + "원 "
                    + cursor.getString(3)
                    + "\n";
        }

        return result;
    }
}

