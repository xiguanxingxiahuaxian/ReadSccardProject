# ReadSccardProject

 #Xh_V2ReadSccardLibrary  

# 初始化

## XhdReadCardCore.getInstance(this).Register();

# 上电

##  XhdReadCardCore.getInstance(this).setCpuup();

#  ShowDialog

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

#  回调

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
       
     
   
 # 获取设备终端号
       XhdReadCardCore.getInstance(this).getStrTerm();
       return "编号"
       
 # 验证密码
       XhdReadCardCore.getInstance(this).CheckMm(Stirng pw)
       return ==true?"true":"其他的说明"
       
       
 # 具体例子
          String term=XhdReadCardCore.getInstance(getApplicationContext()).getStrTerm();
                       Log.i("term",term);
          boolean term=XhdReadCardCore.getInstance(getApplicationContext()).CheckMm("123454");
                       Log.i("term",term+"");
                       
 #  集成方式 
       
       allprojects {
       		repositories {
       			...
       			maven { url 'https://jitpack.io' }
       		}
       	}
       	
       	dependencies {
        	        implementation 'com.github.xiguanxingxiahuaxian:ReadSccardProject:Tag'
        }
       