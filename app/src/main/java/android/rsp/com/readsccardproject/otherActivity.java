package android.rsp.com.readsccardproject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.neuqsoft.r.xh_v2readsccardlibrary.IShow;
import com.neuqsoft.r.xh_v2readsccardlibrary.IdCardBean;
import com.neuqsoft.r.xh_v2readsccardlibrary.XhdReadCardCore;

/**
 * 项目名称：ReadSccardProject
 * 类描述：
 * 创建人：maw@neuqsoft.com
 * 创建时间： 2018/12/5 10:28
 * 修改备注
 */
public class otherActivity extends Activity {

    private TextView read;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        read = (TextView) findViewById(R.id.read);
        read.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String term=XhdReadCardCore.getInstance(getApplicationContext()).getStrTerm();
                Log.i("term",term);
                /*boolean term=XhdReadCardCore.getInstance(getApplicationContext()).CheckMm("123456");
                Log.i("term",term+"");*/
            }
        });
       XhdReadCardCore.getInstance(this).setShowListener(new IShow() {
            @Override
            public void show() {
                Log.i("执行", "show().....");
            }

            @Override
            public void disShow() {
                Log.i("执行", "disshow().....");
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 1) {
            if (data != null) {
                IdCardBean idcardbean = (IdCardBean) data.getSerializableExtra("idcardbean");
                Toast.makeText(this, idcardbean.getName(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        XhdReadCardCore.getInstance(this).readStart(otherActivity.this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        XhdReadCardCore.getInstance(this).endStart();
    }
}
