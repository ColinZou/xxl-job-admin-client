package top.nb6.scheduler.xxl.biz

import com.google.gson.Gson
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import top.nb6.scheduler.xxl.biz.exceptions.ApiInvokeException
import top.nb6.scheduler.xxl.biz.model.JobGroupDto
import top.nb6.scheduler.xxl.biz.model.JobGroupListDto
import top.nb6.scheduler.xxl.biz.model.JobInfoDto
import top.nb6.scheduler.xxl.biz.model.JobInfoListDto
import top.nb6.scheduler.xxl.biz.model.types.FlagConstants
import top.nb6.scheduler.xxl.http.*
import top.nb6.scheduler.xxl.utils.FormUtils
import java.net.URLEncoder
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets
import java.util.*

fun validateJsonResponse(client: XxlAdminHttpClient, jsonContent: String) {
    val responseCheck = client.isErrorJsonResponse(jsonContent)
    if (!responseCheck.first) {
        throw ApiInvokeException(
            responseCheck.second ?: "Unknown"
        )
    }
}

class JobGroupBizImpl(private val client: XxlAdminHttpClient) :
    JobGroupBiz {
    companion object {
        val log: Logger = LoggerFactory.getLogger(JobGroupBizImpl::class.java)
    }

    override fun query(appName: String?, title: String?, offset: Int?, count: Int?): JobGroupListDto {
        val queryForm = "appname=${URLEncoder.encode(appName ?: "", ClientConstants.utf8)}&title=${
            URLEncoder.encode(
                title ?: "",
                ClientConstants.utf8
            )
        }&start=${offset ?: 0}&length=${count ?: 1000}"
        val response = client.request(
            ClientConstants.URI_JOB_GROUP_LIST, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8),
            HttpRequest.BodyPublishers.ofString(queryForm),
            "POST",
            contentType = Constants.CONTENT_TYPE_URL_FORM_ENCODED
        )
        if (log.isDebugEnabled) {
            log.debug("Got jobgroup data ${response.body()} for query $queryForm")
        }
        val content = response.body()
        return Gson().fromJson(content, JobGroupListDto::class.java)
    }

    override fun create(appName: String?, title: String?, registerType: Int?, addressList: String?): JobGroupDto {
        return internalCreateOrUpdate(
            ClientConstants.URI_JOB_GROUP_CREATE,
            0,
            appName,
            title,
            registerType,
            addressList
        )
    }

    override fun update(
        id: Long,
        appName: String?,
        title: String?,
        registerType: Int?,
        addressList: String?
    ): JobGroupDto {
        return internalCreateOrUpdate(
            ClientConstants.URI_JOB_GROUP_UPDATE,
            id,
            appName,
            title,
            registerType,
            addressList
        )
    }

    override fun delete(id: Long?): JobGroupListDto {
        val formData = "id=$id"
        val response = client.request(
            ClientConstants.URI_JOB_GROUP_REMOVE, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8),
            HttpRequest.BodyPublishers.ofString(formData),
            "POST",
            contentType = Constants.CONTENT_TYPE_URL_FORM_ENCODED
        )
        if (log.isDebugEnabled) {
            log.debug("Got response data ${response.body()} for delete request $formData")
        }
        validateJsonResponse(client, response.body())
        val currentList = query(null, null, null, null)
        val existed = currentList.data.firstOrNull { it.id == id }
        if (Objects.nonNull(existed)) {
            throw ApiInvokeException("Job group id=$id was not removed")
        }
        return currentList
    }

    private fun internalCreateOrUpdate(
        uri: String,
        id: Long,
        appName: String?,
        title: String?,
        registerType: Int?,
        addressList: String?
    ): JobGroupDto {
        val generalParams = "appname=${URLEncoder.encode(appName, ClientConstants.utf8) ?: ""}&title=${
            URLEncoder.encode(
                title,
                ClientConstants.utf8
            )
        }&addressType=${registerType ?: 0}&addressList=${addressList ?: ""}"
        val body = if (id > 0) {
            "$generalParams&id=$id"
        } else {
            generalParams
        }
        if (log.isDebugEnabled) {
            log.debug("Trying call $uri with data $body")
        }
        val response = client.request(
            uri,
            HttpResponse.BodyHandlers.ofString(ClientConstants.utf8),
            HttpRequest.BodyPublishers.ofString(body, ClientConstants.utf8),
            "POST",
            contentType = Constants.CONTENT_TYPE_URL_FORM_ENCODED
        )
        validateJsonResponse(client, response.body())
        val queryResult = query(appName, title, null, null)
        if (queryResult.recordsTotal > 0 && queryResult.data.size > 0) {
            return queryResult.data[0]
        } else {
            throw ApiInvokeException("Failed to save jobgroup")
        }
    }
}

class JobInfoBizImpl(private val client: XxlAdminHttpClient) : JobInfoBiz {
    companion object {
        private val log: Logger = LoggerFactory.getLogger(JobGroupBizImpl::class.java)
    }

