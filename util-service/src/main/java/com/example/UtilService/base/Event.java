package com.example.UtilService.base;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.time.ZonedDateTime;

// Generic class for Event<Key,Data>
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Event<K,T> {
    //Event Type with allowed values
    public enum Type{
        CREATE,
        DELETE
    }
    private Event.Type eventType;
    private K key;
    private T data;
    private ZonedDateTime eventCreatedAt;
}
