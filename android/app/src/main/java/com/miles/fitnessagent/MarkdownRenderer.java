package com.miles.fitnessagent;

import android.os.Build;
import android.text.Html;
import android.text.Spanned;

public class MarkdownRenderer {
    public static Spanned render(String markdown) {
        String html = escape(markdown == null ? "" : markdown);
        html = html.replaceAll("(?m)^### (.*)$", "<h3>$1</h3>");
        html = html.replaceAll("(?m)^## (.*)$", "<h2>$1</h2>");
        html = html.replaceAll("(?m)^# (.*)$", "<h1>$1</h1>");
        html = html.replaceAll("\\*\\*(.*?)\\*\\*", "<b>$1</b>");
        html = html.replaceAll("`([^`]+)`", "<code>$1</code>");
        html = html.replaceAll("(?m)^[-*] (.*)$", "&#8226; $1<br>");
        html = html.replace("\n", "<br>");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(html, Html.FROM_HTML_MODE_COMPACT);
        }
        return Html.fromHtml(html);
    }

    private static String escape(String value) {
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}
