package br.dev.ampliar.caipora.model;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;

import br.dev.ampliar.caipora.service.DeployService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Map;
import org.springframework.web.servlet.HandlerMapping;


/**
 * Validate that the rfc value isn't taken yet.
 */
@Target({ FIELD, METHOD, ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(
        validatedBy = DeployRfcUnique.DeployRfcUniqueValidator.class
)
public @interface DeployRfcUnique {

    String message() default "This RFC is already taken.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    class DeployRfcUniqueValidator implements ConstraintValidator<DeployRfcUnique, String> {

        private final DeployService deployService;
        private final HttpServletRequest request;

        public DeployRfcUniqueValidator(final DeployService deployService,
                final HttpServletRequest request) {
            this.deployService = deployService;
            this.request = request;
        }

        @Override
        public boolean isValid(final String value, final ConstraintValidatorContext cvContext) {
            if (value == null) {
                // no value present
                return true;
            }
            @SuppressWarnings("unchecked") final Map<String, String> pathVariables =
                    ((Map<String, String>)request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE));
            final String currentId = pathVariables.get("id");
            if (currentId != null && value.equalsIgnoreCase(deployService.get(Integer.parseInt(currentId)).getRfc())) {
                // value hasn't changed
                return true;
            }
            return !deployService.rfcExists(value);
        }

    }

}
