package eu.tailoringexpert.ratelimiting.domain;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class GenericEnumValidator implements ConstraintValidator<AllowedEnums, Enum<?>> {

    private Set<String> allowedValues;
    private String formattedAllowedValues;

    @Override
    public void initialize(AllowedEnums constraintAnnotation) {
        // Speichert die erlaubten Werte für den Abgleich
        this.allowedValues = Arrays.stream(constraintAnnotation.value())
                .map(String::toUpperCase)
                .collect(Collectors.toSet());

        this.formattedAllowedValues = Arrays.toString(constraintAnnotation.value());
    }

    @Override
    public boolean isValid(Enum<?> value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        boolean isValid = allowedValues.contains(value.name().toUpperCase());

        if (!isValid) {
            // 1. Standard-Fehlermeldung der Annotation abschalten
            context.disableDefaultConstraintViolation();

            // 2. Neue Fehlermeldung mit der dynamischen Liste zusammenbauen
            String dynamicMessage = String.format(
                    "Invalid value '%s'. Allowed Values: %s",
                    value.name(),
                    formattedAllowedValues);

            // 3. Den neuen Fehler an den Kontext übergeben
            context.buildConstraintViolationWithTemplate(dynamicMessage)
                    .addConstraintViolation();
        }

        return isValid;
    }
}
