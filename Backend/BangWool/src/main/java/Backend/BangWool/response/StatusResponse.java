package Backend.BangWool.response;

import java.util.Map;

public class StatusResponse extends ResponseDTO {

    private static final Map<Integer, StatusCode> codeMap = Map.of(
            200, StatusCode.OK,
            400, StatusCode.BAD_REQUEST,
            404, StatusCode.NOT_FOUND,
            500, StatusCode.INTERNAL_SERVER_ERROR
    );

    private StatusResponse(StatusCode statusCode) {
        super(statusCode.getStatus(), statusCode.getMessage());
    }

    private StatusResponse(StatusCode statusCode, Exception e) {
        super(statusCode.getStatus(), statusCode.getMessage(e));
    }

    private StatusResponse(StatusCode statusCode, String message) {
        super(statusCode.getStatus(), statusCode.getMessage() + " - " + message);
    }



    public static StatusResponse build(StatusCode statusCode) {
        return new StatusResponse(statusCode);
    }

    public static StatusResponse build(StatusCode statusCode, Exception e) {
        return new StatusResponse(statusCode, e);
    }

    public static StatusResponse build(StatusCode statusCode, String message) {
        return new StatusResponse(statusCode, message);
    }


    public static StatusResponse build(int numCode) {
        return new StatusResponse(codeMap.get(numCode));
    }

    public static StatusResponse build(int numCode, Exception e) {
        return new StatusResponse(codeMap.get(numCode), e);
    }

    public static StatusResponse build(int numCode, String message) {
        return new StatusResponse(codeMap.get(numCode), message);
    }

}