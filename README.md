# ReadSccardProject

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
       