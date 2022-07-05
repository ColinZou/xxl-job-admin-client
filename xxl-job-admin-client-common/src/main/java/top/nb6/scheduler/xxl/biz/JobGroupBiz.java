package top.nb6.scheduler.xxl.biz;

import top.nb6.scheduler.xxl.biz.exceptions.ApiInvokeException;
import top.nb6.scheduler.xxl.biz.exceptions.LoginFailedException;
import top.nb6.scheduler.xxl.biz.model.JobGroupDto;
import top.nb6.scheduler.xxl.biz.model.JobGroupListDto;

public interface JobGroupBiz {
    JobGroupListDto query(String appName, String title, Integer offset, Integer count) throws
        LoginFailedException;

    /**
     * 创建执行器
     *
     * @param appName      应用名称
     * @param title        标题
     * @param registerType 注册方式：0自动、1手动
     * @param addressList  执行器的地址列表
     * @return
     * @throws LoginFailedException
     * @throws ApiInvokeException
     */
    JobGroupDto create(String appName, String title, Integer registerType, String addressList)
        throws LoginFailedException, ApiInvokeException;

    JobGroupDto update(long id, String appName, String title, Integer registerType,
                       String addressList) throws LoginFailedException, ApiInvokeException;

    JobGroupListDto delete(Long id) throws LoginFailedException, ApiInvokeException;
}
