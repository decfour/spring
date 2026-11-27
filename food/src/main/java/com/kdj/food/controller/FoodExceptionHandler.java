package com.kdj.food.controller;

import com.kdj.food.service.DuplicateReviewException;
import com.kdj.food.common.web.ApiError;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = {FoodController.class, FoodReviewController.class})
public class FoodExceptionHandler {
    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    ApiError notFound(EntityNotFoundException exception) {
        return new ApiError("NOT_FOUND", exception.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    ApiError forbidden(AccessDeniedException exception) {
        return new ApiError("FORBIDDEN", exception.getMessage());
    }

    @ExceptionHandler(DuplicateReviewException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    ApiError duplicateReview(DuplicateReviewException exception) {
        return new ApiError("DUPLICATE_REVIEW", exception.getMessage());
    }

}
