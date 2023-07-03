package love.xuqinqin.request.util;

import java.lang.reflect.Parameter;

public class ParameterAndValue {

    private Parameter parameter;

    private Object value;

    public ParameterAndValue() {
    }

    public ParameterAndValue(Parameter parameter, Object value) {
        this.parameter = parameter;
        this.value = value;
    }

    public Parameter getParameter() {
        return parameter;
    }

    public void setParameter(Parameter parameter) {
        this.parameter = parameter;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}