    override fun query(
        jobGroupId: Long,
        triggerStatus: Int,
        jobDesc: String?,
        execHandler: String?,
        author: String?,
        offset: Int?,
        count: Int?
    ): JobInfoListDto {
        val formBody = FormUtils.build(
            mapOf(
                "jobGroup" to jobGroupId.toString(),
                "triggerStatus" to triggerStatus.toString(),
                "jobDesc" to (jobDesc ?: ""),
                "executorHandler" to (execHandler ?: ""),
                "author" to (author ?: ""),
                "start" to (offset ?: 0).toString(),
                "length" to (count ?: 1000).toString()
            )
        )
        if (log.isDebugEnabled) {
            log.debug("Trying to query jobinfo, form body=$formBody")
        }
        val response = client.request(
            ClientConstants.URI_JOB_INFO_LIST,
            HttpResponse.BodyHandlers.ofString(ClientConstants.utf8),
            HttpRequest.BodyPublishers.ofString(formBody),
            "POST",
            contentType = Constants.CONTENT_TYPE_URL_FORM_ENCODED
        )
        return Gson().fromJson(response.body(), JobInfoListDto::class.java)
    }

    private fun retrieveJobInfoById(jobGroupId: Long, jobId: Long): JobInfoDto {
        val jobList = query(
            jobGroupId,
            FlagConstants.JOB_QRY_TRIGGER_STATUS_ALL,
            null,
            null,
            null,
            null,
            null
        )
        val result = jobList.data.firstOrNull {
            it.id == jobId
        }
        if (Objects.isNull(result)) {
            throw ApiInvokeException("Failed to create schedule")
        }
        return result!!
    }

    override fun create(dto: JobInfoDto?): JobInfoDto {
        val formBody = dto?.let { FormUtils.build(FormUtils.jobInfoDtoAsMap(it)) } ?: ""
        if (formBody.isEmpty()) {
            throw ApiInvokeException("Empty request form data")
        }
        if (log.isDebugEnabled) {
            log.debug("Trying to create a schedule $formBody")
        }
        val response = client.request(
            ClientConstants.URI_JOB_INFO_ADD,
            HttpResponse.BodyHandlers.ofString(ClientConstants.utf8),
            HttpRequest.BodyPublishers.ofString(formBody),
            "POST",
            contentType = Constants.CONTENT_TYPE_URL_FORM_ENCODED
        )
        if (log.isDebugEnabled) {
            log.debug("Got ${response.body()} with request form $formBody")
        }
        validateJsonResponse(client, response.body())
        val data = Gson().fromJson(response.body(), JobInfoCreationResponse::class.java)
        val jobInfoId = data.content?.toLong() ?: 0L
        if (jobInfoId <= 0) {
            throw ApiInvokeException("Failed to create schedule")
        }
        return retrieveJobInfoById(dto?.jobGroup ?: 0, jobInfoId)
    }

    override fun update(dto: JobInfoDto?): JobInfoDto {
        val formPart = dto?.let { FormUtils.build(FormUtils.jobInfoDtoAsMap(it)) } ?: ""
        if (formPart.isEmpty()) {
            throw ApiInvokeException("Empty request form data")
        }
        val formBody = "$formPart&id=${dto?.id ?: 0}"
        if (log.isDebugEnabled) {
            log.debug("Trying to create a schedule $formBody")
        }
        val response = client.request(
            ClientConstants.URI_JOB_INFO_UPDATE,
            HttpResponse.BodyHandlers.ofString(ClientConstants.utf8),
            HttpRequest.BodyPublishers.ofString(formBody),
            "POST",
            contentType = Constants.CONTENT_TYPE_URL_FORM_ENCODED
        )
        validateJsonResponse(client, response.body())
        return retrieveJobInfoById(dto?.jobGroup ?: 0, dto?.id ?: 0)
    }

    override fun remove(id: Long?): Boolean {
        return internalJobAction(id, ClientConstants.URI_JOB_INFO_REMOVE)
    }

    override fun startJob(id: Long?): Boolean {
        return internalJobAction(id, ClientConstants.URI_JOB_SCHEDULE_START)
    }

    override fun triggerOnce(id: Long?, params: String?): Boolean {
        val formData = FormUtils.build(
            mapOf(
                "id" to (id ?: 0).toString(),
                "executorParam" to (params ?: ""),
                "addressList" to ""
            )
        )
        if (log.isDebugEnabled) {
            log.debug("Trying to trigger a job with form $formData")
        }
        val response = client.request(
            ClientConstants.URI_JOB_TRIGGER,
            HttpResponse.BodyHandlers.ofString(ClientConstants.utf8),
            HttpRequest.BodyPublishers.ofString(formData),
            "POST",
            contentType = Constants.CONTENT_TYPE_URL_FORM_ENCODED
        )
        validateJsonResponse(client, response.body())
        val jsonResponse = Gson().fromJson(response.body(), CommonAdminApiResponse::class.java)
        return jsonResponse.ok()
    }

    override fun stopJob(id: Long?): Boolean {
        return internalJobAction(id, ClientConstants.URI_JOB_SCHEDULE_STOP)
    }

    private fun internalJobAction(id: Long?, uri: String): Boolean {
        val formBody = FormUtils.build(mapOf("id" to id.toString()))
        val response = client.request(
            uri,
            HttpResponse.BodyHandlers.ofString(ClientConstants.utf8),
            HttpRequest.BodyPublishers.ofString(formBody),
            "POST",
            contentType = Constants.CONTENT_TYPE_URL_FORM_ENCODED
        )
        validateJsonResponse(client, response.body())
        return true
    }
}