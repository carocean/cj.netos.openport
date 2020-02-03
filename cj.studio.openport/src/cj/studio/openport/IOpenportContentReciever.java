package cj.studio.openport;

import cj.studio.ecm.net.Circuit;
import cj.studio.ecm.net.CircuitException;
import cj.studio.ecm.net.Frame;

/**
 * 自开放口内容接收器，
 * <pre>
 *     用于解析请求并填充开放口方法参数。
 *
 * 接收器要实现两种能力：
 * 1.接收请求数据
 * 2.完成对方法参数的赋值，并将返回值写给客户端。注意在写晌应时尽量使用 @see ResponseClient响应格式，否则将与该开放口框架的下游生态不兼容。
 *
 * 系统默认采用了 @see DefaultOpenportContentReciever 作为接收存，该接收器使用了 MemoryContentReciever 作为请求解析器，
 * 很明显它不支持大文件的上传和下载，如果您需要让openport实现大数据传输，就需要定义您自己的IOpenportContentReciever。
 * 注：ecm系统API中提供了请求处理的基本内容，包括用于FormData解析的MultipartFormContentReciever，它支持无限文件大小传输，
 * XwwwFormUrlencodedContentReciever，通过使用上述内容接收器，可让您实现IOpenportContentReciever更简单
 * </pre>
 * @return
 */
public interface IOpenportContentReciever {

    /**
     * 响应数据接收
     * @param b
     * @param pos
     * @param length
     * @throws CircuitException
     */
    void ondataRecieve(byte[] b, int pos, int length) throws CircuitException;

    /**
     * 晌应数据开始
     * @param openportMethod
     * @param frame
     */
    void ondataBegin(IOpenportMethod openportMethod, Frame frame);

    /**
     * 响应数据接收完毕
     * @param b
     * @param pos
     * @param length
     * @throws CircuitException
     */
    void ondataDone(byte[] b, int pos, int length) throws CircuitException;

    /**
     * 执行开放口方法
     * <br>
     *
     * @param openportMethod 开放口方法,可执行目标方法，在执行前完成其内parametersArgsValues的填充
     * @param iSecuritySession
     * @param frame 请求
     * @param circuit 响应，将执行目标方法的返回值解析并写回客户端
     */
    void oninvoke(IOpenportMethod openportMethod, ISecuritySession iSecuritySession, Frame frame, Circuit circuit);
}
