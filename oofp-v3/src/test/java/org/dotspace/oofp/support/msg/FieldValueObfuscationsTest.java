package org.dotspace.oofp.support.msg;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FieldValueObfuscationsTest {

    @Mock
    private MessageSupport messageSupport;

    private FieldValueObfuscations fieldValueObfuscations;

    @BeforeEach
    void setUp() {
        fieldValueObfuscations = new FieldValueObfuscations(messageSupport);
    }

    @Test
    void maskFields_withNullInput_returnsNull() {
        String result = fieldValueObfuscations.maskFields(null, "*");
        assertNull(result);
    }

    @Test
    void maskFields_withBlankInput_returnsBlank() {
        String result = fieldValueObfuscations.maskFields("", "*");
        assertEquals("", result);
        
        result = fieldValueObfuscations.maskFields("   ", "*");
        assertEquals("   ", result);
    }

    @Test
    void maskFields_withSimpleKeyValue_masksValue() {
        when(messageSupport.getObfuscatedString("value1", "*")).thenReturn("***");
        
        String result = fieldValueObfuscations.maskFields("key1=value1", "*");
        
        assertEquals("key1=***", result);
        verify(messageSupport).getObfuscatedString("value1", "*");
    }

    @Test
    void maskFields_withMultipleKeyValues_masksAllValues() {
        when(messageSupport.getObfuscatedString("value1", "*")).thenReturn("***");
        when(messageSupport.getObfuscatedString("value2", "*")).thenReturn("###");
        
        String result = fieldValueObfuscations.maskFields("key1=value1, key2=value2", "*");
        
        assertEquals("key1=***, key2=###", result);
        verify(messageSupport).getObfuscatedString("value1", "*");
        verify(messageSupport).getObfuscatedString("value2", "*");
    }

    @ParameterizedTest
    @ValueSource(strings = {"list=[]", "data={}", "key=null"})
    void maskFields_withNullOrEmptyContent_keepsValue(String input) {
        String result = fieldValueObfuscations.maskFields(input, "*");

        assertEquals(input, result);
        verifyNoInteractions(messageSupport);

    }

    @Test
    void maskFields_withNestedObject_masksNestedFields() {
        when(messageSupport.getObfuscatedString("innerValue", "*")).thenReturn("***");
        
        String result = fieldValueObfuscations.maskFields("key1=Object{innerKey=innerValue}", "*");
        
        assertEquals("key1=Object{innerKey=***}", result);
        verify(messageSupport).getObfuscatedString("innerValue", "*");
    }

    @Test
    void maskFields_withComplexNestedObject_masksAllNestedFields() {
        when(messageSupport.getObfuscatedString("value1", "*")).thenReturn("***");
        when(messageSupport.getObfuscatedString("value2", "*")).thenReturn("###");
        
        String result = fieldValueObfuscations.maskFields("key1=MyClass{field1=value1, field2=value2}", "*");
        
        assertEquals("key1=MyClass{field1=***, field2=###}", result);
        verify(messageSupport).getObfuscatedString("value1", "*");
        verify(messageSupport).getObfuscatedString("value2", "*");
    }

    @Test
    void maskFields_withKeyValueStructure_masksValues() {
        when(messageSupport.getObfuscatedString("val1", "*")).thenReturn("v**1");
        when(messageSupport.getObfuscatedString("val2", "*")).thenReturn("v**2");
        
        String result = fieldValueObfuscations.maskFields("data={key1=val1,key2=val2}", "*");
        
        assertEquals("data={key1=v**1,key2=v**2}", result);
        verify(messageSupport).getObfuscatedString("val1", "*");
        verify(messageSupport).getObfuscatedString("val2", "*");
    }

    @Test
    void maskFields_withCollectionStructure_masksElements() {
        when(messageSupport.getObfuscatedString("item1", "*")).thenReturn("it**1");
        when(messageSupport.getObfuscatedString("item2", "*")).thenReturn("it**2");
        
        String result = fieldValueObfuscations.maskFields("list=[item1, item2]", "*");
        
        assertEquals("list=[it**1, it**2]", result);
        verify(messageSupport).getObfuscatedString("item1", "*");
        verify(messageSupport).getObfuscatedString("item2", "*");
    }

    @Test
    void maskFields_withNestedParentheses_handlesCorrectly() {
        when(messageSupport.getObfuscatedString("innerValue", "*")).thenReturn("inn*****ue");
        
        String result = fieldValueObfuscations.maskFields("outer=Class{inner=Object{field=innerValue}}", "*");
        
        assertEquals("outer=Class{inner=Object{field=inn*****ue}}", result);
        verify(messageSupport).getObfuscatedString("innerValue", "*");
    }

    @Test
    void maskFields_withComplexMixedStructure_masksCorrectly() {
        when(messageSupport.getObfuscatedString("val1", "*")).thenReturn("v**1");
        when(messageSupport.getObfuscatedString("val2", "*")).thenReturn("v**2");
        when(messageSupport.getObfuscatedString("item1", "*")).thenReturn("it**1");
        
        String result = fieldValueObfuscations.maskFields("data={key1=val1,key2=val2}, list=[item1]", "*");
        
        assertEquals("data={key1=v**1,key2=v**2}, list=[it**1]", result);
        verify(messageSupport).getObfuscatedString("val1", "*");
        verify(messageSupport).getObfuscatedString("val2", "*");
        verify(messageSupport).getObfuscatedString("item1", "*");
    }

    @Test
    void maskFields_withSpecialCharactersInValues_masksCorrectly() {
        when(messageSupport.getObfuscatedString("value@123", "*")).thenReturn("***");
        
        String result = fieldValueObfuscations.maskFields("key=value@123", "*");
        
        assertEquals("key=***", result);
        verify(messageSupport).getObfuscatedString("value@123", "*");
    }

    @Test
    void maskFields_withWhitespaceInValues_preservesStructure() {
        when(messageSupport.getObfuscatedString("value with spaces", "*")).thenReturn("***");
        
        String result = fieldValueObfuscations.maskFields("key=value with spaces", "*");
        
        assertEquals("key=***", result);
        verify(messageSupport).getObfuscatedString("value with spaces", "*");
    }

    @Test
    void maskFields_withInvalidNestedStructure_treatsAsRegularValue() {
        when(messageSupport.getObfuscatedString("NotClass(invalid", "*")).thenReturn("***");
        
        String result = fieldValueObfuscations.maskFields("key=NotClass(invalid", "*");
        
        assertEquals("key=***", result);
        verify(messageSupport).getObfuscatedString("NotClass(invalid", "*");
    }

    @Test
    void maskFields_withInvalidKeyValueStructure_treatsAsRegularValue() {
        when(messageSupport.getObfuscatedString("{invalid=structure", "*")).thenReturn("***");
        
        String result = fieldValueObfuscations.maskFields("key={invalid=structure", "*");
        
        assertEquals("key=***", result);
        verify(messageSupport).getObfuscatedString("{invalid=structure", "*");
    }

    @Test
    void maskFields_withInvalidCollectionStructure_treatsAsRegularValue() {
        when(messageSupport.getObfuscatedString("[invalid", "*")).thenReturn("***");
        
        String result = fieldValueObfuscations.maskFields("key=[invalid", "*");
        
        assertEquals("key=***", result);
        verify(messageSupport).getObfuscatedString("[invalid", "*");
    }

    @Test
    void maskFields_withParenthesesStructure_masksElements() {
        when(messageSupport.getObfuscatedString("val1", "*")).thenReturn("v**1");
        when(messageSupport.getObfuscatedString("val2", "*")).thenReturn("v**2");
        
        String result = fieldValueObfuscations.maskFields("data=(key1=val1, key2=val2)", "*");
        
        assertEquals("data=(key1=v**1, key2=v**2)", result);
    }

    @Test
    void maskFields_withValueOnlySegments_masksCorrectly() {
        when(messageSupport.getObfuscatedString("value1", "*")).thenReturn("***");
        when(messageSupport.getObfuscatedString("value2", "*")).thenReturn("###");
        
        String result = fieldValueObfuscations.maskFields("key1=value1, value2", "*");
        
        assertEquals("key1=***, ###", result);
    }

    @Test
    void maskFields_withQuotedValues_preservesQuotes() {
        when(messageSupport.getObfuscatedString("'quoted value'", "*")).thenReturn("***");
        
        String result = fieldValueObfuscations.maskFields("key='quoted value'", "*");
        
        assertEquals("key=***", result);
    }

    @Test
    void maskFields_withUnmatchedBraces_treatsAsValue() {
        when(messageSupport.getObfuscatedString("unmatched{", "*")).thenReturn("***");
        
        String result = fieldValueObfuscations.maskFields("key=unmatched{", "*");
        
        assertEquals("key=***", result);
    }

    @Test
    void maskFields_withWhitespacePreservation_maintainsFormat() {
        when(messageSupport.getObfuscatedString("value", "*")).thenReturn("v***e");
        
        String result = fieldValueObfuscations.maskFields("key=  value  ", "*");
        
        assertEquals("key=  v***e", result);
    }

    @Test
    void maskFields_withTrailingComma_handlesCorrectly() {
        when(messageSupport.getObfuscatedString("val1", "*")).thenReturn("v**1");
        when(messageSupport.getObfuscatedString("val2", "*")).thenReturn("v**2");
        
        String result = fieldValueObfuscations.maskFields("{key1=val1, key2=val2,}", "*");
        
        assertEquals("{key1=v**1, key2=v**2,}", result);
    }
}