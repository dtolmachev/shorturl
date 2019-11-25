package com.dtolmachev.urlshortener.alias.table.exception;

public class WrongUrlFormatException extends RuntimeException {

    private final String urlCandidate;

    public WrongUrlFormatException(String urlCandidate) {
        super();
        this.urlCandidate = urlCandidate;
    }

    public WrongUrlFormatException(Throwable cause, String urlCandidate) {
        super(cause);
        this.urlCandidate = urlCandidate;
    }
}
