package sunday.sdk.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TimeUtil {



    /**
     * 时间戳转换为字符串
     * */
    public static String Stamp2String(long mills){
        Date date = new Date(mills);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss",Locale.CHINA);
        return simpleDateFormat.format(date);
    }


}
