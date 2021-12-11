package com.jvj28.homeworks.auth.contract;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

@Data
@NoArgsConstructor
public class ApiAuthErrorResponse implements Serializable {

    private static final long serialVersionUID = 1286742820087808052L;

    @JsonDeserialize(using = ApiAuthErrorResponse.TimeDeserializer.class)
    private LocalDateTime timestamp;
    private String message;
    private int status;

    public ApiAuthErrorResponse(LocalDateTime timestamp, String message, HttpStatus status) {
        this.timestamp = timestamp;
        this.message = message;
        this.status = status.value();
    }

    private static final class TimeDeserializer extends JsonDeserializer<LocalDateTime> {

        @Override
        public LocalDateTime deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
            try {
                return LocalDateTime.parse(jsonParser.getText());
            } catch (DateTimeParseException pe) {
                throw new JsonParseException(jsonParser, pe.getMessage());
            }
        }
    }
}
