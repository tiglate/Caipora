package br.dev.ampliar.caipora.util;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public class ReferencedWarning {

    private String key = null;
    private List<Object> params = new ArrayList<>();

    public void addParam(final Object param) {
        params.add(param);
    }

    @SuppressWarnings("unused")
    public String toMessage() {
        String message = key;
        if (!params.isEmpty()) {
            message += "," + params.stream()
                    .map(Object::toString)
                    .collect(Collectors.joining(","));
        }
        return message;
    }

    public String getKey() {
        return key;
    }

    public void setKey(final String key) {
        this.key = key;
    }

    public List<Object> getParams() {
        return params;
    }

    @SuppressWarnings("unused")
    public void setParams(final List<Object> params) {
        this.params = params;
    }

}
