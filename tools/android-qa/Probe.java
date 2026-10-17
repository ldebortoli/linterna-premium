package qa.premium;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Activity;
import android.app.Instrumentation;
import android.app.UiAutomation;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Settings;
import android.view.accessibility.AccessibilityNodeInfo;
import org.json.JSONArray;
import org.json.JSONObject;

/** Opt-in platform test helper; this APK is never part of either product. */
public class Probe extends Instrumentation {
    private Bundle arguments;
    private boolean acted;

    @Override public void onCreate(Bundle value) { arguments = value; start(); }
    private String value(CharSequence text) { return text == null ? "" : text.toString(); }

    private void visit(AccessibilityNodeInfo node, JSONArray all, String query, String action) throws Exception {
        if (node == null) return;
        Rect bounds = new Rect(); node.getBoundsInScreen(bounds);
        JSONObject record = new JSONObject();
        record.put("text", value(node.getText()));
        record.put("description", value(node.getContentDescription()));
        record.put("package", value(node.getPackageName()));
        record.put("class", value(node.getClassName()));
        record.put("bounds", new JSONArray(new int[]{bounds.left, bounds.top, bounds.right, bounds.bottom}));
        record.put("clickable", node.isClickable());
        record.put("enabled", node.isEnabled());
        record.put("visible", node.isVisibleToUser());
        record.put("focusable", node.isFocusable());
        record.put("focused", node.isFocused());
        record.put("accessibilityFocused", node.isAccessibilityFocused());
        record.put("selected", node.isSelected());
        record.put("checkable", node.isCheckable());
        record.put("checked", node.isChecked());
        record.put("scrollable", node.isScrollable());
        record.put("actions", node.getActions());
        record.put("liveRegion", node.getLiveRegion());
        if (Build.VERSION.SDK_INT >= 30) record.put("stateDescription", value(node.getStateDescription()));
        boolean scrollAction = action.equals("scroll") && node.isScrollable();
        if (!acted && (scrollAction || (!query.isEmpty() && (value(node.getText()).contains(query)
                || value(node.getContentDescription()).contains(query))))) {
            int id = action.equals("focus") ? AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS
                    : action.equals("click") ? AccessibilityNodeInfo.ACTION_CLICK
                    : scrollAction ? AccessibilityNodeInfo.ACTION_SCROLL_FORWARD : 0;
            if (id != 0) {
                AccessibilityNodeInfo target = node;
                while (!scrollAction && !target.isClickable() && target.getParent() != null) target = target.getParent();
                record.put("performed", target.performAction(id));
                acted = true;
                target.refresh();
                record.put("targetAccessibilityFocused", target.isAccessibilityFocused());
                record.put("targetClass", value(target.getClassName()));
            }
        }
        all.put(record);
        for (int i = 0; i < node.getChildCount(); i++) visit(node.getChild(i), all, query, action);
    }

    @Override public void onStart() {
        Bundle result = new Bundle();
        try {
            UiAutomation automation = getUiAutomation(UiAutomation.FLAG_DONT_SUPPRESS_ACCESSIBILITY_SERVICES);
            AccessibilityServiceInfo info = automation.getServiceInfo();
            info.flags |= AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS
                    | AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS;
            automation.setServiceInfo(info);
            AccessibilityNodeInfo root = null;
            long deadline = SystemClock.uptimeMillis() + 20_000;
            while (root == null && SystemClock.uptimeMillis() < deadline) {
                SystemClock.sleep(200); root = automation.getRootInActiveWindow();
            }
            if (root == null) throw new IllegalStateException("No accessibility root after 20 seconds");
            JSONArray nodes = new JSONArray();
            visit(root, nodes, arguments.getString("query", ""), arguments.getString("action", ""));
            JSONObject output = new JSONObject(); output.put("nodes", nodes);
            output.put("services", Settings.Secure.getString(getContext().getContentResolver(), "enabled_accessibility_services"));
            result.putString("json", output.toString());
            finish(Activity.RESULT_OK, result);
        } catch (Exception error) {
            result.putString("error", error.toString()); finish(Activity.RESULT_CANCELED, result);
        }
    }
}
