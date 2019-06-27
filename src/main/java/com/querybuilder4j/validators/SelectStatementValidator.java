package com.querybuilder4j.validators;

public interface SelectStatementValidator {

    boolean passesBasicValidation() throws Exception;
    boolean passesDatabaseValidation() throws Exception;

}
