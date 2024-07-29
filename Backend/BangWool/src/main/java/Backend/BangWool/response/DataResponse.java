package Backend.BangWool.response;

import lombok.Getter;

@Getter
public class DataResponse<T> extends ResponseDTO {

    private final T data;

    private DataResponse(T data) {
        super(StatusCode.OK.getStatus(), StatusCode.OK.getMessage());
        this.data = data;
    }

    public static <T> DataResponse<T> of(T data) {
        return new DataResponse<>(data);
    }
}