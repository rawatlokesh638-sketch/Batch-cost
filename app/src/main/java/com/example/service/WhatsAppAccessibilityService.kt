package com.example.service

import android.accessibilityservice.AccessibilityService
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class WhatsAppAccessibilityService : AccessibilityService() {
    private val TAG = "WhatsAppAccessibility"
    private var lastScannedNodeText = ""
    private var lastScanTime = 0L
    private val mainHandler = Handler(Looper.getMainLooper())
    private var isNavigatingBack = false

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        val pkg = event.packageName?.toString() ?: ""
        if (pkg != "com.whatsapp" && pkg != "com.whatsapp.w4b") return

        val now = System.currentTimeMillis()
        if (now - lastScanTime < 600) return
        lastScanTime = now

        try {
            val root = rootInActiveWindow ?: return
            val isAutoPilot = WhatsAppOrderCaptureHub.isAutoPilotRunning.value

            // 1. Detect if we are inside an active conversation
            val contactNameHeader = findContactHeader(root)
            val isInsideConversation = contactNameHeader != null

            if (isInsideConversation) {
                val contactName = contactNameHeader ?: "WhatsApp Customer"
                val messages = mutableListOf<String>()
                collectChatMessages(root, messages)

                // Scroll inspection: Check if conversation can be scrolled up slightly to see earlier order specs if brief
                if (messages.size < 3) {
                    val chatScrollable = findScrollable(root)
                    chatScrollable?.performAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD)
                }

                val latestMessagesText = messages.takeLast(8).joinToString("\n")
                if (latestMessagesText.isNotBlank() && latestMessagesText != lastScannedNodeText) {
                    lastScannedNodeText = latestMessagesText
                    Log.d(TAG, "Active conversation detected with $contactName: $latestMessagesText")

                    WhatsAppOrderCaptureHub.processCapturedWhatsAppMessage(
                        senderName = contactName,
                        messageText = latestMessagesText,
                        source = if (isAutoPilot) "Auto-Pilot Crawler" else "Live Screen",
                        context = applicationContext
                    )
                }

                // If Auto-Pilot is running, automatically press BACK after reading to scan the next chat!
                if (isAutoPilot && !isNavigatingBack) {
                    isNavigatingBack = true
                    mainHandler.postDelayed({
                        WhatsAppOrderCaptureHub.addCrawlerLog("↩️ Auto-Navigating Back to WhatsApp Chat List...")
                        performGlobalAction(GLOBAL_ACTION_BACK)
                        mainHandler.postDelayed({
                            isNavigatingBack = false
                        }, 1000)
                    }, 1400)
                }
            } else if (isAutoPilot && !isNavigatingBack) {
                // 2. We are in the main WhatsApp Chat List screen
                scanAndOpenNextChat(root)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error in WhatsAppAccessibility: ${e.message}")
        }
    }

    private fun findContactHeader(root: AccessibilityNodeInfo): String? {
        val queue = ArrayDeque<AccessibilityNodeInfo>()
        queue.add(root)
        while (queue.isNotEmpty()) {
            val node = queue.removeFirst()
            val id = node.viewIdResourceName ?: ""
            val text = node.text?.toString()?.trim() ?: ""

            if ((id.contains("conversation_contact_name", ignoreCase = true) ||
                 id.contains("conversation_title", ignoreCase = true)) && text.isNotBlank()) {
                return text
            }

            for (i in 0 until node.childCount) {
                node.getChild(i)?.let { queue.add(it) }
            }
        }
        return null
    }

    private fun collectChatMessages(node: AccessibilityNodeInfo?, outList: MutableList<String>) {
        if (node == null) return
        val id = node.viewIdResourceName ?: ""
        val text = node.text?.toString()?.trim() ?: ""

        if (id.contains("message_text", ignoreCase = true) && text.isNotBlank()) {
            if (!text.contains("AM") && !text.contains("PM") && text.length > 5) {
                outList.add(text)
            }
        } else if (text.length > 15 && !id.contains("entry", ignoreCase = true) && !id.contains("toolbar", ignoreCase = true)) {
            if (!text.contains("Type a message") && !text.contains("Search")) {
                outList.add(text)
            }
        }

        for (i in 0 until node.childCount) {
            collectChatMessages(node.getChild(i), outList)
        }
    }

    private fun scanAndOpenNextChat(root: AccessibilityNodeInfo) {
        val clickableChats = mutableListOf<Pair<String, AccessibilityNodeInfo>>()

        fun findChats(node: AccessibilityNodeInfo?) {
            if (node == null) return
            val id = node.viewIdResourceName ?: ""

            // Look for chat list row
            if (id.contains("conversations_row", ignoreCase = true) ||
                id.contains("contact_row", ignoreCase = true) ||
                (node.isClickable && id.contains("container", ignoreCase = true))) {

                var rowTitle = ""
                fun extractTitle(subNode: AccessibilityNodeInfo?) {
                    if (subNode == null) return
                    val subId = subNode.viewIdResourceName ?: ""
                    val t = subNode.text?.toString()?.trim() ?: ""
                    if ((subId.contains("conversations_row_contact_name", ignoreCase = true) ||
                         subId.contains("name", ignoreCase = true)) && t.isNotBlank()) {
                        rowTitle = t
                    }
                    for (j in 0 until subNode.childCount) extractTitle(subNode.getChild(j))
                }
                extractTitle(node)

                if (rowTitle.isNotBlank()) {
                    clickableChats.add(Pair(rowTitle, node))
                }
            }

            for (i in 0 until node.childCount) {
                findChats(node.getChild(i))
            }
        }

        findChats(root)

        // Find the first unvisited chat
        for ((title, node) in clickableChats) {
            if (!WhatsAppOrderCaptureHub.visitedContacts.contains(title)) {
                WhatsAppOrderCaptureHub.visitedContacts.add(title)
                WhatsAppOrderCaptureHub.addCrawlerLog("👉 Agent 3.8 Flash auto-opening chat with '$title'...")
                node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                return
            }
        }

        // If all visible rows scanned, try scrolling down to scan more
        val scrollableNode = findScrollable(root)
        if (scrollableNode != null) {
            WhatsAppOrderCaptureHub.addCrawlerLog("📜 Scrolling down to scan more chats...")
            scrollableNode.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
        } else {
            WhatsAppOrderCaptureHub.addCrawlerLog("🎉 All visible WhatsApp chats checked!")
            WhatsAppOrderCaptureHub.stopAutoPilot()
        }
    }

    private fun findScrollable(node: AccessibilityNodeInfo?): AccessibilityNodeInfo? {
        if (node == null) return null
        if (node.isScrollable) return node
        for (i in 0 until node.childCount) {
            val res = findScrollable(node.getChild(i))
            if (res != null) return res
        }
        return null
    }

    override fun onInterrupt() {
        Log.d(TAG, "WhatsAppAccessibilityService interrupted")
    }
}
