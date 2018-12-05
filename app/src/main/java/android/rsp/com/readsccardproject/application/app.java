package android.rsp.com.readsccardproject.application;

import android.app.Application;

/**
 * 项目名称：ReadSccardProject
 * 类描述：
 * 创建人：maw@neuqsoft.com
 * 创建时间： 2018/12/5 10:22
 * 修改备注
 */
public class app extends Application{
    @Override
    public void onCreate() {
        super.onCreate();
        initView();
    }

    private void initView() {
        //注册
      /*  XhdReadCardCore.getInstance(this).Register();*/
    }
}
