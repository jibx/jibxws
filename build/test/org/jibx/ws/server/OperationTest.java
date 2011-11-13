package org.jibx.ws.server;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.io.PrintStream;
import java.util.Date;

import org.jibx.ws.WsConfigurationException;
import org.jibx.ws.context.InContext;
import org.jibx.ws.context.OutContext;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link Operation}.
 */
public class OperationTest
{
    private static String testString;
    
    public static void staticInputOnlyMethod(Date s) {
        testString = "staticInputOnlyMethod";
    }
    
    public void inputOnlyMethodWithInContext(Date s, InContext inCtx) {
        testString = "inputOnlyMethodWithInContext";
    }
    
    public static String inOutMethodWithInAndOutContext(Float f, InContext inCtx, OutContext outCtx) {
        testString = "inOutMethodWithInAndOutContext";
        return null;
    }
    
    public String methodWith3Params(Float f, Integer i, Double d) {
        testString = "methodWith3Params";
        return null;
    }
    
    @Before public void init() {
        testString = null;
    }
  
    /** Test static void method with one parameter. */
    @Test public void testStaticInputOnlyMethod() throws Exception {
        OperationDefinition opDef = new OperationDefinition();
        String methodName = "staticInputOnlyMethod";
        opDef.setMethodName(methodName);
        Operation op = Operation.newInstance(this.getClass(), opDef);
//        assertThat(op.getOperationName(), equalTo(methodName));
        assertThat(op.getInputClass().getName(), equalTo(Date.class.getName()));
//        assertThat(op.getInputName(), equalTo("Date"));
        assertThat(op.getOutputClass(), nullValue());
//        assertThat(op.getOutputName(), nullValue());

        op.invoke(null, new Date(), null);
        assertThat(testString, is(methodName));
    }

    /** Test static method with no parameters. */
    @Test public void testStaticOutputOnlyMethod() throws Exception {
        OperationDefinition opDef = new OperationDefinition();
        opDef.setMethodName("getClass");
        Operation op = Operation.newInstance(this.getClass(), opDef);
//        assertThat(op.getOperationName(), equalTo("getClass"));
        assertThat(op.getInputClass(), nullValue());
//        assertThat(op.getInputName(), nullValue());
        assertThat(op.getOutputClass().getName(), equalTo(Class.class.getName()));
//        assertThat(op.getOutputName(), equalTo("Class"));
    }

    /** Test static method with one parameter. Ensure it is selected over static method with multiple parameters. */
    @Test public void testStaticInOutMethod() throws Exception {
        OperationDefinition opDef = parseIntOperation();
        Operation op = Operation.newInstance(Integer.class, opDef);
//        assertThat(op.getOperationName(), equalTo("parseInt"));
        assertThat(op.getInputClass().getName(), equalTo(String.class.getName()));
//        assertThat(op.getInputName(), equalTo("String"));
        assertThat(op.getOutputClass().getName(), equalTo(int.class.getName()));
//        assertThat(op.getOutputName(), equalTo("int"));
    }

    /** Test void method with one parameter. */
    @Test public void testInstanceInputOnlyMethod() throws Exception {
        OperationDefinition opDef = new OperationDefinition();
        opDef.setMethodName("ensureCapacity");
        Operation op = Operation.newInstance(StringBuilder.class, opDef);
//        assertThat(op.getOperationName(), equalTo("ensureCapacity"));
        assertThat(op.getInputClass().getName(), equalTo(int.class.getName()));
//        assertThat(op.getInputName(), equalTo("int"));
        assertThat(op.getOutputClass(), nullValue());
//        assertThat(op.getOutputName(), nullValue());
    }

    /** Test void method with one parameter, where parameter type is specified. */
    @Test public void testInstanceSpecificInputOnlyMethod() throws Exception {
        OperationDefinition opDef = new OperationDefinition();
        opDef.setMethodName("println");
        opDef.setInputClassName("java.lang.String");
        Operation op = Operation.newInstance(PrintStream.class, opDef);
//        assertThat(op.getOperationName(), equalTo("println"));
        assertThat(op.getInputClass().getName(), equalTo(String.class.getName()));
//        assertThat(op.getInputName(), equalTo("String"));
        assertThat(op.getOutputClass(), nullValue());
//        assertThat(op.getOutputName(), nullValue());
    }

    /** Test method with no parameters. */
    @Test public void testInstanceOutputOnlyMethod() throws Exception {
        OperationDefinition opDef = new OperationDefinition();
        opDef.setMethodName("length");
        Operation op = Operation.newInstance(StringBuffer.class, opDef);
//        assertThat(op.getOperationName(), equalTo("length"));
        assertThat(op.getInputClass(), nullValue());
//        assertThat(op.getInputName(), nullValue());
        assertThat(op.getOutputClass().getName(), equalTo(int.class.getName()));
//        assertThat(op.getOutputName(), equalTo("int"));
    }

