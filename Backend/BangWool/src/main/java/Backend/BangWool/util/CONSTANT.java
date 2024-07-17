package Backend.BangWool.util;

public class CONSTANT {

    private CONSTANT() {}

    // Redis Prefix
    public static final String REDIS_EMAIL_CODE = "email:code:";
    public static final String REDIS_EMAIL_VERIFY = "email:verify:";
    public static final String REDIS_TOKEN = "token:";

    // JWT Header
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";
    public static final long ACCESS_EXPIRED = 60 * 60L; // (초 단위) 1시간
    public static final long REFRESH_EXPIRED = 14 * 24 * 60 * 60L; // (초 단위) 14일

}
