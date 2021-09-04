package eyobkt.webmonitor.exception;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * Thrown when there is an attempt to insert a record into a table with an already existing record 
 * that has identical values in the primary key fields
 */
public class PrimaryKeyConstraintViolationException extends SQLIntegrityConstraintViolationException {

  private static final long serialVersionUID = 1L;
}