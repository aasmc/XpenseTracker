package ru.aasmc.xpensemanager.utils

import androidx.test.platform.app.InstrumentationRegistry
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import ru.aasmc.xpensemanager.data.network.CONVERT_ENDPOINT
import ru.aasmc.xpensemanager.util.logging.Logger
import ru.aasmc.xpensemanager.util.logging.LoggerImpl
import java.io.IOException
import java.io.InputStream
import java.util.concurrent.atomic.AtomicInteger

class FakeServer {
    private val mockWebServer = MockWebServer()
    private val logger: Logger = LoggerImpl()

    private val endPointSeparator = "/"
    private val responsesBasePath = "networkresponses/"
    private val convertEndpointPath = endPointSeparator + CONVERT_ENDPOINT
    private val notFoundResponse = MockResponse().setResponseCode(404)
    private val counter = AtomicInteger(0)

    val baseEndPoint
        get() = mockWebServer.url(endPointSeparator)

    fun start() {
        mockWebServer.start(8095)
    }

    fun setHappyPathDispatcher(vararg assets: String) {
        mockWebServer.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                val path = request.path ?: return notFoundResponse

                return with(path) {
                    when {
                        startsWith(convertEndpointPath) -> {
                            val asset = if (counter.get() == 0) {
                                assets[0]
                            } else if (counter.get() == 1) {
                                assets[1]
                            } else {
                                ""
                            }
                            counter.getAndIncrement()
                            MockResponse()
                                .setResponseCode(200)
                                .setBody(getJson("${responsesBasePath}${asset}.json"))
                        }
                        else -> {
                            notFoundResponse
                        }
                    }
                }
            }
        }
    }

    fun shutDown() {
        counter.set(0)
        mockWebServer.shutdown()
    }

    private fun getJson(path: String): String {
        return try {
            val context = InstrumentationRegistry.getInstrumentation().context
            val jsonStream: InputStream = context.assets.open(path)
            String(jsonStream.readBytes())
        } catch (e: IOException) {
            logger.e(e, "Error reading network response json asset")
            throw e
        }
    }
}
