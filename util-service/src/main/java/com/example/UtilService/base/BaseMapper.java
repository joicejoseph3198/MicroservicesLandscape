package com.example.UtilService.base;

import org.mapstruct.Mapping;

import java.util.List;
public interface BaseMapper<E, D> {
    D toDto(E entity);
    E toEntity(D dto);
    List<D> toDtoList(List<E> entityList);
    List<E> toEntityList(List<D> dtoList);
}


