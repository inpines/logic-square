package org.dotspace.oofp.utils;

import lombok.Setter;

public class ReflectionsTestClass {
    @Setter
    private String privateField;
    public String publicField;

    public ReflectionsTestClass() {}

    public ReflectionsTestClass(String value) {
        this.privateField = value;
    }

    public String testMethod(String arg1, Integer arg2) {
        return arg1 + arg2;
    }

    public void voidMethod() {
        // void method ...
    }
}
