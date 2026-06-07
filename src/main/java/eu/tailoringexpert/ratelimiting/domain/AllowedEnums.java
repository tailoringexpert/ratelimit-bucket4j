package eu.tailoringexpert.ratelimiting.domain;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = GenericEnumValidator.class)
public  @interface AllowedEnums {
    // Nimmt die String-Namen der erlaubten Enum-Konstanten auf (z.B. {"SECONDS", "MINUTES"})
    String[] value(); 
    
    String message() default "Ausgewählter Wert ist für dieses Feld nicht erlaubt.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
