package ui.pda.com.xhdreadsccardlibrary;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.nexgo.oaf.apiv3.APIProxy;
import com.nexgo.oaf.apiv3.DeviceEngine;
import com.nexgo.oaf.apiv3.card.cpu.APDUEntity;
import com.nexgo.oaf.apiv3.card.cpu.CPUCardHandler;
import com.nexgo.oaf.apiv3.device.reader.CardSlotTypeEnum;

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


    public XhdReadCardCore(Context context) {
        this.context = context;
    }


    //全局注册
    public void Register() {
        //获取
        deviceEngine = APIProxy.getDeviceEngine();

        deviceEngine.getCardReader();
    }

    public CPUCardHandler setCpuup() {
        HashSet<CardSlotTypeEnum> slotTypes = new HashSet<>();
        slotTypes.add(CardSlotTypeEnum.SWIPE);
        slotTypes.add(CardSlotTypeEnum.ICC1);
        slotTypes.add(CardSlotTypeEnum.RF);

        byte[] ATR = new byte[20];
        //获取CPU卡类对象
        cpucardhandler = deviceEngine.getCPUCardHandler(CardSlotTypeEnum.ICC1);
        //CPU卡上电
        boolean status = cpucardhandler.powerOn(ATR);
        if (false == status) {
            return null;
        }
        return cpucardhandler;
    }

    private boolean flag = true;

    //开启线程
    public void readStart(final Activity activity) {
        flag = true;
        if (iShowlistenter != null) {
            iShowlistenter.show();
        }
        readThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (flag) {
                    setCpuup();
                    if (cpucardhandler != null) {
                        readSocialSecurityCard(activity);
                        Log.i("tag", "执行");
                    }
                }
            }
        });
        readThread.start();
    }


    //终止线程
    public void endStart() {
        flag = false;
        if (readThread != null) {
            readThread = null;
        }
    }


    private void readSocialSecurityCard(Activity activity) {

        byte[] name = new byte[40];
        byte[] IDCard = new byte[40];
        byte[] DateBirth = new byte[40];
        byte[] sex = new byte[40];
        byte[] CardNumber = new byte[40];
        byte[] temp = new byte[255];
        APDUEntity apduEntity;
        try {
            String strbuf = "";

            strbuf = "00A404000F7378312E73682EC9E7BBE1B1A3D5CF";
            apduEntity = setAPDUEntity(cpucardhandler, strbuf, 20);
            //卡号
            strbuf = "00B2072C00";
            apduEntity = setAPDUEntity(cpucardhandler, strbuf, 11);
            System.arraycopy(apduEntity.getDataOut(), 2, CardNumber, 0, 9);
            //社会保障号码
            strbuf = "00B2013400";
            apduEntity = setAPDUEntity(cpucardhandler, strbuf, 22);
            System.arraycopy(apduEntity.getDataOut(), 2, IDCard, 0, 18);
            //姓名
            strbuf = "00B2023400";
            apduEntity = setAPDUEntity(cpucardhandler, strbuf, 34);
            System.arraycopy(apduEntity.getDataOut(), 2, name, 0, 30);
            //性别
            strbuf = "00B2033400";
            apduEntity = setAPDUEntity(cpucardhandler, strbuf, 5);
            System.arraycopy(apduEntity.getDataOut(), 2, sex, 0, 1);
            String sex1 = "男";
            String sex2 = "女";
            if ('1' == (char) sex[0])
                System.arraycopy(sex1.getBytes(), 0, sex, 0, 3);
            else
                System.arraycopy(sex2.getBytes(), 0, sex, 0, 3);
            //出生日期
            strbuf = "00B2063400";
            apduEntity = setAPDUEntity(cpucardhandler, strbuf, 8);
            System.arraycopy(apduEntity.getDataOut(), 2, temp, 0, 4);
            bytesToHexString(temp, 4, DateBirth);
            final StringBuilder sb = new StringBuilder();
            sb.append("姓名：" + new String(name, "GBK") + "\n");
            sb.append("性别：" + new String(sex) + "\n");
            sb.append("出生日期：" + new String(DateBirth) + "\n");
            sb.append("身份证号：" + new String(IDCard) + "\n");
            sb.append("卡号：" + new String(CardNumber) + "\n");
            Log.i("tag", new String(sex));
            IdCardBean cardbean = new IdCardBean.Builder()
                    .name(new String(name, "GBK").trim())
                    .sex(transStr(sex))
                    .DateBirth(transStr(DateBirth))
                    .IDCard(transStr(IDCard))
                    .CardNumber(transStr(CardNumber))
                    .build();
            Intent intent = new Intent();
            intent.putExtra("idcardbean", cardbean);

            if (!cardbean.getName().trim().isEmpty()) {
                if (iShowlistenter != null) {
                    flag = false;
                    iShowlistenter.disShow();
                }
                activity.setResult(RESULTCODE, intent);
                activity.finish();
            }

            //如果线程结束

        } catch (Exception e) {
            Toast.makeText(activity, "Exception: " + e, Toast.LENGTH_SHORT).show();
        }
    }

    //除了身份证，其他的全部转String
    private String transStr(byte[] str) {
        return new String(str).trim();
    }

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

    public void setShowListener(IShow iShowlistenter) {
        this.iShowlistenter = iShowlistenter;
    }
}
