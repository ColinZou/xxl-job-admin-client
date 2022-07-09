package top.nb6.scheduler.xxl.http

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.TestInstance
import java.net.http.HttpRequest
import java.net.http.HttpResponse

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class XxlAdminHttpClientTest {
    companion object {
        val adminSiteConfigProps = XxlAdminSiteProperties(
            "http://localhost:8080/xxl-job-admin",
            "admin", "123456"
        )
        val client: XxlAdminHttpClient = XxlAdminHttpClient(adminSiteConfigProps)
        val log: java.util.logging.Logger = java.util.logging.Logger.getLogger(XxlAdminHttpClientTest::class.java.name)
    }

    @org.junit.jupiter.api.Test
    fun testDoLogin() {
        val response = client.request(
            ClientConstants.URI_JOB_INFO_LIST,
            HttpResponse.BodyHandlers.ofString(Constants.UTF_8),
            HttpRequest.BodyPublishers.noBody(), autoLogin = true
        )
        Assertions.assertNotNull(response)
        val statusCode = response.statusCode()
        if (statusCode != 200) {
            log.info("Got bad status code $statusCode and body ${response.body()}")
            response.headers().map().forEach { entry ->
                log.info("HEADER ${entry.key} = ${entry.value.joinToString { "," }}")
            }
        }
        Assertions.assertEquals(200, statusCode)
    }
}