package com.neuqsoft.r.xh_v2readsccardlibrary;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.nexgo.oaf.apiv3.APIProxy;
import com.nexgo.oaf.apiv3.DeviceEngine;
import com.nexgo.oaf.apiv3.DeviceInfo;
import com.nexgo.oaf.apiv3.card.cpu.APDUEntity;
import com.nexgo.oaf.apiv3.card.cpu.CPUCardHandler;
import com.nexgo.oaf.apiv3.device.reader.CardInfoEntity;
import com.nexgo.oaf.apiv3.device.reader.CardReader;
import com.nexgo.oaf.apiv3.device.reader.CardSlotTypeEnum;
import com.nexgo.oaf.apiv3.device.reader.OnCardInfoListener;
import com.nexgo.oaf.apiv4.SicardInterface;
import com.nexgo.oaf.apiv4.sicard;

import java.io.UnsupportedEncodingException;
import java.util.HashSet;

/**
 * 项目名称：ReadSccardProject
 * 类描述： 设计思想：线上电，上不了电，重复。 上电之后获取唯一的CPUCardHandler,通过线程开启或者终止读卡服务
 * 目前，只有社保卡读卡模式
 * 创建人：maw@neuqsoft.com
 * 创建时间： 2018/12/5 9:29
 * 修改备注
 */
public class XhdReadCardCore {

    private static XhdReadCardCore instance;
    private Context context;
    private static DeviceEngine deviceEngine;
    private Thread readThread;
    private CPUCardHandler cpucardhandler;
    public final static int RESULTCODE = 1;
    private IShow iShowlistenter;

    byte[] bysPin = new byte[40];
    byte[] bysTerm = new byte[40];
    byte[] bysTemp = new byte[20];
    byte[] name = new byte[40];
    byte[] IDCard = new byte[40];
    byte[] DateBirth = new byte[40];
    byte[] sex = new byte[40];
    byte[] CardNumber = new byte[40];
    byte[] temp = new byte[255];
    private int nRt = 0;
    private sicard sicardInterface;

    public XhdReadCardCore(Context context) {
        this.context = context;
    }


    /**
     * @method： Register
     * @describe: 初始化注册
     * @Create：maw@neuqsoft.com
     * @CreateTime： 2019/3/4 9:51
     */
    public void Register() {
        //获取
        deviceEngine = APIProxy.getDeviceEngine();

        deviceEngine.getCardReader();
    }

    public void setCpuup() {

        final CardReader cardReader = deviceEngine.getCardReader();
        HashSet<CardSlotTypeEnum> slotTypes = new HashSet<>();
        slotTypes.add(CardSlotTypeEnum.SWIPE);
        slotTypes.add(CardSlotTypeEnum.ICC1);
        slotTypes.add(CardSlotTypeEnum.RF);
        cardReader.searchCard(slotTypes, 60, new OnCardInfoListener() {
            @Override
            public void onCardInfo(int retCode, CardInfoEntity cardInfo) {
                final StringBuilder sb = new StringBuilder();
                sb.append("返回值" + retCode + "\n");
                if (cardInfo != null) {
                    sb.append("卡存在的卡槽类型" + cardInfo.getCardExistslot() + "\n");
                    sb.append("一磁道" + cardInfo.getTk1() + "\n");
                    sb.append("二磁道" + cardInfo.getTk2() + "\n");
                    sb.append("三磁道" + cardInfo.getTk3() + "\n");
                    sb.append("卡号" + cardInfo.getCardNo() + "\n");
                    sb.append("是否为IC卡" + cardInfo.isICC() + "\n");
                }
            }

            @Override
            public void onSwipeIncorrect() {

            }

            @Override
            public void onMultipleCards() {

            }
        });
    }

    private boolean flag = true;

    /**
     * @method： readstart
     * @describe: 开启线程
     * @Create：maw@neuqsoft.com
     * @CreateTime： 2019/3/4 9:52
     */
    public void readStart(final Activity activity) {
        flag = true;
        if (iShowlistenter != null) {
            iShowlistenter.show();
        }
        try {
            setCpuup();
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        sicardInterface = new sicard();
        //步骤1：初始化社保卡接口
        readThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (flag) {
                    nRt = sicardInterface.iReaderSicardInit();
                    if (nRt < 0) {
                        Log.i("message", "无法查询到卡");
                    } else {
                        try {
                            Thread.sleep(2000);
                            readSocialSecurityCard(activity);
                            Log.i("tag", "执行");
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                }

            }
        });
        readThread.start();
    }

    /**
     * @method： readstart
     * @describe: 终止线程
     * @Create：maw@neuqsoft.com
     * @CreateTime： 2019/3/4 9:52
     */
    public void endStart() {
        flag = false;
        if (readThread != null) {
            readThread = null;
        }
    }

