package ui.pda.com.xhdreadsccardlibrary;

import java.io.Serializable;

/**
 * 项目名称：ReadSccardProject
 * 类描述：
 * 创建人：maw@neuqsoft.com
 * 创建时间： 2018/12/5 9:10
 * 修改备注
 */
public class IdCardBean implements Serializable{
    private String name;
    private String sex;
    private String DateBirth;
    private String IDCard;
    private String CardNumber;

    public String getName() {
        return name;
    }

    public String getSex() {
        return sex;
    }

    public String getDateBirth() {
        return DateBirth;
    }

    public String getIDCard() {
        return IDCard;
    }

    public String getCardNumber() {
        return CardNumber;
    }

    private IdCardBean(Builder builder) {
        name = builder.name;
        sex = builder.sex;
        DateBirth = builder.DateBirth;
        IDCard = builder.IDCard;
        CardNumber = builder.CardNumber;
    }


    public static final class Builder {
        private String name;
        private String sex;
        private String DateBirth;
        private String IDCard;
        private String CardNumber;

        public Builder() {
        }

        public Builder name(String val) {
            name = val;
            return this;
        }

        public Builder sex(String val) {
            sex = val;
            return this;
        }

        public Builder DateBirth(String val) {
            DateBirth = val;
            return this;
        }

        public Builder IDCard(String val) {
            IDCard = val;
            return this;
        }

        public Builder CardNumber(String val) {
            CardNumber = val;
            return this;
        }

        public IdCardBean build() {
            return new IdCardBean(this);
        }
    }
}
