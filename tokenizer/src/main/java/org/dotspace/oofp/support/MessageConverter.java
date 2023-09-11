package org.dotspace.oofp.support;

public interface MessageConverter {

    byte[] convertBig5ToUTF8(String str);

    byte[] convertUTF8ToBig5(String str);

    byte[] convertUTF8ToMS950(String str);

}
