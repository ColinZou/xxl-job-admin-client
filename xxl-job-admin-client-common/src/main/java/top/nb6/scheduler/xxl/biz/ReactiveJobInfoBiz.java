package top.nb6.scheduler.xxl.biz;

import top.nb6.scheduler.xxl.biz.exceptions.ApiInvokeException;
import top.nb6.scheduler.xxl.biz.exceptions.LoginFailedException;
import top.nb6.scheduler.xxl.biz.model.JobInfoDto;
import top.nb6.scheduler.xxl.biz.model.JobInfoListDto;
import reactor.core.publisher.Mono;

public interface ReactiveJobInfoBiz {
    /**
     * 查询任务信息
     *
     * @param jobGroupId    执行器ID
     * @param triggerStatus 任务状态，-1表示全部，0表示停止、1表示执行
     * @param jobDesc       任务描述
     * @param execHandler   执行Handler名称
     * @param author        负责人
     * @param offset        起始索引
     * @param count         期望返回数量行数
     * @return
     * @throws LoginFailedException
     * @throws ApiInvokeException
     */
    Mono<JobInfoListDto> query(long jobGroupId, int triggerStatus, String jobDesc,
                               String execHandler, String author, Integer offset, Integer count);

    /**
     * 创建任务调度
     *
     * @param dto 数据，请参照UI填写必填项
     * @return
     * @throws LoginFailedException
     * @throws ApiInvokeException
     */
    Mono<JobInfoDto> create(JobInfoDto dto);

    Mono<JobInfoDto> update(JobInfoDto dto);

    Mono<Boolean> remove(Long id);

    Mono<Boolean> startJob(Long id);

    Mono<Boolean> stopJob(Long id);
}
