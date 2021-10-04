package com.ibm.ssi.controller.hotel.util.validation;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.support.ResourceBundleMessageSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomPatternValidator implements ConstraintValidator<CustomPattern, String> {

  private final Logger log = LoggerFactory.getLogger(CustomPatternValidator.class);

  private final String general = "(?i).*[^\\sa-z0-9_.&äáâàăçéëêèïíììñóöôòøöșțüúüûùßÄÁÂÀĂÇÉËÊÈÏÍÌÌÑÓÖÔÒØÖȘẞȚÜÚÜÛÙ-].*";

  private final String password = "(?i).*[^a-z0-9_!@#$%^&*()-.?=].*";

  private final String id = "(?i).*[^a-z0-9_.äöüßÄÖÜẞ-].*";
  
  private String type;
  
  private ResourceBundleMessageSource messageSource;
  
  @Value("${spring.messages.basename}")
  private String basename;
 
  @Override
  public void initialize(CustomPattern constraint) {
    type = constraint.type();
    messageSource = new ResourceBundleMessageSource();
    messageSource.setBasenames(basename);
  }

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    if (value == null) {
      return true;
    }
    
    String errorMessage;
    Pattern pattern;
    Boolean nonBlocking = false;

    context.disableDefaultConstraintViolation();

    switch(type) {
      case "id":
        pattern = Pattern.compile(id);
        errorMessage = messageSource.getMessage("customPattern.error.id", null, Locale.getDefault());
        context.buildConstraintViolationWithTemplate(errorMessage).addConstraintViolation();
        break;
      case "password":
        pattern = Pattern.compile(password);
        errorMessage = messageSource.getMessage("customPattern.error.password", null, Locale.getDefault());
        context.buildConstraintViolationWithTemplate(errorMessage).addConstraintViolation();
        break;
      case "nonBlocking":
        pattern = Pattern.compile(general);
        errorMessage = messageSource.getMessage("customPattern.error.nonblocking", null, Locale.getDefault());
        nonBlocking = true;
      default:
        pattern = Pattern.compile(general);
        errorMessage = messageSource.getMessage("customPattern.error.general", null, Locale.getDefault());
        context.buildConstraintViolationWithTemplate(errorMessage).addConstraintViolation();
    }
    Matcher matcher = pattern.matcher(value);

    if (nonBlocking) {
      if (matcher.lookingAt()) {
        log.error(errorMessage);
      }

      return true;
    }

    return !matcher.lookingAt();
  }
}
