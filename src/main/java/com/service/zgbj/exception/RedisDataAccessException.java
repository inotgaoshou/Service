package com.service.zgbj.exception;

import org.springframework.dao.DataAccessException;

/**
 */
public class RedisDataAccessException extends DataAccessException{

    public RedisDataAccessException(String message) {
        super(message);
    }

    public RedisDataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
