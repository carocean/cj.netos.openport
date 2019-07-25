package cj.studio.openport.client;

import java.util.Map;

/**
 * 请求命令适配器，用于调用远程任意服务
 */
public interface IRequestAdapter {
    /**
     * 请求
     *
     * @param command    命令行格式：post|get,传null默认get，可为空
     * @param protocol   协议，传null默认:http/1.1，可为空
     * @param headers    请求头，可为空。 建议写法：<br>
     *                   new HashMap() {<br>
     *                   {<br>
     *                   put("Name", "Unmi");<br>
     *                   put("QQ", "1125535");<br>
     *                   }<br>
     *                   }<br>
     * @param parameters 请求参，可为空。建议写法:<br>
     *                   new HashMap() {<br>
     *                   {<br>
     *                   put("Name", "Unmi");<br>
     *                   put("QQ", "1125535");<br>
     *                   }<br>
     *                   }<br>
     * @param data       内容，可以为null，如果不是post将忽略
     * @return 返回可能是结果，也可能是异常
     */
    String request(String command, String protocol, Map<String, String> headers, Map<String, String> parameters, byte[] data);
}