    /** Test method with one parameter. Ensure it is selected over methods with multiple parameters.*/
    @Test public void testInstanceInOutMethod() throws Exception {
        OperationDefinition opDef = new OperationDefinition();
        opDef.setMethodName("startsWith");
        Operation op = Operation.newInstance(String.class, opDef);
//        assertThat(op.getOperationName(), equalTo("startsWith"));
        assertThat(op.getInputClass().getName(), equalTo(String.class.getName()));
//        assertThat(op.getInputName(), equalTo("String"));
        assertThat(op.getOutputClass().getName(), equalTo(boolean.class.getName()));
//        assertThat(op.getOutputName(), equalTo("boolean"));
    }

    /** Test method with InContext. Should be selected without specifying InContext parameter. */
    @Test public void testMethodWithInContext() throws Exception {
        OperationDefinition opDef = new OperationDefinition();
        opDef.setMethodName("inputOnlyMethodWithInContext");
        Operation op = Operation.newInstance(this.getClass(), opDef);
//        assertThat(op.getOperationName(), equalTo("inputOnlyMethodWithInContext"));
        assertThat(op.getInputClass().getName(), equalTo(Date.class.getName()));
        assertThat(op.getOutputClass(), nullValue());
    }
    
    /** Test method with InContext and OutContext. */
    @Test public void testMethodWithInAndOutContext() throws Exception {
        OperationDefinition opDef = new OperationDefinition();
        opDef.setMethodName("inOutMethodWithInAndOutContext");
        Operation op = Operation.newInstance(this.getClass(), opDef);
//        assertThat(op.getOperationName(), equalTo("inOutMethodWithInAndOutContext"));
        assertThat(op.getInputClass().getName(), equalTo(Float.class.getName()));
        assertThat(op.getOutputClass().getName(), equalTo(String.class.getName()));
    }
    
    /** Test with non existent method. */
    @Test (expected=WsConfigurationException.class)
    public void testNonMatchingMethodShouldThrowWsConfigurationException() throws Exception {
        OperationDefinition opDef = new OperationDefinition();
        opDef.setMethodName("methodWith3Params");
        Operation op = Operation.newInstance(StringBuffer.class, opDef);
    }    
    
    /** Test that specifying the output class still finds correct method. */
    @Test public void testMethodWithOutputClassSpecified() throws Exception {
        OperationDefinition opDef = new OperationDefinition();
        opDef.setMethodName("length");
        opDef.setOutputClassName("int");
        Operation op = Operation.newInstance(StringBuffer.class, opDef);
        assertThat(op.getOutputClass().getName(), equalTo(int.class.getName()));
    }
    
    /** Test that specifying the wrong output class errors. */
    @Test (expected=WsConfigurationException.class)
    public void testMethodWithWrongOutputClassSpecifiedThrowsWsConfigurationException() throws Exception {
        OperationDefinition opDef = new OperationDefinition();
        opDef.setMethodName("length");
        opDef.setOutputClassName("float");
        Operation op = Operation.newInstance(StringBuffer.class, opDef);
    }
    
//    /** Test setting of operation name, which is used for WSDL generation. */
//    @Test public void testSetOperationNameShouldAlterOperationName() throws Exception {
//        OperationDefinition opDef = parseIntOperation();
//        String opName = "testOperationName";
//        opDef.setOperationName(opName);
//        Operation op = Operation.newInstance(Integer.class, opDef);
//        assertThat(op.getOperationName(), equalTo(opName));
//    }
//
//    @Test public void testSetOperationNameShouldNotAlterOperationMethod() throws Exception {
//        OperationDefinition opDef = parseIntOperation();
//        String opName = "testOperationName";
//        opDef.setOperationName(opName);
//        Operation op = Operation.newInstance(Integer.class, opDef);
//        Object retval = op.invoke(null, "1", null);
//        assertThat((Integer)retval, is(1));
//    }
//    
//    /** Test setting of input message name, which is used for WSDL generation. */
//    @Test public void testSetInputMessageNameShouldAlterInputMessageName() throws Exception {
//        OperationDefinition opDef = parseIntOperation();
//        String testName = "testInName";
//        opDef.setInputMessageName(testName);
//        Operation op = Operation.newInstance(Integer.class, opDef);
//        assertThat(op.getInputName(), equalTo(testName));
//    }
//
//    /** Test setting of output message name, which is used for WSDL generation. */
//    @Test public void testSetOutputMessageNameShouldAlterOutputMessageName() throws Exception {
//        OperationDefinition opDef = parseIntOperation();
//        String testName = "testOutName";
//        opDef.setOutputMessageName(testName);
//        Operation op = Operation.newInstance(Integer.class, opDef);
//        assertThat(op.getOutputName(), equalTo(testName));
//    }

    private OperationDefinition parseIntOperation() {
        OperationDefinition opDef = new OperationDefinition();
        opDef.setMethodName("parseInt");
        return opDef;
    }
}
