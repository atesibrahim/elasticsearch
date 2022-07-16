package com.ates.elasticsearch.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.data.elasticsearch.annotations.Field;

import static org.springframework.data.elasticsearch.annotations.FieldType.Text;

@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
@Builder
public class Author {

    @Field(type = Text)
    private String name;
}
