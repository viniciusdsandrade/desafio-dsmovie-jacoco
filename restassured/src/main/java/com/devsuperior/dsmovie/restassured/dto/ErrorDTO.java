package com.devsuperior.dsmovie.restassured.dto;

public sealed interface ErrorDTO permits CustomErrorDTO, ValidationErrorDTO {}
