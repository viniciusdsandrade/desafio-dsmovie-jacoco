package com.devsuperior.dsmovie.dto;

public sealed interface ErrorDTO permits CustomErrorDTO, ValidationErrorDTO {}
