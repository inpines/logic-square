package org.dotspace.oofp.support.test.tokenizer.msgconverter;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.dotspace.oofp.support.MessageConverter;

public class MessageConverterImpl implements MessageConverter {

    public byte[] convertBig5ToUTF8(String str) {
        String strUtf8 = new String(str.getBytes(Charset.forName("Big5")), StandardCharsets.UTF_8);
        return strUtf8.getBytes();
    }

    public byte[] convertUTF8ToBig5(String str) {
        String strBig5 = new String(str.getBytes(StandardCharsets.UTF_8), Charset.forName("Big5"));
        return strBig5.getBytes();
    }

    public byte[] convertUTF8ToMS950(String str) {
        String strMS950 = new String(str.getBytes(StandardCharsets.UTF_8), Charset.forName("MS950"));
        return strMS950.getBytes();
    }

}