    /**
     * @method：
     * @describe:
     * @Create：maw@neuqsoft.com
     * @CreateTime： 2019/3/4 9:52
     */

    private void readSocialSecurityCard(Activity activity) {
        byte[] name = new byte[40];
        byte[] IDCard = new byte[40];
        byte[] bysPin = new byte[40];
        byte[] sex = new byte[40];
        byte[] CardNumber = new byte[40];
        byte[] temp = new byte[255];
        byte[] bysTerm = new byte[40];
        byte[] bysTemp = new byte[20];
        String strBir, strTerm;
        //步骤3：开始获取单项信息，需要哪条就调用对应接口
        try {

            nRt = sicardInterface.iGetSBKKH(CardNumber);
            if (nRt <= 0) {
                return;
            }

            nRt = sicardInterface.iGetSHBZHM(IDCard);
            if (nRt <= 0) {
                return;
            }

            nRt = sicardInterface.iGetName(name);
            if (nRt <= 0) {
                return;
            }

            nRt = sicardInterface.iGetSex(sex);
            if (nRt <= 0) {
                return;
            } else {
                String sex1 = "男";
                String sex2 = "女";
                for (int i = 0; i < nRt; i++) {
                    bysTemp[i] = sex[i + 2];
                }
                if ('1' == (char) bysTemp[0])
                    System.arraycopy(sex1.getBytes(), 0, sex, 0, 3);
                else
                    System.arraycopy(sex2.getBytes(), 0, sex, 0, 3);
            }

            nRt = sicardInterface.iGetBir(temp);
            if (nRt <= 0) {
                return;
            } else {
                for (int i = 0; i < nRt; i++) {
                    bysTemp[i] = temp[i + 2];
                }
                strBir = sicardInterface.arrayByteToString(bysTemp, nRt);
            }

            final StringBuilder sb = new StringBuilder();
            sb.append("姓名：" + new String(name, "GBK") + "\n");
            sb.append("性别：" + new String(sex) + "\n");
            sb.append("出生日期：" + strBir + "\n");
            sb.append("身份证号：" + new String(IDCard) + "\n");
            sb.append("卡号：" + new String(CardNumber) + "\n");
            IdCardBean cardbean = new IdCardBean.Builder()
                    .name(new String(name, "GBK").trim()).sex(transStr(sex))
                    .DateBirth(transStr(DateBirth))
                    .IDCard(transStr(IDCard))
                    .CardNumber(transStr(CardNumber))
                    .build();
            Intent intent = new Intent();
            intent.putExtra("idcardbean", cardbean);
            Log.i("name", cardbean.getName());
            if (!cardbean.getName().trim().isEmpty()) {
                if (iShowlistenter != null) {
                    flag = false;
                    iShowlistenter.disShow();
                }
                activity.setResult(RESULTCODE, intent);
                //activity.finish();
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {

        }
    }

    /**
     * @method： transStr
     * @describe: 字节转换
     * @Create：maw@neuqsoft.com
     * @CreateTime： 2019/3/4 9:52
     */

    private String transStr(byte[] str) {
        return new String(str).trim();
    }

    /**
     * @method： etAPDUEntity
     * @describe: 上电
     * @Create：maw@neuqsoft.com
     * @CreateTime： 2019/3/4 9:52
     */
    private APDUEntity setAPDUEntity(CPUCardHandler cpuCardHandler, String strcmd, int le) {
        byte[] cmd = new byte[255];
        byte[] dataIn = new byte[100];
        byte[] dataOut = new byte[100];
        byte swa, swb;
        int dataOutLen = 0;
        swa = 0;
        swb = 0;
        int strlen = strcmd.length();
        StringToHex(strcmd, cmd);
        if (strlen / 2 > 5) ;
        System.arraycopy(cmd, 5, dataIn, 0, strlen / 2 - 5);
        APDUEntity a = new APDUEntity();
        a.setCla(cmd[0]);
        a.setIns(cmd[1]);
        a.setP1(cmd[2]);
        a.setP2(cmd[3]);
        a.setLc(cmd[4]);
        a.setDataIn(dataIn);
        a.setLe(le);
        a.setSwa(swa);
        a.setSwb(swb);
        a.setDataOutLen(dataOutLen);
        a.setDataOut(dataOut);
        int status = cpuCardHandler.exchangeAPDUCmd(a);
        //     showResponse(cmd,"指令:",strlen/2);
        return a;
    }

    private boolean StringToHex(String strIn, byte[] strOut) {
        int j = 0;
        byte strbuf[] = new byte[strIn.length() + 1];
        byte pstrTemp[] = new byte[strIn.length() + 1];

        strbuf = strIn.getBytes();

        for (int i = 0; i < strIn.length() - 1; i += 2, j++) {
            if (strbuf[i] >= '0' && strbuf[i] <= '9') {
                pstrTemp[i] = (byte) (strbuf[i] - '0');
            } else if (strbuf[i] >= 'A' && strbuf[i] <= 'F') {
                pstrTemp[i] = (byte) (strbuf[i] - 'A' + 10);
            } else if (strbuf[i] >= 'a' && strbuf[i] <= 'f') {
                pstrTemp[i] = (byte) (strbuf[i] - 'a' + 10);
            } else {
                //非十六进制字符
                return false;
            }

            if (strbuf[i + 1] >= '0' && strbuf[i + 1] <= '9') {
                pstrTemp[i + 1] = (byte) (strbuf[i + 1] - '0');
            } else if (strbuf[i + 1] >= 'A' && strbuf[i + 1] <= 'F') {
                pstrTemp[i + 1] = (byte) (strbuf[i + 1] - 'A' + 10);
            } else if (strbuf[i + 1] >= 'a' && strbuf[i + 1] <= 'f') {
                pstrTemp[i + 1] = (byte) (strbuf[i + 1] - 'a' + 10);
            } else {
                //非十六进制字符
                return false;
            }

            strOut[j] = (byte) ((pstrTemp[i] << 4) & 0xF0);
            strOut[j] += pstrTemp[i + 1] & 0x0F;
        }

        return true;
    }

    private boolean bytesToHexString(byte[] strIn, int lenIn, byte[] strOut) {

        if (strIn == null || lenIn <= 0) {
            return false;
        }
        for (int i = 0; i < lenIn; i++) {
            int v = strIn[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                strOut[i * 2] = '0';
                strOut[i * 2 + 1] = (byte) hv.charAt(0);
            } else {
                strOut[i * 2] = (byte) hv.charAt(0);
                strOut[i * 2 + 1] = (byte) hv.charAt(1);
            }
        }

        return true;
    }

    public static XhdReadCardCore getInstance(Context context) {
        if (instance == null) {
            instance = new XhdReadCardCore(context);
        }
        return instance;
    }

    /**
     * @method： setShowListener
     * @describe: 提供监听
     * @Create：maw@neuqsoft.com
     * @CreateTime： 2019/3/4 9:53
     */
    public void setShowListener(IShow iShowlistenter) {
        this.iShowlistenter = iShowlistenter;
    }

    public String getDevice() {
        DeviceInfo deviceInfo = deviceEngine.getDeviceInfo();
        return deviceInfo.getSn();
    }

    public String getStrTerm() {

        String strTerm = "";
        try {
            SicardInterface sicardInterface = new sicard();

            nRt = sicardInterface.iReaderSicardInit();
            if(nRt<0){
                return strTerm;
            }
            //步骤4：获取PSAM卡的终端机编号，此处无卡：暂未测试
            nRt = sicardInterface.iGetTermNo(bysTerm);
            if (nRt <= 0) {
                Toast.makeText(context, "获取终端机编号失败！" + nRt + "-" + sicardInterface.arrayByteToString(bysTerm, nRt), Toast.LENGTH_SHORT).show();
                strTerm = "获取终端机编号失败";
                return strTerm;
            }
            strTerm = sicardInterface.arrayByteToString(bysTerm, nRt);

        } catch (Exception e) {
            Toast.makeText(context, "Exception: " + e, Toast.LENGTH_SHORT).show();
        }
        return strTerm;

    }

    public boolean CheckMm(String strPin) {

        boolean bcheck = false;
        try {
            SicardInterface sicardInterface = new sicard();
            int nRt = 0;
            //步骤1：初始化社保卡接口
            nRt = sicardInterface.iReaderSicardInit();
            if (nRt != 0) {
                Toast.makeText(context, "初始化失败！" + nRt, Toast.LENGTH_SHORT).show();
                return bcheck;
            }
            //步骤2：校验社保卡PIN, 由于读基本信息不需要PIN的权限，所以PIN出错了也可以继续读卡，但是需要提示密码错误
            StringToHex(strPin, bysPin);
            Toast.makeText(context, "PIN长度：" + strPin.length() / 2, Toast.LENGTH_SHORT).show();
            nRt = sicardInterface.iVerifyPIN(bysPin, (byte) (strPin.length() / 2));
            if (nRt != 0) {
                Toast.makeText(context, "PIN校验失败！剩余错误次数：" + nRt, Toast.LENGTH_SHORT).show();
                return bcheck;
            }
            bcheck = true;
        } catch (Exception e) {
            Toast.makeText(context, "Exception: " + e, Toast.LENGTH_SHORT).show();
        }
        return bcheck;
    }
}
