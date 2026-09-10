package com.valb.skipad;

import android.accessibilityservice.AccessibilityService;
import android.os.SystemClock;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.List;
import java.util.Locale;

/**
 * Serviço de acessibilidade que detecta o botão "Pular anúncios" do YouTube
 * e clica nele automaticamente.
 *
 * Só recebe eventos do pacote do YouTube (definido no XML de config), então
 * fica ocioso o resto do tempo — não faz polling, reage a eventos. Nenhuma
 * permissão de rede é usada.
 */
public class SkipAdService extends AccessibilityService {

    private static final String TAG = "SkipAd";
    private static final String YT = "com.google.android.youtube";

    // IDs conhecidos do botão de pular (variam entre versões do YouTube).
    private static final String[] SKIP_IDS = {
            YT + ":id/skip_ad_button",
            YT + ":id/skip_ad_button_text",
            YT + ":id/skip_ad_button_container",
    };

    // Textos/descrições que indicam um botão de pular (pt + en), minúsculos.
    private static final String[] SKIP_TEXTS = {
            "pular anúncios",
            "pular anúncio",
            "pular anuncios",
            "pular anuncio",
            "skip ads",
            "skip ad",
            "pular",
            "skip",
    };

    // Evita cliques repetidos no mesmo botão em rajada.
    private static final long CLICK_COOLDOWN_MS = 700;
    private long lastClickAt = 0;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null) return;
        CharSequence pkg = event.getPackageName();
        if (pkg == null || !YT.contentEquals(pkg)) return;

        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null) return;

        AccessibilityNodeInfo target = findSkipById(root);
        if (target == null) {
            target = scanForSkipText(root);
        }
        if (target != null) {
            clickNode(target);
        }
    }

    /** Procura o botão pelos IDs conhecidos. */
    private AccessibilityNodeInfo findSkipById(AccessibilityNodeInfo root) {
        for (String id : SKIP_IDS) {
            List<AccessibilityNodeInfo> found = root.findAccessibilityNodeInfosByViewId(id);
            if (found != null && !found.isEmpty()) {
                return found.get(0);
            }
        }
        return null;
    }

    /** Varre a árvore procurando um nó com texto/descrição de "pular". */
    private AccessibilityNodeInfo scanForSkipText(AccessibilityNodeInfo node) {
        if (node == null) return null;

        CharSequence text = node.getText();
        CharSequence desc = node.getContentDescription();
        if (matchesSkip(text) || matchesSkip(desc)) {
            return node;
        }

        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            if (child == null) continue;
            AccessibilityNodeInfo hit = scanForSkipText(child);
            if (hit != null) return hit;
        }
        return null;
    }

    private boolean matchesSkip(CharSequence cs) {
        if (cs == null) return false;
        String s = cs.toString().trim().toLowerCase(Locale.ROOT);
        if (s.isEmpty()) return false;
        for (String t : SKIP_TEXTS) {
            if (s.equals(t) || s.startsWith(t)) return true;
        }
        return false;
    }

    /** Sobe até um ancestral clicável e executa o clique, com cooldown. */
    private void clickNode(AccessibilityNodeInfo node) {
        long now = SystemClock.uptimeMillis();
        if (now - lastClickAt < CLICK_COOLDOWN_MS) return;

        AccessibilityNodeInfo n = node;
        while (n != null && !n.isClickable()) {
            n = n.getParent();
        }
        AccessibilityNodeInfo clickable = (n != null) ? n : node;

        boolean ok = clickable.performAction(AccessibilityNodeInfo.ACTION_CLICK);
        if (ok) {
            lastClickAt = now;
            Log.d(TAG, "Anúncio pulado.");
        }
    }

    @Override
    public void onInterrupt() {
        // nada a fazer
    }
}
