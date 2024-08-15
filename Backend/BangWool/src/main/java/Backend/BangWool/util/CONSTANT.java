package Backend.BangWool.util;

import java.net.URI;

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

    // default image path
    private static final String imagePrefix = "https://bangwool-images.s3.ap-northeast-2.amazonaws.com/";

    public static final URI DEFAULT_PROFILE = URI.create(imagePrefix + "default-profile.jpg");

    // public static final URI DEFAULT_COS_BASIC = URI.create(imagePrefix + "default-cos-basic.jpg");
    // public static final URI DEFAULT_COS_BASE = URI.create(imagePrefix + "default-cos-base.jpg");
    // public static final URI DEFAULT_COS_COLOR = URI.create(imagePrefix + "default-cos-color.jpg");
    // public static final URI DEFAULT_COS_OTHERS = URI.create(imagePrefix + "default-cos-others.jpg");

    public static final URI DEFAULT_COS_BASIC = URI.create(imagePrefix + "default-profile.jpg");
    public static final URI DEFAULT_COS_BASE = URI.create(imagePrefix + "default-profile.jpg");
    public static final URI DEFAULT_COS_COLOR = URI.create(imagePrefix + "default-profile.jpg");
    public static final URI DEFAULT_COS_OTHERS = URI.create(imagePrefix + "default-profile.jpg");

}
