package cn.langya.login

import net.ccbluex.liquidbounce.authlib.account.MicrosoftAccount
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiScreen
import net.minecraft.util.Session
import org.apache.logging.log4j.LogManager
import java.awt.Desktop
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.net.URI

/**
 * @author LangYa466
 * @date 8/11/2025
 */
object LoginHandler {
    private val log = LogManager.getLogger()
    private val mc = Minecraft.getMinecraft()
    private var isLoginInProgress = false
    private const val MAX_RETRY_ATTEMPTS = 3
    private var retryAttempts = 0

    fun initiateLogin() {
        if (isLoginInProgress) {
            retry("Login is already in progress. Retrying...")
        } else {
            try {
                isLoginInProgress = true
                loginUsingMicrosoft()
            } catch (e: Exception) {
                retry("An error occurred during login. Retrying...")
                e.printStackTrace()
            }
        }
    }

    private fun retry(logMessage: String) {
        log.info(logMessage)
        if (retryAttempts < MAX_RETRY_ATTEMPTS) {
            retryAttempts++
            log.info("Retry attempt: $retryAttempts")
            isLoginInProgress = false
            initiateLogin()
        } else {
            log.error("Max retry attempts reached. Login failed.")
            isLoginInProgress = false
        }
    }

    private fun loginUsingMicrosoft() {
        MicrosoftAccount.buildFromOpenBrowser(object : MicrosoftAccount.OAuthHandler {
            override fun openUrl(url: String) {
                try {
                    if (!GuiScreen.isShiftKeyDown()) {
                        Desktop.getDesktop().run {
                            if (isSupported(Desktop.Action.BROWSE)) browse(URI(url))
                            else log.error("Opening URL is not supported.")
                        }
                    } else {
                        Toolkit.getDefaultToolkit().systemClipboard.setContents(StringSelection(url), null)
                    }
                } catch (e: Exception) {
                    retry("Failed to open the URL. Retrying...")
                    e.printStackTrace()
                }
            }

            override fun authResult(account: MicrosoftAccount) {
                try {
                    val session = account.login().first

                    mc.setFreshSession(Session(session.username, session.uuid.toString(), session.token))

                    log.info("Authentication successful for user: ${session.username}")
                    isLoginInProgress = false
                } catch (e: Exception) {
                    log.error("Authentication failed: ${e.message}")
                    isLoginInProgress = false
                }
            }

            override fun authError(error: String) {
                log.error("Microsoft authentication error: $error")
                isLoginInProgress = false
            }
        }, MicrosoftAccount.AuthMethod.AZURE_APP)
    }
}
