package com.example.push.constant;

public interface WsConstants {
    /**
     * redis key 格式
     */
    String FORMAT = "ws:{}:{}";


    /**
     * 具体请求参数
     */
    String data = "data";
    /**
     * 常量,订阅类型
     * 请求参数: {"sub":"market.symbol.depth","data":{ "relationId":3,"pageSize":14,"accuracy":"8"}}
     */
    String subType = "sub";

    /**
     * 常量,取消订阅类型
     * 请求参数: {"unSub":"market.symbol.depth"}
     */
    String unSubType = "unSub";

    /**
     * 测试订阅 {"sub":"test_Sub", "data":{"xx":xx, "yy":"yy"}}
     */
    String TEST_SUB = "test_sub";
}
