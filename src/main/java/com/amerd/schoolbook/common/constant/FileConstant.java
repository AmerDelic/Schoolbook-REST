package com.amerd.schoolbook.common.constant;

public class FileConstant {
    public static final String DEFAULT_IMAGE_PATH = "src/main/resources/temp/images/temp-profile.jpg";
    public static final String USER_IMAGE_PATH = "/user/profile/image/%s.jpg";
    public static final String JPG_EXTENSION = "jpg";
    public static final String USER_FOLDER = "file://" + System.getProperty("user.home") + "/supportportal/user/";
    public static final String USER_TEMP_FOLDER = USER_FOLDER + "temp";
    public static final String DIRECTORY_CREATED = "Created directory for: %s -- path: %s";

    public static final String FILE_SAVED_IN_FILE_SYSTEM = "Saved file: %s";
    public static final String DOT = ".";
    public static final String TEMP_PROFILE_IMAGE_BASE_ULR = "https://robohash.org/%s";
    public static final String DEFAULT_PROFILE_IMAGE = "temp-profile-img";
}
