package com.example.actions

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.core.content.ContextCompat
import com.example.data.model.ActionResult
import java.net.URLEncoder

class AndroidActionExecutor(
    private val context: Context,
    val contactResolver: SmartContactResolver = SmartContactResolver(context)
) {

    /**
     * Launch an application by name or package
     */
    fun openApp(appName: String, packageName: String? = null): ActionResult {
        val pm = context.packageManager

        // 1. Direct package match if provided
        if (!packageName.isNullOrBlank()) {
            val launchIntent = pm.getLaunchIntentForPackage(packageName)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
                return ActionResult(true, "$appName khol rahi hoon.")
            }
        }

        // 2. Known common app packages
        val lower = appName.lowercase()
        val targetPkg = when {
            lower.contains("whatsapp") -> "com.whatsapp"
            lower.contains("youtube") -> "com.google.android.youtube"
            lower.contains("chrome") -> "com.android.chrome"
            lower.contains("camera") -> {
                val camIntent = Intent("android.media.action.IMAGE_CAPTURE").apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                if (camIntent.resolveActivity(pm) != null) {
                    context.startActivity(camIntent)
                    return ActionResult(true, "Camera khol rahi hoon.")
                }
                null
            }
            lower.contains("settings") -> {
                val setIntent = Intent(Settings.ACTION_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(setIntent)
                return ActionResult(true, "Settings khol rahi hoon.")
            }
            lower.contains("dialer") || lower.contains("phone") -> {
                val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(dialIntent)
                return ActionResult(true, "Phone dialer khol rahi hoon.")
            }
            else -> null
        }

        if (targetPkg != null) {
            val launchIntent = pm.getLaunchIntentForPackage(targetPkg)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
                return ActionResult(true, "$appName khol rahi hoon.")
            }
        }

        // 3. Fallback: Search installed launcher applications
        try {
            val intent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            val apps = pm.queryIntentActivities(intent, 0)
            val matchedApp = apps.firstOrNull {
                val label = it.loadLabel(pm).toString().lowercase()
                label.contains(lower) || lower.contains(label)
            }

            if (matchedApp != null) {
                val launchIntent = pm.getLaunchIntentForPackage(matchedApp.activityInfo.packageName)
                if (launchIntent != null) {
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(launchIntent)
                    return ActionResult(true, "${matchedApp.loadLabel(pm)} khol rahi hoon.")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return ActionResult(false, "App '$appName' phone mein install nahi mila.")
    }

    /**
     * Safely open phone dialer with number filled in
     */
    fun openDialer(phoneNumber: String, name: String? = null): ActionResult {
        val cleanNumber = phoneNumber.replace(Regex("[^0-9+]"), "")
        val uri = Uri.parse("tel:$cleanNumber")
        val intent = Intent(Intent.ACTION_DIAL, uri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return try {
            context.startActivity(intent)
            ActionResult(true, "${name ?: cleanNumber} ke liye phone dialer khol diya hai.")
        } catch (e: Exception) {
            ActionResult(false, "Dialer nahi khul paya: ${e.message}")
        }
    }

    /**
     * Direct call if CALL_PHONE permission is authorized
     */
    fun makeDirectCall(phoneNumber: String, name: String? = null): ActionResult {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.CALL_PHONE
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasPermission) {
            // Safe fallback to dialer
            return openDialer(phoneNumber, name)
        }

        val cleanNumber = phoneNumber.replace(Regex("[^0-9+]"), "")
        val uri = Uri.parse("tel:$cleanNumber")
        val intent = Intent(Intent.ACTION_CALL, uri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return try {
            context.startActivity(intent)
            ActionResult(true, "${name ?: cleanNumber} ko call lagayi ja rahi hai.")
        } catch (e: Exception) {
            openDialer(phoneNumber, name)
        }
    }

    /**
     * Prepare WhatsApp message and open sharing screen
     */
    fun prepareWhatsAppMessage(recipientPhone: String?, messageText: String): ActionResult {
        val cleanPhone = recipientPhone?.replace(Regex("[^0-9]"), "") ?: ""
        val encodedText = try {
            URLEncoder.encode(messageText, "UTF-8")
        } catch (e: Exception) {
            messageText
        }

        val url = if (cleanPhone.isNotBlank()) {
            "https://api.whatsapp.com/send?phone=$cleanPhone&text=$encodedText"
        } else {
            "https://api.whatsapp.com/send?text=$encodedText"
        }

        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            setPackage("com.whatsapp")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        return try {
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
                ActionResult(true, "WhatsApp par message taiyar kar diya hai.")
            } else {
                // Generic share fallback
                shareText("WhatsApp: $messageText")
            }
        } catch (e: Exception) {
            shareText(messageText)
        }
    }

    /**
     * Search the web / Google
     */
    fun searchWeb(query: String): ActionResult {
        return try {
            val intent = Intent(Intent.ACTION_WEB_SEARCH).apply {
                putExtra(SearchManager.QUERY, query)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
                ActionResult(true, "'$query' web par search kiya ja raha hai.")
            } else {
                val webUrl = "https://www.google.com/search?q=" + URLEncoder.encode(query, "UTF-8")
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(webUrl)).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(browserIntent)
                ActionResult(true, "'$query' Google par search kiya ja raha hai.")
            }
        } catch (e: Exception) {
            ActionResult(false, "Web search open nahi ho paya: ${e.message}")
        }
    }

    /**
     * Open system settings
     */
    fun openSettings(): ActionResult {
        return try {
            val intent = Intent(Settings.ACTION_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            ActionResult(true, "Settings kholi ja rahi hai.")
        } catch (e: Exception) {
            ActionResult(false, "Settings nahi khul payi: ${e.message}")
        }
    }

    /**
     * Share text with any supported app
     */
    fun shareText(text: String): ActionResult {
        return try {
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, text)
                type = "text/plain"
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val chooser = Intent.createChooser(sendIntent, "Share via").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
            ActionResult(true, "Share dialog khol diya gaya hai.")
        } catch (e: Exception) {
            ActionResult(false, "Sharing fail hui: ${e.message}")
        }
    }
}
