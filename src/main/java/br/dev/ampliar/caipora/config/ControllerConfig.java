package br.dev.ampliar.caipora.config;

import org.springframework.beans.propertyeditors.StringTrimmerEditor;

import java.beans.PropertyEditorSupport;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;


@ControllerAdvice
public class ControllerConfig {

    private static final String[] PASSWORD_FIELDS = {"password", "currentPassword", "newPassword", "confirmPassword"};

    @InitBinder
    public void initBinder(final WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
        // passwords are taken verbatim (a passphrase may legitimately start or end with a space)
        for (final String field : PASSWORD_FIELDS) {
            binder.registerCustomEditor(String.class, field, new PropertyEditorSupport() {
                @Override
                public void setAsText(final String text) {
                    setValue(text == null || text.isEmpty() ? null : text);
                }
            });
        }
    }

}
