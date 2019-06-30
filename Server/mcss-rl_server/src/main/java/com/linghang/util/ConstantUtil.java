package com.linghang.util;

public class ConstantUtil {

    public static final int BUFLENGTH = 10240;
    public static final int _2M = 2097152;
    public static final int _16M = 16777216;
    public static final int _1K = 1024;
    public static final int _3K = 3072;
    public static final int _5K = 5120;

    public static final Integer UPLOADED = 0;
    public static final Integer RSCALCED = 1;
    public static final Integer LAGCALCED = 2;

    public static final String SERVER_PROPERTY_NAME = "server.properties";
    public static final Long FILE_READ_INIT_FLG = 0L;
    public static final Integer START_SEND_CODE = -100;
    public static final Integer SEND_FINISH_CODE = -200;
    public static final Integer LAG_CALC_FINISH_CODE = -300;
    public static final Integer SEND_ERROR_CODE = -500;
    public static final Integer LAG_CALC_RPC_PORT = 9600;
    public static final Integer RS_CALC_RPC_PORT = 9700;
    public static final Integer GET_DATA_SERVICE_PORT = 9800;
    public static final Integer SEND_FILE_SERVICE_PORT = 9999;
}
